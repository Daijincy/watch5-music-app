package com.xiaomi.watch5music.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xiaomi.watch5music.ui.model.SampleSongs
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 播放状态管理。
 * 真实实现中：播放进度 / 歌曲信息由手机端 MediaSession 经蓝牙推送，
 * 本工程为 UI 原型，用定时器模拟进度，蓝牙为演示态（[setConnected]）。
 */
class PlayerViewModel : ViewModel() {

    data class UiState(
        val songIndex: Int = 0,
        val progress: Float = 0.28f,      // 0..1
        val playing: Boolean = true,
        val connected: Boolean = true,    // 蓝牙链路
        val volume: Int = 80
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var ticker: Job? = null

    init {
        ticker = viewModelScope.launch {
            while (isActive) {
                val s = _state.value
                if (s.playing && s.connected) {
                    val song = SampleSongs.all[s.songIndex]
                    val np = s.progress + 0.25f / song.durationSec
                    if (np >= 1f) {
                        _state.update {
                            it.copy(
                                songIndex = (it.songIndex + 1) % SampleSongs.all.size,
                                progress = 0f
                            )
                        }
                    } else {
                        _state.update { it.copy(progress = np) }
                    }
                }
                delay(250)
            }
        }
    }

    val currentSong get() = SampleSongs.all[_state.value.songIndex]

    fun togglePlay() = _state.update { it.copy(playing = !it.playing) }

    fun next() = _state.update {
        it.copy(songIndex = (it.songIndex + 1) % SampleSongs.all.size, progress = 0f)
    }

    fun prev() = _state.update {
        it.copy(songIndex = (it.songIndex - 1 + SampleSongs.all.size) % SampleSongs.all.size, progress = 0f)
    }

    fun seekTo(p: Float) = _state.update { it.copy(progress = p.coerceIn(0f, 1f)) }

    fun setVolume(v: Int) = _state.update { it.copy(volume = v.coerceIn(0, 100)) }

    fun setConnected(c: Boolean) = _state.update { it.copy(connected = c) }

    override fun onCleared() {
        ticker?.cancel()
        super.onCleared()
    }
}
