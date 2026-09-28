package com.aniruddha.dreamsave.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.aniruddha.dreamsave.data.ThemeMode

val ElectricBlue = Color(0xFF2E8BFF)
val Cyan = Color(0xFF00D4FF)
val Violet = Color(0xFF8B5CF6)
val Magenta = Color(0xFFC04DFF)
val Gold = Color(0xFFFFD166)
val Mint = Color(0xFF3DDC97)

val LocalDarkTheme = compositionLocalOf { true }

private val DarkColors = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF12305E),
    onPrimaryContainer = Color(0xFFD6E6FF),
    secondary = Violet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF2E2266),
    onSecondaryContainer = Color(0xFFE6DEFF),
    tertiary = Cyan,
    onTertiary = Color(0xFF00232B),
    background = Color(0xFF060A18),
    onBackground = Color(0xFFEAF0FF),
    surface = Color(0xFF0E1428),
    onSurface = Color(0xFFEAF0FF),
    surfaceVariant = Color(0xFF1A2140),
    onSurfaceVariant = Color(0xFF9AA8CC),
    surfaceContainerLowest = Color(0xFF060A18),
    surfaceContainerLow = Color(0xFF0B1124),
    surfaceContainer = Color(0xFF0E1428),
    surfaceContainerHigh = Color(0xFF141C38),
    surfaceContainerHighest = Color(0xFF1A2346),
    outline = Color(0x40FFFFFF),
    outlineVariant = Color(0x22FFFFFF),
    error = Color(0xFFFF6B8A),
    onError = Color(0xFF3A0010)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF1F6FE0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9E8FF),
    onPrimaryContainer = Color(0xFF00214D),
    secondary = Color(0xFF7042E0),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE7DEFF),
    onSecondaryContainer = Color(0xFF260E66),
    tertiary = Color(0xFF008CB0),
    onTertiary = Color.White,
    background = Color(0xFFF2F5FF),
    onBackground = Color(0xFF0B1230),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0B1230),
    surfaceVariant = Color(0xFFE3E8FA),
    onSurfaceVariant = Color(0xFF4A5478),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F9FF),
    surfaceContainer = Color(0xFFF0F3FF),
    surfaceContainerHigh = Color(0xFFEAEEFF),
    surfaceContainerHighest = Color(0xFFE3E8FA),
    outline = Color(0x552A3568),
    outlineVariant = Color(0x222A3568),
    error = Color(0xFFD62B57),
    onError = Color.White
)

private val AppTypography = Typography(
    displayMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 40.sp, lineHeight = 46.sp, letterSpacing = (-1).sp),
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 36.sp, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp, letterSpacing = (-0.3).sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.1.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.3.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun DreamSaveTheme(themeMode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !dark
            controller.isAppearanceLightNavigationBars = !dark
        }
    }
    CompositionLocalProvider(LocalDarkTheme provides dark) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
