package com.xiaomi.watch5music.media

import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 本机信源：轮询【本机】MediaSessionManager 的活动媒体会话（手表本机安装的音乐 App 播放时）。
 * 与 [MediaNotifService]（蓝牙通知通道）互补：本机播放走这里，手机经蓝牙同步走 NLS。
 *
 * 注意：普通 App 的 getActiveSessions 在部分系统上拿不到其他包的会话（返回空），
 * 该通道尽力而为（runCatching 兜底），拿不到时界面数据由 NLS 通道提供。
 */
object MediaBridge {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var started = false

    fun start(context: Context) {
        if (started) return
        started = true
        scope.launch {
            var lastTitle: String? = null
            while (isActive) {
                runCatching {
                    val msm = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
                    val sessions = msm.getActiveSessions(null)
                    val s = sessions.firstOrNull {
                        !it.metadata?.getString(MediaMetadata.METADATA_KEY_TITLE).isNullOrBlank()
                    }
                    if (s != null) {
                        val meta = s.metadata
                        val title = meta?.getString(MediaMetadata.METADATA_KEY_TITLE)
                        if (!title.isNullOrBlank()) {
                            val dur = meta?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L
                            if (title != lastTitle) {
                                lastTitle = title
                                val art = meta?.getBitmap(MediaMetadata.METADATA_KEY_ART)
                                MediaSync.onMeta(
                                    title,
                                    meta?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "",
                                    (dur / 1000).toInt(),
                                    art
                                )
                                MediaSync.onConnectionChanged(true)
                            }
                            val st = s.playbackState
                            if (st != null) {
                                MediaSync.onPlaying(st.state == PlaybackState.STATE_PLAYING)
                                if (dur > 0) {
                                    MediaSync.onProgress((st.position.toFloat() / dur.toFloat()).coerceIn(0f, 1f))
                                }
                            }
                        }
                    }
                }
                delay(1000)
            }
        }
    }
}
