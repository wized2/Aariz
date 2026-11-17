package com.endroid.aariz.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Claude-inspired warm stone palette
private val ClaudeAccent = Color(0xFFDA7756) // soft terracotta
private val ClaudeAccentDark = Color(0xFFE8A48A)

private val LightColors = lightColorScheme(
    primary = ClaudeAccent,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF5E6DF),
    onPrimaryContainer = Color(0xFF3D2419),
    secondary = Color(0xFF6B6560),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFEFECE6),
    onSecondaryContainer = Color(0xFF2C2926),
    tertiary = Color(0xFF8B7355),
    background = Color(0xFFFAF9F5), // Claude warm paper
    onBackground = Color(0xFF2C2A26),
    surface = Color(0xFFFAF9F5),
    onSurface = Color(0xFF2C2A26),
    surfaceVariant = Color(0xFFF0EEE8),
    onSurfaceVariant = Color(0xFF5C574F),
    outline = Color(0xFFC4C0B6),
    outlineVariant = Color(0xFFE5E2D9),
    error = Color(0xFFC44B3F),
)

private val DarkColors = darkColorScheme(
    primary = ClaudeAccentDark,
    onPrimary = Color(0xFF2A1810),
    primaryContainer = Color(0xFF4A3228),
    onPrimaryContainer = Color(0xFFF5E6DF),
    secondary = Color(0xFFB0AAA3),
    onSecondary = Color(0xFF1F1E1C),
    secondaryContainer = Color(0xFF3A3834),
    onSecondaryContainer = Color(0xFFE8E4DC),
    tertiary = Color(0xFFC4B09A),
    background = Color(0xFF1F1E1C), // warm charcoal
    onBackground = Color(0xFFE8E4DC),
    surface = Color(0xFF262522),
    onSurface = Color(0xFFE8E4DC),
    surfaceVariant = Color(0xFF33312E),
    onSurfaceVariant = Color(0xFFB8B2A8),
    outline = Color(0xFF5C574F),
    outlineVariant = Color(0xFF3F3C38),
    error = Color(0xFFE88A80),
)

private val AarizTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.3).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.15.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
    ),
)

@Composable
fun AarizTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AarizTypography,
        content = content,
    )
}
