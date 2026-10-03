package com.xiaomi.watch5music.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * 主界面：纯黑极简背景，两张横向滑动卡片（封面 / 控制）。
 * 底部为水平无级进度条（从左到右，灰白底 / 深灰已播，无圆钮），拖动真实 seek。
 * 所有数据来自蓝牙（[PlayerViewModel] 无任何预设），无蓝牙数据时显示「不可用」遮罩。
 */
@Composable
fun MusicPlayerScreen(vm: PlayerViewModel) {
    val state by vm.state.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 2 })

    // 无蓝牙数据：未连接或无歌名
    val noData = !state.connected || state.title.isNullOrBlank()

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        MediaHead(state.title ?: "未连接", state.artist ?: "—", Modifier.align(Alignment.TopCenter))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(top = 74.dp, bottom = 76.dp)
        ) { page ->
            // 卡片切换：随滑动偏移快速淡入淡出
            val offset = (page - pagerState.currentPage) + pagerState.currentPageOffsetFraction
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = (1f - 0.55f * abs(offset)).coerceIn(0f, 1f) }
            ) {
                when (page) {
                    0 -> CoverCard(state.title, state.artist, state.art, state.playing, vm::togglePlay)
                    else -> ControlCard(
                        playing = state.playing,
                        onPlayPause = vm::togglePlay,
                        onPrev = vm::prev,
                        onNext = vm::next
                    )
                }
            }
        }

        // 底部水平无级进度条
        SeekBar(
            progress = state.progress,
            enabled = !noData,
            onSeek = vm::seekTo,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 52.dp, bottom = 38.dp)
        )

        Dots(pagerState.currentPage, Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp))

        // 无蓝牙数据 → 不可用遮罩
        if (noData) UnavailableOverlay(Modifier.fillMaxSize())
    }
}
