package com.xiaomi.watch5music.ui

import android.graphics.Bitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiaomi.watch5music.ui.theme.AccentBlue
import com.xiaomi.watch5music.ui.theme.InkDim

/**
 * 卡片 1 · 封面页。
 * 有同步到的专辑封面则显示封面；无封面时兜底为灰黑渐变 + 歌名 / 歌手。
 * 无任何播放控制按钮；长按封面 = 快速播放/暂停（可选手势）。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CoverCard(
    title: String?,
    artist: String?,
    art: Bitmap?,
    playing: Boolean,
    onLongPlayPause: () -> Unit
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val fallback =
            Brush.linearGradient(
                listOf(Color(0xFF4A4D52), Color(0xFF16181B)),
                start = androidx.compose.ui.geometry.Offset.Zero,
                end = androidx.compose.ui.geometry.Offset.Infinite
            )
        Box(
            Modifier
                .size(150.dp)
                .shadow(14.dp, RoundedCornerShape(20.dp), spotColor = AccentBlue.copy(alpha = 0.20f))
                .clip(RoundedCornerShape(20.dp))
                .background(fallback)
                .combinedClickable(onClick = {}, onLongClick = onLongPlayPause),
            contentAlignment = Alignment.Center
        ) {
            if (art != null) {
                // 真实专辑封面
                Image(
                    bitmap = art.asImageBitmap(),
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = title ?: "—",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(text = artist ?: "", fontSize = 12.sp, color = InkDim)
                    if (!playing) {
                        Spacer(Modifier.height(10.dp))
                        Text("▶", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.9f))
                    }
                }
            }
        }
    }
}
