package com.xiaomi.watch5music.ui

import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiaomi.watch5music.ui.theme.AccentBlue
import com.xiaomi.watch5music.ui.theme.InkDim

// ===== 模糊工具：API31+ 真模糊，低版本降级为无模糊（安全） =====
@Composable
fun blurIfSupported(dp: Dp): Modifier =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Modifier.blur(dp) else Modifier

// ===== 时间格式化（m:ss） =====
fun formatTime(sec: Int): String {
    val s = sec.coerceAtLeast(0)
    return String.format("%d:%02d", s / 60, s % 60)
}

// ===== 顶部媒体信息（三卡共用） =====
@Composable
fun MediaHead(title: String, artist: String, modifier: Modifier = Modifier) {
    Column(modifier.padding(top = 26.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 40.dp)
        )
        Spacer(Modifier.height(3.dp))
        Text(text = artist, fontSize = 13.sp, color = InkDim)
    }
}

// ===== 底部页点 =====
@Composable
fun Dots(current: Int, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)) {
        repeat(2) { i ->
            val w = if (i == current) 18.dp else 6.dp
            Box(
                Modifier
                    .size(width = w, height = 6.dp)
                    .clip(CircleShape)
                    .background(if (i == current) AccentBlue else Color.White.copy(alpha = 0.25f))
            )
        }
    }
}

// ===== 当前播放时间（极简小字，拖拽时同样可见） =====
@Composable
fun TimePill(progress: Float, durationSec: Int, modifier: Modifier = Modifier) {
    val totalSec = (progress * durationSec).toInt()
    val text = String.format("%d:%02d", totalSec / 60, totalSec % 60)
    Box(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.10f))
            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDFF3FF))
    }
}

// ===== 无蓝牙数据遮罩 =====
@Composable
fun UnavailableOverlay(modifier: Modifier = Modifier) {
    Box(modifier.background(Color.Black.copy(alpha = 0.72f)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("不可用", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(6.dp))
            Text("未收到蓝牙媒体数据\n请连接手机播放", fontSize = 13.sp, color = InkDim, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

// ===== 毛玻璃按钮（按压缩放反馈，Apple 风格） =====
@Composable
fun GlassButton(label: String, size: Dp, circle: Boolean = false, onClick: () -> Unit) {
    val shape = if (circle) CircleShape else RoundedCornerShape(16.dp)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.88f else 1f)
    Box(
        Modifier
            .size(size)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape)
            .background(Color.White.copy(alpha = 0.10f))
            .border(1.dp, Color.White.copy(alpha = 0.20f), shape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = if (circle) 28.sp else 22.sp, color = Color.White)
    }
}

// ===== 毛玻璃胶囊（音量入口） =====
@Composable
fun GlassPill(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier
            .clip(shape)
            .background(Color.White.copy(alpha = 0.10f))
            .border(1.dp, Color.White.copy(alpha = 0.20f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.White)
    }
}
