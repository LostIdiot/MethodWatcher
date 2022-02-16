package cn.zz.xposed.methodwatcher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Color(0xFF17223b),
    primaryVariant = Color(0xFF263859),
    secondary = Color(0xFF6B778d),
    secondaryVariant = Color(0xFFFF6768)
)

private val LightColorPalette = lightColors(
    primary = Color(0xFF03A9F4),
    primaryVariant = Color(0xFF0288D1),
    secondary = Color(0xFFFFEB3B),
    secondaryVariant = Color(0xFFFFA000)
)

@Composable
fun SystemCallMonitorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}