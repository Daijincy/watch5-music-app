package com.xiaomi.watch5music.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.xiaomi.watch5music.ui.theme.AccentBlue
import com.xiaomi.watch5music.ui.theme.AccentIndigo
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * 屏幕下方贴边半圆弧进度条（纯绘制，不拦截事件；手势见 [arcSeekGesture]）。
 * 圆弧半径 = 屏宽/高的 40%，紧贴圆形屏幕内边缘。
 */
@Composable
fun ArcOverlay(progress: Float, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r = min(size.width, size.height) * 0.40f
            val arcTopLeft = Offset(cx - r, cy - r)
            val arcSize = Size(r * 2f, r * 2f)

            // 底环（下方半圈，0°=右侧，正角度=顺时针即经过底部）
            drawArc(
                color = Color.White.copy(alpha = 0.10f),
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )
            // 进度环（液态发光蓝）
            drawArc(
                brush = Brush.linearGradient(listOf(AccentBlue, AccentIndigo)),
                startAngle = 0f,
                sweepAngle = 180f * progress,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round)
            )
            // 可拖拽滑块
            val ang = PI.toFloat() * progress
            val kx = cx + r * cos(ang)
            val ky = cy + r * sin(ang)
            drawCircle(Color.White, radius = 13.dp.toPx(), center = Offset(kx, ky))
            drawCircle(AccentBlue, radius = 6.5.dp.toPx(), center = Offset(kx, ky))
        }
    }
}
