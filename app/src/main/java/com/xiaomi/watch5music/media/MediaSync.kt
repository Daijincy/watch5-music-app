package com.xiaomi.watch5music.media

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 手机媒体信息共享源（普通 App 可用，无需 root）。
 *
 * 数据来源：Wear OS 手表同步手机的通知 → [MediaNotifService] 读到媒体通知，
 * 解析出 歌名/歌手/时长/进度/播放状态，写入本单例。
 * [com.xiaomi.watch5music.ui.PlayerViewModel] 收集本单例驱动界面。
 */
object MediaSync {

    data class Media(
        val title: String? = null,
        val artist: String? = null,
        val durationSec: Int = 0,
        val progress: Float = 0f,
        val playing: Boolean = false,
        val connected: Boolean = false
    )

    private val _state = MutableStateFlow(Media())
    val state: StateFlow<Media> = _state.asStateFlow()

    fun onMeta(title: String, artist: String, durationSec: Int) {
        _state.value = _state.value.copy(
            title = title, artist = artist, durationSec = durationSec, connected = true
        )
    }

    fun onProgress(p: Float) {
        _state.value = _state.value.copy(progress = p.coerceIn(0f, 1f))
    }

    fun onPlaying(playing: Boolean) {
        _state.value = _state.value.copy(playing = playing)
    }

    /** 通知/会话消失或断开 → 回到无数据（不可用）态 */
    fun onConnectionChanged(connected: Boolean) {
        _state.value = if (connected) _state.value.copy(connected = true) else Media()
    }
}
