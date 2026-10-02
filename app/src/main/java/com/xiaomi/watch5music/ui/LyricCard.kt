package com.xiaomi.watch5music.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiaomi.watch5music.ui.theme.InkDim

/**
 * 卡片 2 · 歌词页。
 * 只显示蓝牙逐句推送的【当前】一句（大字号居中）：
 * 手机端在换句时推送当前句文本，手表端原样展示这一句，不做上下句联想。
 */
@Composable
fun LyricCard(lyric: String?, modifier: Modifier = Modifier) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val line = lyric?.takeIf { it.isNotBlank() }
        if (line == null) {
            Text(
                text = "等待歌词推送…",
                fontSize = 15.sp,
                color = InkDim,
                textAlign = TextAlign.Center
            )
        } else {
            AnimatedContent(
                targetState = line,
                transitionSpec = { (fadeIn(tween(280)) + androidx.compose.animation.slideInVertically(initialOffsetY = { it / 4 })) togetherWith fadeOut(tween(160)) },
                label = "lyric"
            ) { current ->
                Text(
                    text = current,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 28.dp)
                )
            }
        }
    }
}
