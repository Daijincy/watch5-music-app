package com.xiaomi.watch5music.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 播放状态管理（蓝牙数据驱动，无任何预设/模拟数据）。
 *
 * 所有媒体信息（歌名 / 歌手 / 时长 / 进度 / 逐句歌词 / 播放状态）由手机端
 * MediaSession 经蓝牙 BLE 推送调用下面的 onXxx 入口写入；本类不生成数据。
 * 未收到任何蓝牙数据时 [UiState.connected]=false，界面显示「不可用」。
 *
 * 控制指令（播放/暂停/切歌/seek/音量）由界面调用，经 [sendCommand] 下发给手机。
 */
class PlayerViewModel : ViewModel() {

    data class UiState(
        val title: String? = null,          // 蓝牙推送的歌名，无数据时为 null
        val artist: String? = null,         // 歌手
        val durationSec: Int = 0,           // 总时长（秒）
        val progress: Float = 0f,           // 播放进度 0..1
        val playing: Boolean = false,       // 播放/暂停
        val connected: Boolean = false,     // 是否已收到蓝牙媒体数据
        val volume: Int = 80,               // 音量 0..100
        val currentLyric: String? = null    // 蓝牙逐句推送的当前句（只显示这一句）
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    // ===== 蓝牙数据入口（手机端 MediaSession → BLE 特征值 → 回调调用） =====

    /** 元信息：歌名 / 歌手 / 时长。首次收到即视为已连接（有数据）。 */
    fun onMeta(title: String, artist: String, durationSec: Int) {
        _state.update {
            it.copy(title = title, artist = artist, durationSec = durationSec, connected = true)
        }
    }

    /** 播放进度 0..1（手机端持续推送） */
    fun onProgress(p: Float) {
        _state.update { it.copy(progress = p.coerceIn(0f, 1f)) }
    }

    /** 逐句歌词：手机端按时间轴在换句时推送当前句文本，这里只保存当前句。 */
    fun onLyric(text: String) {
        _state.update { it.copy(currentLyric = text) }
    }

    /** 播放/暂停状态 */
    fun onPlaying(p: Boolean) {
        _state.update { it.copy(playing = p) }
    }

    /** 蓝牙链路断开 / 恢复。断开即无数据 → 不可用。 */
    fun onConnectionChanged(connected: Boolean) {
        if (!connected) {
            _state.update { UiState(volume = it.volume) }
        } else {
            _state.update { it.copy(connected = true) }
        }
    }

    // ===== 用户操作 → 下发命令（经 [sendCommand] 返回手机） =====

    fun togglePlay() {
        _state.update { it.copy(playing = !it.playing) }
        sendCommand("toggle")
    }

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

    /** 命令下发占位：真实实现写入 BLE 命令特征值 [com.xiaomi.watch5music.media.MediaSessionBridge.CHAR_COMMAND]。 */
    private fun sendCommand(cmd: String) {
        viewModelScope.launch { /* TODO: write to BLE CHAR_COMMAND */ }
    }
}
