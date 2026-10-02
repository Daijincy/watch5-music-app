package com.xiaomi.watch5music.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiaomi.watch5music.ui.model.Song
import com.xiaomi.watch5music.ui.theme.AccentBlue

/**
 * 卡片 1 · 封面页。
 * 无任何播放控制按钮；长按封面 = 快速播放/暂停（可选手势）。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CoverCard(song: Song, playing: Boolean, onLongPlayPause: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(150.dp)
                .shadow(14.dp, RoundedCornerShape(20.dp), spotColor = AccentBlue.copy(alpha = 0.35f))
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(Color(song.coverStart), Color(song.coverEnd))))
                .combinedClickable(onClick = {}, onLongClick = onLongPlayPause),
            contentAlignment = Alignment.Center
        ) {
            if (!playing) {
                Text("▶", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.92f))
            }
        }
    }
}
