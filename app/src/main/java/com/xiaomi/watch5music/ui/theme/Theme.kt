package com.xiaomi.watch5music.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ===== 纯黑极简配色（灰白黑中性风：黑背景 + 白内容 + 灰强调） =====
val AccentBlue = Color(0xFFF2F3F5)   // 亮白灰（渐变/滑块高光端）
val AccentIndigo = Color(0xFF9AA0A6) // 中灰（渐变/强调末端）
val InkWhite = Color(0xFFF2F3F5)
val InkDim = Color(0xFF9AA0A6)
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
