package com.xiaomi.watch5music.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ===== 纯黑极简配色（苹果风：克制蓝 + 细腻白玻璃） =====
val AccentBlue = Color(0xFF4AA3FF)
val AccentIndigo = Color(0xFF9AA8FF)
val InkWhite = Color(0xFFF4F7FF)
val InkDim = Color(0xFF8A93AC)
val GlassWhite = Color(0x1AFFFFFF)   // 白色 10%
val GlassBorder = Color(0x33FFFFFF)  // 白色 20%

private val DarkColors = darkColorScheme(
    primary = AccentBlue,
    secondary = AccentIndigo,
    background = Color.Black,
    surface = Color.Black,
    onBackground = InkWhite,
    onSurface = InkWhite,
    onSurfaceVariant = InkDim
)

@Composable
fun Watch5MusicTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}
