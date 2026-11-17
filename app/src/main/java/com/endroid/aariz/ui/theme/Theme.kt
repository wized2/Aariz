package com.endroid.aariz.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF3D6B45),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD4EBD8),
    onPrimaryContainer = Color(0xFF0F2414),
    secondary = Color(0xFF5C5346),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFF4A6350),
    tertiaryContainer = Color(0xFFCCE8D1),
    background = Color(0xFFFBF8F0),
    onBackground = Color(0xFF1A1914),
    surface = Color(0xFFFBF8F0),
    onSurface = Color(0xFF1A1914),
    surfaceVariant = Color(0xFFE8E4D6),
    onSurfaceVariant = Color(0xFF4A453C),
    outline = Color(0xFF7B756A),
    error = Color(0xFFBA1A1A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA8D0B0),
    onPrimary = Color(0xFF10321A),
    primaryContainer = Color(0xFF2A4A32),
    onPrimaryContainer = Color(0xFFD4EBD8),
    secondary = Color(0xFFCDC3B4),
    onSecondary = Color(0xFF342F25),
    tertiary = Color(0xFFB0CCB7),
    tertiaryContainer = Color(0xFF334B3A),
    background = Color(0xFF0E0E0C),
    onBackground = Color(0xFFE6E2D8),
    surface = Color(0xFF141412),
    onSurface = Color(0xFFE6E2D8),
    surfaceVariant = Color(0xFF2A2823),
    onSurfaceVariant = Color(0xFFCBC5B8),
    outline = Color(0xFF958F84),
    error = Color(0xFFFFB4AB),
)

@Composable
fun AarizTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
