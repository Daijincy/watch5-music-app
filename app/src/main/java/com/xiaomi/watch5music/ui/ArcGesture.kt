package com.xiaomi.watch5music.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.awaitEachGesture
import androidx.compose.ui.input.pointer.awaitFirstDown
import androidx.compose.ui.input.pointer.awaitPointerEvent
import androidx.compose.ui.input.pointer.consume
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.min

/**
 * 半圆弧进度拖拽手势。
 * 挂在 HorizontalPager 的最外层：
 *  - 手势起点落在“下方半圆进度带”内 → 消费移动事件，转为进度 seek；
 *  - 否则不消费任何事件，由 pager 正常完成卡片滑动。
 */
fun Modifier.arcSeekGesture(
    enabled: Boolean,
    onSeek: (Float) -> Unit
): Modifier = pointerInput(enabled) {
    val bandPx = 46.dp.toPx()
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val inBand = enabled && down.position.isInArcBand(size, bandPx)
        if (inBand) {
            onSeek(down.position.arcProgress(size))
            var last = down.position
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Main)
                val change = event.changes.firstOrNull { it.id == down.id }
                if (change == null || !change.pressed) break
                if (change.position != last) {
                    last = change.position
                    change.consume()
                    onSeek(change.position.arcProgress(size))
                }
            }
        }
    }
}

/** 是否落在下方半圆弧进度带内（距圆心 40% 屏宽 ± bandPx，且在下半屏） */
private fun Offset.isInArcBand(size: Size, bandPx: Float): Boolean {
    val cx = size.width / 2f
    val cy = size.height / 2f
    if (y < cy) return false
    val r = min(size.width, size.height) * 0.40f
    val dist = (this - Offset(cx, cy)).getDistance()
    return dist in (r - bandPx)..(r + bandPx)
}

/** 屏幕坐标 → 半圆进度 0..1（0=右侧，经底部到左侧=1） */
private fun Offset.arcProgress(size: Size): Float {
    val cx = size.width / 2f
    val cy = size.height / 2f
    var deg = atan2(y - cy, x - cx)
    if (deg < 0) deg += 2f * PI.toFloat()
    deg = deg.coerceIn(0f, PI.toFloat())
    return deg / PI.toFloat()
}
