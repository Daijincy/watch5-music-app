package com.xiaomi.watch5music.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiaomi.watch5music.ui.theme.AccentBlue
import com.xiaomi.watch5music.ui.theme.AccentIndigo
import kotlin.math.roundToInt

/**
 * 音量二级菜单（无边框卡片）：
 * 主内容层已在 MusicPlayerScreen 中被整屏高模糊（RenderEffect blur 30px），
 * 这里只叠加一层压暗遮罩 + 悬浮的音量滑块；点击遮罩空白处关闭，菜单内点击不关闭。
 */
@Composable
fun VolumeOverlay(
    volume: Int,
    onVolumeChange: (Int) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier.fillMaxSize()) {
        // 压暗遮罩（点击关闭）
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.42f))
                .pointerInput(Unit) { detectTapGestures { onClose() } }
        )
        // 悬浮菜单内容（无边框）
        Column(
            Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "音量",
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.92f),
                letterSpacing = 4.sp
            )
            VerticalVolumeSlider(volume, onVolumeChange, Modifier.height(200.dp))
            Text("$volume%", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

/** 竖向音量滑块（白色圆钮 + 蓝色渐变填充） */
@Composable
fun VerticalVolumeSlider(volume: Int, onVolumeChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier.pointerInput(Unit) {
            fun apply(y: Float) {
                val h = size.height.toFloat()
                val v = ((h - y) / h * 100).roundToInt().coerceIn(0, 100)
                onVolumeChange(v)
            }
            detectDragGestures(
                onDragStart = { apply(it.position.y) },
                onDrag = { change, _ ->
                    apply(change.position.y)
                    change.consume()
                }
            )
        }
    ) {
        val trackH = maxHeight
        val fillH = trackH * volume / 100f
        Box(
            Modifier
                .width(6.dp)
                .height(trackH)
                .background(Color.White.copy(alpha = 0.12f), CircleShape)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(fillH)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(listOf(AccentBlue, AccentIndigo)),
                        CircleShape
                    )
            )
            Box(
                Modifier
                    .size(22.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = 11.dp - fillH)
                    .shadow(6.dp, CircleShape)
                    .background(Color.White, CircleShape)
            )
        }
    }
}
