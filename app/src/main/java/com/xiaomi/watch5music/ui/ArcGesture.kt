package com.xiaomi.watch5music.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.PI
import kotlin.math.atan2

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
    // PointerInputScope.size 是 IntSize，转成几何 Size 供进度计算使用
    val sz = Size(size.width.toFloat(), size.height.toFloat())
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        // 无级调节：按下位置在下半屏（进度弧所在区域）即开始调节，
        // 拖动中每个事件都平滑映射进度并持续跟随手指，不做档位/跳变。
        val startSeek = enabled && down.position.y >= sz.height / 2f
        if (startSeek) {
            onSeek(down.position.arcProgress(sz))
            var last = down.position
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Main)
                val change = event.changes.firstOrNull { it.id == down.id }
                if (change == null || !change.pressed) break
                if (change.position != last) {
                    last = change.position
                    change.consume()
                    onSeek(change.position.arcProgress(sz))
                }
            }
        }
    }
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
