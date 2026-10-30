package com.sahaaya.companion.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light Colors
val PrimaryTeal = Color(0xFF00695C)
val PrimaryTealDark = Color(0xFF004D40)
val SecondaryNavy = Color(0xFF1F4E8C)
val DangerRed = Color(0xFFD32F2F)
val DangerRedLight = Color(0xFFFFEBEE)
val WarningAmber = Color(0xFFEF6C00)
val WarningAmberLight = Color(0xFFFFF3E0)
val SuccessGreen = Color(0xFF2E7D32)
val SuccessGreenLight = Color(0xFFE8F5E9)
val BackgroundLight = Color(0xFFF4F6F9)
val SurfaceWhite = Color(0xFFFFFFFF)

// Dark Colors
val BackgroundDark = Color(0xFF070F1E)
val SurfaceDark = Color(0xFF0E1A2C)
val SurfaceElevatedDark = Color(0xFF16253B)
val PrimaryTealDarkTheme = Color(0xFF2DD4BF)
val SecondaryNavyDarkTheme = Color(0xFF60A5FA)
val DangerRedDark = Color(0xFFF87171)
val DangerRedDarkBg = Color(0xFF3B1219)
val WarningAmberDark = Color(0xFFFBBF24)
val WarningAmberDarkBg = Color(0xFF38230D)
val SuccessGreenDark = Color(0xFF4ADE80)
val SuccessGreenDarkBg = Color(0xFF0F2E1B)
val TextPrimaryDark = Color(0xFFF8FAFC)
val TextSecondaryDark = Color(0xFF94A3B8)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryTeal,
    onPrimary = Color.White,
    secondary = SecondaryNavy,
    onSecondary = Color.White,
    tertiary = WarningAmber,
    background = BackgroundLight,
    onBackground = Color(0xFF0F172A),
    surface = SurfaceWhite,
    onSurface = Color(0xFF0F172A),
    error = DangerRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryTealDarkTheme,
    onPrimary = Color(0xFF042F2C),
    secondary = SecondaryNavyDarkTheme,
    onSecondary = Color(0xFF082F49),
    tertiary = WarningAmberDark,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    error = DangerRedDark,
    onError = Color.Black
)

@Composable
fun SahaayaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
