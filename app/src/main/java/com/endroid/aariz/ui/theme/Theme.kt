package com.endroid.aariz.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Warm cream / forest green — aligned with Aariz web
private val LightColors = lightColorScheme(
    primary = Color(0xFF4F6F52),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD8E8D8),
    onPrimaryContainer = Color(0xFF1A2E1C),
    secondary = Color(0xFF6B5E4E),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFF5A6B5A),
    background = Color(0xFFFEFAE0),
    onBackground = Color(0xFF1C1A12),
    surface = Color(0xFFFEFAE0),
    onSurface = Color(0xFF1C1A12),
    surfaceVariant = Color(0xFFEFEBD1),
    onSurfaceVariant = Color(0xFF4D473D),
    outline = Color(0xFF7D776C),
    error = Color(0xFFBA1A1A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA8C5AA),
    onPrimary = Color(0xFF1A2E1C),
    primaryContainer = Color(0xFF3A523C),
    onPrimaryContainer = Color(0xFFD8E8D8),
    secondary = Color(0xFFD0C4B0),
    onSecondary = Color(0xFF382F22),
    tertiary = Color(0xFFB5C4B5),
    background = Color(0xFF12110E),
    onBackground = Color(0xFFE8E4DA),
    surface = Color(0xFF1A1915),
    onSurface = Color(0xFFE8E4DA),
    surfaceVariant = Color(0xFF2A2822),
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
