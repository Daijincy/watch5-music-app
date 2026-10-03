package com.xiaomi.watch5music.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xiaomi.watch5music.media.MediaSync
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 播放状态管理（通知监听驱动，无任何预设/模拟数据）。
 *
 * 初始化即收集 [MediaSync]：手机放歌 → 手机媒体通知经蓝牙同步到手表 →
 * [com.xiaomi.watch5music.media.MediaNotifService] 解析后写入 MediaSync。
 * 未读到任何媒体数据时 [UiState.connected]=false，界面显示「不可用」。
 *
 * 控制指令（播放/暂停/切歌/seek/音量）由界面调用，经 [sendCommand] 下发（待接 MediaController）。
 */
class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    data class UiState(
        val title: String? = null,          // 手机播放的歌名，无数据时为 null
        val artist: String? = null,         // 歌手
        val durationSec: Int = 0,           // 总时长（秒）
        val progress: Float = 0f,           // 播放进度 0..1
        val playing: Boolean = false,       // 播放/暂停
        val connected: Boolean = false,     // 是否已收到媒体数据
        val volume: Int = 80,               // 音量 0..100
        val art: Bitmap? = null,            // 专辑封面（同步自手机媒体元数据）
        val currentLyric: String? = null    // 逐句歌词（通知通道不携带，保持等待推送）
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            MediaSync.state.collect { m ->
                _state.update {
                    it.copy(
                        title = m.title,
                        artist = m.artist,
                        durationSec = m.durationSec,
                        progress = m.progress,
                        playing = m.playing,
                        connected = m.connected,
                        art = m.art
                    )
                }
            }
        }
    }

    // ===== 用户操作 → 下发命令 =====

    fun togglePlay() = sendCommand("toggle")

    fun next() = sendCommand("next")

    fun prev() = sendCommand("prev")

    fun seekTo(p: Float) {
        _state.update { it.copy(progress = p.coerceIn(0f, 1f)) }
        sendCommand("seek:${p.coerceIn(0f, 1f)}")
    }

    fun setVolume(v: Int) {
        _state.update { it.copy(volume = v.coerceIn(0, 100)) }
        sendCommand("volume:${v.coerceIn(0, 100)}")
    }

    /** 命令下发占位：待通过 MediaController transportControls 下发到手机播放会话。 */
    private fun sendCommand(cmd: String) {
        viewModelScope.launch { /* TODO: MediaController transportControls */ }
    }
}
