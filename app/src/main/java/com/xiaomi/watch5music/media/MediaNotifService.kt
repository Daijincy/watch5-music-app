package com.xiaomi.watch5music.media

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 通知监听服务：读取 Wear OS 同步到手表端、由手机播放产生的【媒体通知】。
 *
 * 原理：手机放歌 → 手机弹出媒体通知（带 MediaSession Token）→ 经蓝牙同步，
 * 在手表端投递；本服务作为 NotificationListenerService 读到该通知，用通知携带的
 * MediaSession.Token 创建 [MediaController] 读取精确的 歌名/歌手/时长/进度/播放状态，
 * 写入 [MediaSync] 驱动界面。
 *
 * 注意：Android 强制要求用户在系统设置 → 通知访问 中手动授权本服务，否则收不到通知。
 */
class MediaNotifService : NotificationListenerService() {

    companion object {
        /** 检查当前 App 是否已获得「通知访问」授权（Android 无运行时 API 直接申请，只能跳系统设置）。 */
        fun isGranted(context: Context): Boolean {
            val cn = ComponentName(context, MediaNotifService::class.java).flattenToString()
            val enabled = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners") ?: return false
            return enabled.split(":").contains(cn)
        }
    }

    private var controller: MediaController? = null
    private var durationMs = 0L
    private var basePosMs = 0L
    private var baseElapsed = 0L
    private var speed = 0f
    private var playing = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        // 界面下发的控制命令（播放/暂停/切歌/seek）→ MediaController.transportControls
        scope.launch {
            MediaSync.cmds.collect { c ->
                val t = controller?.transportControls ?: return@collect
                runCatching {
                    when (c) {
                        MediaSync.Cmd.Toggle -> if (playing) t.pause() else t.play()
                        MediaSync.Cmd.Next -> t.skipToNext()
                        MediaSync.Cmd.Prev -> t.skipToPrevious()
                        is MediaSync.Cmd.Seek -> t.seekTo(c.ms)
                    }
                }
            }
        }
    }

    private val cb = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) = push()
        override fun onPlaybackStateChanged(state: PlaybackState?) = push()
        override fun onSessionDestroyed() {
            MediaSync.onConnectionChanged(false)
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val n = sbn?.notification ?: return
        val extras = n.extras ?: return
        val token = sessionToken(extras)
        if (token != null) {
            bind(token)
            return
        }
        // token 丢失（蓝牙同步后常见）：直接从通知内容读歌名/歌手/封面兜底
        readFromExtras(extras)
    }

    /** 无 MediaSession token 时，尽力从通知 extras 解析基础媒体信息（歌名/歌手/封面）。 */
    private fun readFromExtras(extras: android.os.Bundle) {
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            ?: extras.getCharSequence("android.media.metadata.TITLE")?.toString()
        if (title.isNullOrBlank()) return

        // 歌手：媒体通知把 artist 存在 android.media.metadata.* 键下（无公开 EXTRA_ARTIST 常量）
        val artist = extras.getCharSequence("android.media.metadata.ARTIST")?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()

        var art: Bitmap? = null
        @Suppress("DEPRECATION")
        runCatching { art = extras.getParcelable(MediaMetadata.METADATA_KEY_ART) }
        if (art == null) {
            @Suppress("DEPRECATION")
            runCatching { art = extras.getParcelable(MediaMetadata.METADATA_KEY_ALBUM_ART) }
        }

        // 通知存在即视为连接中/播放中（无 token 无法拿到精确进度）
        MediaSync.onMeta(title, artist ?: "", 0, art)
        MediaSync.onPlaying(true)
        MediaSync.onConnectionChanged(true)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // 媒体通知被清除（停止播放/被划掉）→ 回到无数据态
        if (sbn != null && controller != null && sameToken(sbn)) {
            controller?.unregisterCallback(cb)
            controller = null
            MediaSync.onConnectionChanged(false)
        }
    }

    private fun bind(token: MediaSession.Token) {
        if (controller != null && controller?.sessionToken == token) return
        controller?.unregisterCallback(cb)
        controller = null
        val c = MediaController(this, token)
        controller = c
        c.registerCallback(cb)
        push()
        startProgressTicker()
    }

    /** 读一次当前媒体信息并写入 MediaSync */
    private fun push() {
        val c = controller ?: return
        val meta = c.metadata
        durationMs = meta?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L
        val title = meta?.getString(MediaMetadata.METADATA_KEY_TITLE)
        if (!title.isNullOrBlank()) {
            // 专辑封面：MediaSession 元数据里的 Bitmap（无则留空，界面走兜底）
            val art = meta.getBitmap(MediaMetadata.METADATA_KEY_ART)
            MediaSync.onMeta(
                title,
                meta.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "",
                (durationMs / 1000).toInt(),
                art
            )
            MediaSync.onConnectionChanged(true)
        }
        val st = c.playbackState
        if (st != null) {
            basePosMs = st.position
            baseElapsed = SystemClock.elapsedRealtime()
            speed = st.playbackSpeed
            playing = st.state == PlaybackState.STATE_PLAYING
            MediaSync.onPlaying(playing)
        }
    }

    /** 播放中持续推进进度（media 通知只推一次，position 靠本地 ticker 走） */
    private fun startProgressTicker() {
        scope.launch {
            while (isActive) {
                if (durationMs > 0) {
                    val now = basePosMs + ((SystemClock.elapsedRealtime() - baseElapsed) * speed).toLong()
                    MediaSync.onProgress((now.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f))
                }
                delay(500)
            }
        }
    }

    private fun sameToken(sbn: StatusBarNotification): Boolean =
        sbn.notification?.extras?.let { sessionToken(it) } == controller?.sessionToken

    private fun sessionToken(extras: android.os.Bundle): MediaSession.Token? =
        if (Build.VERSION.SDK_INT >= 33)
            extras.getParcelable(Notification.EXTRA_MEDIA_SESSION, MediaSession.Token::class.java)
        else
            @Suppress("DEPRECATION")
            extras.getParcelable(Notification.EXTRA_MEDIA_SESSION)
}
