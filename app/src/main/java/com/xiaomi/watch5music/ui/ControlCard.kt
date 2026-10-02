package com.xiaomi.watch5music.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 卡片 3 · 控制页（精简版）。
 * 仅：上一首 / 播放暂停 / 下一首 + 音量入口（二级菜单）。
 */
@Composable
fun ControlCard(
    playing: Boolean,
    volume: Int,
    onPlayPause: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onOpenVolume: () -> Unit
) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassButton("‹‹", size = 54.dp, onClick = onPrev)
            GlassButton(
                if (playing) "❚❚" else "▶",
                size = 72.dp,
                circle = true,
                onClick = onPlayPause
            )
            GlassButton("››", size = 54.dp, onClick = onNext)
        }
        Spacer(Modifier.height(28.dp))
        GlassPill("音量  $volume", onClick = onOpenVolume)
    }
}
