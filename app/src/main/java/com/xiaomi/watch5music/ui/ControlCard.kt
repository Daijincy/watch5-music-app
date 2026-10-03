package com.xiaomi.watch5music.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 卡片 2 · 控制页。
 * 居中三键：上一首 / 播放·暂停 / 下一首；命令经 MediaSync 下发到手机媒体会话。
 */
@Composable
fun ControlCard(
    playing: Boolean,
    onPlayPause: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassButton("‹", size = 52.dp, onClick = onPrev)
            GlassButton(
                if (playing) "❚❚" else "▶",
                size = 74.dp,
                circle = true,
                onClick = onPlayPause
            )
            GlassButton("›", size = 52.dp, onClick = onNext)
        }
    }
}
