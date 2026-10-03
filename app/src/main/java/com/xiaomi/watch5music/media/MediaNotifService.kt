package com.xiaomi.watch5music.media

import android.app.Notification
import android.content.ComponentName
import android.content.Context
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
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

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
        // 媒体通知携带 MediaSession Token；只有它才包含可读的播放信息
        val token = sessionToken(extras) ?: return
        bind(token)
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
            MediaSync.onPlaying(st.state == PlaybackState.STATE_PLAYING)
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
