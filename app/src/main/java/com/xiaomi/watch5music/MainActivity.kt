package com.xiaomi.watch5music

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xiaomi.watch5music.ui.MusicPlayerScreen
import com.xiaomi.watch5music.ui.PlayerViewModel
import com.xiaomi.watch5music.ui.theme.Watch5MusicTheme
import kotlin.math.min

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Watch5MusicTheme {
                val isRound = LocalConfiguration.current.isScreenRound
                if (isRound) {
                    // 真圆形表盘（Watch 5 等）：直接全屏铺满
                    RoundScreenSurface()
                } else {
                    // 普通安卓设备：居中 480×480 圆形窗口模拟表盘，圆外纯黑
                    WatchSimulator()
                }
            }
        }
    }
}

@Composable
private fun RoundScreenSurface() {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        val vm: PlayerViewModel = viewModel()
        MusicPlayerScreen(vm)
    }
}

/**
 * 非圆形设备上的表盘模拟：以屏幕中心显示一个圆形窗口（最大 480dp），
 * 圆形区域为应用界面，圆外填充纯黑，模拟 Watch 5 的 480×480 圆形 AMOLED 屏。
 */
@Composable
private fun WatchSimulator() {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        val faceDp = min(maxWidth, maxHeight).coerceAtMost(480.dp)
        Box(
            modifier = Modifier
                .size(faceDp)
                .clip(CircleShape)
                .background(Color.Black)
        ) {
            val vm: PlayerViewModel = viewModel()
            MusicPlayerScreen(vm)
        }
    }
}
