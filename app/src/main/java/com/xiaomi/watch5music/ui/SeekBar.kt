package com.xiaomi.watch5music.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * 水平无级进度条（从左到右）。
 * 细条、无圆钮；底色灰白 [track]，已播放部分深灰 [fill]；拖动时配色不变。
 * 拖动全程实时预览，松手回调 [onSeek] 下发真实 seek。
 */
@Composable
fun SeekBar(
    progress: Float,
    enabled: Boolean,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var drag by remember { mutableStateOf<Float?>(null) }
    val shown = (drag ?: progress).coerceIn(0f, 1f)
    // 灰白黑配色：底 = 灰白，已播 = 深灰
    val track = if (enabled) Color(0xFFE8EAED) else Color(0xFFE8EAED).copy(alpha = 0.35f)
    val fill = if (enabled) Color(0xFF16181B) else Color(0xFF16181B).copy(alpha = 0.45f)

    Box(
        modifier
            .fillMaxWidth()
            .height(22.dp)
            .clip(RoundedCornerShape(11.dp))
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectHorizontalDragGestures(
                    onDragStart = { off -> drag = (off.x / size.width).coerceIn(0f, 1f) },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        drag = (change.position.x / size.width).coerceIn(0f, 1f)
                    },
                    onDragEnd = {
                        drag?.let(onSeek)
                        drag = null
                    },
                    onDragCancel = { drag = null }
                )
            }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val y = size.height / 2f
            val strokeW = 6.dp.toPx()
            drawLine(track, Offset(0f, y), Offset(size.width, y), strokeW, StrokeCap.Round)
            if (shown > 0f) {
                drawLine(fill, Offset(0f, y), Offset(size.width * shown, y), strokeW, StrokeCap.Round)
            }
        }
    }
}
