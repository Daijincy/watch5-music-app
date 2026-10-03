package com.xiaomi.watch5music.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiaomi.watch5music.ui.theme.InkDim

/**
 * Apple Watch Music 风格底部控制条（毛玻璃）：
 * 时间行（当前 / 总长）→ 水平无级进度条 → 上一首 / 播放·暂停 / 下一首。
 * 高模糊背景 + 半透明黑，圆角胶囊。
 */
@Composable
fun PlaybackBar(
    playing: Boolean,
    progress: Float,
    durationSec: Int,
    enabled: Boolean,
    onToggle: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .clip(RoundedCornerShape(30.dp))
            .background(Color.Black.copy(alpha = 0.45f))
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(30.dp))
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 时间行
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                formatTime((progress * durationSec).toInt()),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.75f)
            )
            Text(
                formatTime(durationSec),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = InkDim
            )
        }
        Spacer(Modifier.height(4.dp))
        SeekBar(
            progress = progress,
            enabled = enabled,
            onSeek = onSeek,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        )
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(26.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassButton("‹", size = 42.dp, onClick = onPrev)
            GlassButton(
                if (playing) "❚❚" else "▶",
                size = 56.dp,
                circle = true,
                onClick = onToggle
            )
            GlassButton("›", size = 42.dp, onClick = onNext)
        }
    }
}
