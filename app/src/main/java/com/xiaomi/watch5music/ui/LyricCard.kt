package com.xiaomi.watch5music.ui

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiaomi.watch5music.ui.model.Song
import com.xiaomi.watch5music.ui.model.currentLineIndex
import com.xiaomi.watch5music.ui.theme.InkDim
import kotlinx.coroutines.delay

/**
 * 卡片 2 · 歌词页。
 * 蓝牙逐句推送：只显示当前句（大字号居中），上下句弱显示；
 * 句切换时带 模糊 + 上下跳动 动画（AnimatedContent + RenderEffect 模糊）。
 */
@Composable
fun LyricCard(song: Song, progress: Float, modifier: Modifier = Modifier) {
    val ci = currentLineIndex(song, progress)
    val prev = song.lyrics.getOrNull(ci - 1)?.text ?: ""
    val cur = song.lyrics[ci].text
    val next = song.lyrics.getOrNull(ci + 1)?.text ?: ""

    // 句切换时短暂触发模糊
    var showBlur by remember { mutableStateOf(false) }
    LaunchedEffect(cur) {
        showBlur = true
        delay(340)
        showBlur = false
    }
    val blur by animateFloatAsState(
        targetValue = if (showBlur) 20f else 0f,
        animationSpec = tween(320),
        label = "lyricBlur"
    )

    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = prev,
            fontSize = 15.sp,
            color = InkDim,
            maxLines = 2,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer { alpha = 0.55f }
        )
        Spacer(Modifier.height(8.dp))

        AnimatedContent(
            targetState = cur,
            transitionSpec = {
                (slideInVertically(initialOffsetY = { it / 3 }) + fadeIn(tween(320))) togetherWith
                    (slideOutVertically(targetOffsetY = { -it / 3 }) + fadeOut(tween(200)))
            },
            label = "lyric"
        ) { line ->
            Text(
                text = line,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        renderEffect = RenderEffect.createBlurEffect(blur, blur, Shader.TileMode.CLAMP)
                    }
                }
            )
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = next,
            fontSize = 15.sp,
            color = InkDim,
            maxLines = 2,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer { alpha = 0.55f }
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = if (next.isNotEmpty()) "下句 ${ci + 2} / ${song.lyrics.size}" else "—— 已到本曲最后一句 ——",
            fontSize = 10.sp,
            color = InkDim
        )
    }
}
