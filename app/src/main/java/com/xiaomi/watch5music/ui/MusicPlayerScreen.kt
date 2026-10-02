package com.xiaomi.watch5music.ui

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * 主界面：纯黑极简背景，三张横向滑动卡片（封面 / 歌词 / 控制）。
 * 所有数据来自蓝牙（[PlayerViewModel] 无任何预设），无蓝牙数据时显示「不可用」遮罩。
 */
@Composable
fun MusicPlayerScreen(vm: PlayerViewModel) {
    val state by vm.state.collectAsState()
    var volumeOpen by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(pageCount = { 3 })

    // 无蓝牙数据：未连接或无歌名
    val noData = !state.connected || state.title.isNullOrBlank()

    Box(Modifier.fillMaxSize().background(Color.Black)) {

        // ===== 主内容层：音量菜单展开时整体高模糊（Modifier.blur 30dp，API31+ 生效） =====
        Box(
            Modifier
                .fillMaxSize()
                .then(if (volumeOpen && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Modifier.blur(30.dp) else Modifier)
        ) {
            MediaHead(state.title ?: "未连接", state.artist ?: "—", Modifier.align(Alignment.TopCenter))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    // 手势优先判断：起点在下半屏则做无级进度调节，否则交给 pager 滑动
                    .arcSeekGesture(enabled = state.connected && !volumeOpen, onSeek = vm::seekTo)
                    .fillMaxSize()
                    .padding(top = 74.dp, bottom = 92.dp)
            ) { page ->
                // 卡片切换：随滑动偏移快速淡入淡出
                val offset = (page - pagerState.currentPage) + pagerState.currentPageOffsetFraction
                Box(
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = (1f - 0.55f * abs(offset)).coerceIn(0f, 1f) }
                ) {
                    when (page) {
                        0 -> CoverCard(state.title, state.artist, state.playing, vm::togglePlay)
                        1 -> LyricCard(state.currentLyric)
                        else -> ControlCard(
                            playing = state.playing,
                            volume = state.volume,
                            onPlayPause = vm::togglePlay,
                            onPrev = vm::prev,
                            onNext = vm::next,
                            onOpenVolume = { volumeOpen = true }
                        )
                    }
                }
            }

            // 下方半圆进度条（贴边，纯绘制）
            ArcOverlay(state.progress, Modifier.fillMaxSize())
            Dots(pagerState.currentPage, Modifier.align(Alignment.BottomCenter).padding(bottom = 26.dp))
        }

        // 当前秒数（拖拽圆弧时可见，极简小字）
        TimePill(state.progress, state.durationSec, Modifier.align(Alignment.BottomCenter).padding(bottom = 62.dp))

        // 无蓝牙数据 → 不可用遮罩
        if (noData) UnavailableOverlay(Modifier.fillMaxSize())

        // 音量二级菜单（高模糊背景弹层）
        if (volumeOpen) {
            VolumeOverlay(
                volume = state.volume,
                onVolumeChange = vm::setVolume,
                onClose = { volumeOpen = false },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
