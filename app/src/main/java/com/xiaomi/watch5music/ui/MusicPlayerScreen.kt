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
 * 主界面（Apple Watch Music 风格）：
 * 纯黑极简背景，两张横向滑动卡片（全屏封面 / 控制页），
 * 底部毛玻璃控制条（时间 + 水平无级进度条 + 三键）两页共用。
 * 数据来自【本机信源 + 蓝牙 NLS】双通道（[PlayerViewModel] 无任何预设），
 * 无数据时显示「不可用」遮罩。
 */
@Composable
fun MusicPlayerScreen(vm: PlayerViewModel) {
    val state by vm.state.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 2 })

    // 无蓝牙数据：未连接或无歌名
    val noData = !state.connected || state.title.isNullOrBlank()

    Box(Modifier.fillMaxSize().background(Color.Black)) {

        // ===== 页面区（底部预留控制条高度） =====
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(bottom = 156.dp)
        ) { page ->
            val offset = (page - pagerState.currentPage) + pagerState.currentPageOffsetFraction
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = (1f - 0.45f * abs(offset)).coerceIn(0f, 1f) }
            ) {
                when (page) {
                    0 -> CoverCard(state.title, state.artist, state.art, state.playing, vm::togglePlay)
                    else -> ControlCard(
                        title = state.title ?: "—",
                        artist = state.artist ?: "",
                        playing = state.playing,
                        onPlayPause = vm::togglePlay,
                        onPrev = vm::prev,
                        onNext = vm::next
                    )
                }
            }
        }

        // ===== 底部毛玻璃控制条（Apple Watch Music 风格） =====
        PlaybackBar(
            playing = state.playing,
            progress = state.progress,
            durationSec = state.durationSec,
            enabled = !noData,
            onToggle = vm::togglePlay,
            onPrev = vm::prev,
            onNext = vm::next,
            onSeek = vm::seekTo,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 26.dp, end = 26.dp, bottom = 22.dp)
        )

        // 无蓝牙数据 → 不可用遮罩
        if (noData) UnavailableOverlay(Modifier.fillMaxSize())
    }
}
