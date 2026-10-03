package com.xiaomi.watch5music

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xiaomi.watch5music.media.MediaNotifService
import com.xiaomi.watch5music.ui.GlassPill
import com.xiaomi.watch5music.ui.MusicPlayerScreen
import com.xiaomi.watch5music.ui.PlayerViewModel
import com.xiaomi.watch5music.ui.theme.InkDim
import com.xiaomi.watch5music.ui.theme.Watch5MusicTheme

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

/**
 * 打开时先检查「通知访问」授权：未授权则显示引导页（按钮直达系统通知访问设置）。
 * Android 不允许用运行时 API 直接申请该权限，只能跳系统设置由用户手动开启；
 * 从设置返回（ON_RESUME）后自动重新检测，授权通过即进入主界面。
 */
@Composable
private fun PermissionGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var granted by remember { mutableStateOf(MediaNotifService.isGranted(context)) }

    // 每次回到前台都重检一次（从设置页授权返回后立即生效）
    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                granted = MediaNotifService.isGranted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    if (!granted) {
        Box(
            Modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "需要「通知访问」权限",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "开启后手表才能同步手机正在播放的\n歌名 / 进度，否则显示「不可用」",
                    fontSize = 13.sp,
                    color = InkDim,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                GlassPill("去开启权限") {
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }
            }
        }
    } else {
        content()
    }
}

@Composable
private fun RoundScreenSurface() {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        val vm: PlayerViewModel = viewModel()
        PermissionGate { MusicPlayerScreen(vm) }
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
        val shortSide = if (maxWidth < maxHeight) maxWidth else maxHeight
        val faceDp = if (shortSide > 480.dp) 480.dp else shortSide
        Box(
            modifier = Modifier
                .size(faceDp)
                .clip(CircleShape)
                .background(Color.Black)
        ) {
            val vm: PlayerViewModel = viewModel()
            PermissionGate { MusicPlayerScreen(vm) }
        }
    }
}
