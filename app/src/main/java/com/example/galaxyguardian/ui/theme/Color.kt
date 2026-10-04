package com.example.galaxyguardian.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Base Static Dark Palette
val BaseDarkGalaxyBackground = Color(0xFF090C15)
val BaseDarkGalaxySurface = Color(0xFF121726)
val BaseDarkGalaxySurfaceVariant = Color(0xFF1B2338)
val BaseDarkGalaxySurfaceHighlight = Color(0xFF242E49)
val BaseDarkTextPrimary = Color(0xFFF0F4FC)
val BaseDarkTextSecondary = Color(0xFF98A6C5)
val BaseDarkTextTertiary = Color(0xFF6B7A99)
val BaseDarkCyanPrimary = Color(0xFF00E5FF)
val BaseDarkCosmicPurple = Color(0xFFB388FF)
val BaseDarkNeonGreen = Color(0xFF00E676)
val BaseDarkWarningAmber = Color(0xFFFFB300)
val BaseDarkSecurityRed = Color(0xFFFF5252)

// Base Static Light Palette
val BaseLightGalaxyBackground = Color(0xFFF4F6FB)
val BaseLightGalaxySurface = Color(0xFFFFFFFF)
val BaseLightGalaxySurfaceVariant = Color(0xFFE9EDF5)
val BaseLightGalaxySurfaceHighlight = Color(0xFFDDE3EE)
val BaseLightTextPrimary = Color(0xFF0F172A)
val BaseLightTextSecondary = Color(0xFF475569)
val BaseLightTextTertiary = Color(0xFF64748B)
val BaseLightCyanPrimary = Color(0xFF00838F)
val BaseLightCosmicPurple = Color(0xFF6A1B9A)
val BaseLightNeonGreen = Color(0xFF00796B)
val BaseLightWarningAmber = Color(0xFFD97706)
val BaseLightSecurityRed = Color(0xFFDC2626)

data class GalaxyColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val surfaceHighlight: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val primaryCyan: Color,
    val cosmicPurple: Color,
    val neonGreen: Color,
    val warningAmber: Color,
    val securityRed: Color,
    val terminalBackground: Color,
    val terminalText: Color
)

val DarkGalaxyColors = GalaxyColors(
    isDark = true,
    background = BaseDarkGalaxyBackground,
    surface = BaseDarkGalaxySurface,
    surfaceVariant = BaseDarkGalaxySurfaceVariant,
    surfaceHighlight = BaseDarkGalaxySurfaceHighlight,
    textPrimary = BaseDarkTextPrimary,
    textSecondary = BaseDarkTextSecondary,
    textTertiary = BaseDarkTextTertiary,
    primaryCyan = BaseDarkCyanPrimary,
    cosmicPurple = BaseDarkCosmicPurple,
    neonGreen = BaseDarkNeonGreen,
    warningAmber = BaseDarkWarningAmber,
    securityRed = BaseDarkSecurityRed,
    terminalBackground = Color(0xFF05070D),
    terminalText = Color(0xFF4AF626)
)

val LightGalaxyColors = GalaxyColors(
    isDark = false,
    background = BaseLightGalaxyBackground,
    surface = BaseLightGalaxySurface,
    surfaceVariant = BaseLightGalaxySurfaceVariant,
    surfaceHighlight = BaseLightGalaxySurfaceHighlight,
    textPrimary = BaseLightTextPrimary,
    textSecondary = BaseLightTextSecondary,
    textTertiary = BaseLightTextTertiary,
    primaryCyan = BaseLightCyanPrimary,
    cosmicPurple = BaseLightCosmicPurple,
    neonGreen = BaseLightNeonGreen,
    warningAmber = BaseLightWarningAmber,
    securityRed = BaseLightSecurityRed,
    terminalBackground = Color(0xFF1E293B),
    terminalText = Color(0xFF38BDF8)
)

val LocalGalaxyColors = staticCompositionLocalOf { DarkGalaxyColors }

object GalaxyTheme {
    val colors: GalaxyColors
        @Composable
        get() = LocalGalaxyColors.current
}

// Dynamic Theme-Aware Color Accessors for Composable Tree
val GalaxyBackground: Color
    @Composable
    get() = GalaxyTheme.colors.background

val GalaxySurface: Color
    @Composable
    get() = GalaxyTheme.colors.surface

val GalaxySurfaceVariant: Color
    @Composable
    get() = GalaxyTheme.colors.surfaceVariant

val GalaxySurfaceHighlight: Color
    @Composable
    get() = GalaxyTheme.colors.surfaceHighlight

val CyanPrimary: Color
    @Composable
    get() = GalaxyTheme.colors.primaryCyan

val CosmicPurple: Color
    @Composable
    get() = GalaxyTheme.colors.cosmicPurple

val NeonGreen: Color
    @Composable
    get() = GalaxyTheme.colors.neonGreen

val WarningAmber: Color
    @Composable
    get() = GalaxyTheme.colors.warningAmber

val SecurityRed: Color
    @Composable
    get() = GalaxyTheme.colors.securityRed

val TextPrimary: Color
    @Composable
    get() = GalaxyTheme.colors.textPrimary

val TextSecondary: Color
    @Composable
    get() = GalaxyTheme.colors.textSecondary

val TextTertiary: Color
    @Composable
    get() = GalaxyTheme.colors.textTertiary

val TerminalBackground: Color
    @Composable
    get() = GalaxyTheme.colors.terminalBackground

val TerminalText: Color
    @Composable
    get() = GalaxyTheme.colors.terminalText

val CodeKeyword = Color(0xFFFF7B72)
val CodeString = Color(0xFFA5D6FF)
val CodeComment = Color(0xFF8B949E)
val CodeFunction = Color(0xFFD2A8FF)
