package com.xiaomi.watch5music.ui

import android.graphics.Bitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiaomi.watch5music.ui.theme.InkDim

/**
 * 卡片 1 · 封面页（Apple Watch Music 风格）。
 * 全屏背景 = 专辑封面放大模糊（灰白黑基调），前景 = 居中清晰封面 + 歌名 / 歌手。
 * 长按封面 = 播放/暂停。
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
    Box(Modifier.fillMaxSize()) {
        // ===== 背景层：模糊封面 / 灰黑渐变兜底 =====
        if (art != null) {
            Image(
                bitmap = art.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 0.55f }
                    .then(blurIfSupported(42.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF2A2D31), Color(0xFF0A0B0D))
                        )
                    )
            )
        }
        // 底部压暗渐变（保证信息可读）
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.35f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // ===== 前景：居中封面 + 歌名/歌手 =====
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier
                    .size(150.dp)
                    .shadow(22.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF16181B))
                    .combinedClickable(onClick = {}, onLongClick = onLongPlayPause),
                contentAlignment = Alignment.Center
            ) {
                if (art != null) {
                    Image(
                        bitmap = art.asImageBitmap(),
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = if (playing) "❚❚" else "▶",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = title ?: "—",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 36.dp)
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = artist ?: "",
                fontSize = 13.sp,
                color = InkDim,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
