package com.xiaomi.watch5music.media

import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.xiaomi.watch5music.ui.PlayerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 系统媒体桥：手表端通过系统 [MediaController] 读取手机正在播放的媒体信息。
 *
 * 原理：手机经蓝牙 AVRCP / Wear OS 媒体同步把 MediaSession 广播给手表，
 * 手表系统的 MediaSessionManager 会暴露这些活跃会话；本桥绑定到播放中的
 * 会话，把 歌名 / 歌手 / 时长 / 进度 / 播放状态 实时喂给 [PlayerViewModel]。
 * 无需手机安装任何配套 App。
 *
 * 注：系统通道不携带逐句歌词，歌词由 [PlayerViewModel.onLyric] 保留，
 * 供后续 BLE/数据层推送；此处歌词保持「等待推送」。
 */
class MediaBridge(private val context: Context, private val vm: PlayerViewModel) {

    private val msm = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
    private var controller: MediaController? = null
    private var durationMs = 0L
    private var basePosMs = 0L
    private var baseElapsed = 0L
    private var speed = 0f
    private val handler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // 会话列表变化（新设备/新播放会话出现或消失）时重新绑定
    private val sessionCb = object : MediaSessionManager.OnActiveSessionsChangedListener {
        override fun onActiveSessionsChanged(controllers: List<MediaController>) {
            bind(controllers)
        }
    }

    // 绑定会话的元信息 / 播放状态回调
    private val cb = object : MediaController.Callback() {
        override fun onSessionDestroyed() {
            controller = null
            vm.onConnectionChanged(false)
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            durationMs = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L
            val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
            if (!title.isNullOrBlank()) {
                vm.onMeta(
                    title,
                    metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "",
                    (durationMs / 1000).toInt()
                )
            }
        }

        override fun onPlaybackStateChanged(state: PlaybackState?) {
            if (state == null) {
                vm.onConnectionChanged(false)
                return
            }
            basePosMs = state.position
            baseElapsed = SystemClock.elapsedRealtime()
            speed = state.playbackSpeed
            vm.onPlaying(state.state == PlaybackState.STATE_PLAYING)
            // 只有处于可播放状态且已有歌名时，才算有数据
            val active = state.state != PlaybackState.STATE_NONE &&
                state.state != PlaybackState.STATE_STOPPED &&
                state.state != PlaybackState.STATE_ERROR &&
                !vm.state.value.title.isNullOrBlank()
            vm.onConnectionChanged(active)
        }
    }

    @Suppress("DEPRECATION") // Handler 版本在 API 31+ 弃用但仍可用，兼容 minSdk 26
    fun start() {
        msm.registerOnActiveSessionsChangedListener(sessionCb, handler)
        bind(msm.getActiveSessions(null) ?: emptyList())
        // 播放中由本桥持续推进进度（无需手机每秒推送）
        scope.launch {
            while (isActive) {
                vm.onProgress(currentProgress())
                delay(500)
            }
        }
    }

    private fun bind(controllers: List<MediaController>) {
        val current = controller
        if (current != null && controllers.contains(current)) return
        controller?.unregisterCallback(cb)
        controller = null
        val best = controllers.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING }
            ?: controllers.firstOrNull { !it.metadata?.getString(MediaMetadata.METADATA_KEY_TITLE).isNullOrBlank() }
            ?: controllers.firstOrNull { it.playbackState != null }
        if (best != null) {
            controller = best
            best.registerCallback(cb, handler)
            cb.onMetadataChanged(best.metadata)
            cb.onPlaybackStateChanged(best.playbackState)
        } else {
            vm.onConnectionChanged(false)
        }
    }

    private fun currentProgress(): Float {
        if (durationMs <= 0) return 0f
        val nowPos = basePosMs + ((SystemClock.elapsedRealtime() - baseElapsed) * speed).toLong()
        return (nowPos.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    }
}
