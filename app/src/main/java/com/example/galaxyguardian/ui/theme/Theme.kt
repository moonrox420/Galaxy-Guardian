package com.example.galaxyguardian.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.galaxyguardian.data.repository.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = BaseDarkCyanPrimary,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF8CEEFF),
    secondary = BaseDarkCosmicPurple,
    onSecondary = BaseDarkGalaxyBackground,
    secondaryContainer = Color(0xFF512DA8),
    onSecondaryContainer = BaseDarkTextPrimary,
    tertiary = BaseDarkNeonGreen,
    onTertiary = BaseDarkGalaxyBackground,
    tertiaryContainer = Color(0xFF004D25),
    onTertiaryContainer = BaseDarkTextPrimary,
    background = BaseDarkGalaxyBackground,
    onBackground = BaseDarkTextPrimary,
    surface = BaseDarkGalaxySurface,
    onSurface = BaseDarkTextPrimary,
    surfaceVariant = BaseDarkGalaxySurfaceVariant,
    onSurfaceVariant = BaseDarkTextSecondary,
    outline = BaseDarkGalaxySurfaceHighlight,
    error = BaseDarkSecurityRed,
    errorContainer = Color(0xFF6A040F),
    onError = BaseDarkTextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = BaseLightCyanPrimary,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFBCEBFF),
    onPrimaryContainer = Color(0xFF001F25),
    secondary = BaseLightCosmicPurple,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF0DFFF),
    onSecondaryContainer = Color(0xFF28004E),
    tertiary = BaseLightNeonGreen,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFB7F5C5),
    onTertiaryContainer = Color(0xFF00210C),
    background = BaseLightGalaxyBackground,
    onBackground = BaseLightTextPrimary,
    surface = BaseLightGalaxySurface,
    onSurface = BaseLightTextPrimary,
    surfaceVariant = BaseLightGalaxySurfaceVariant,
    onSurfaceVariant = BaseLightTextSecondary,
    outline = BaseLightGalaxySurfaceHighlight,
    error = BaseLightSecurityRed,
    errorContainer = Color(0xFFFFDAD6),
    onError = Color(0xFFFFFFFF)
)

@Composable
fun GalaxyGuardianTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemInDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme
    val galaxyColors = if (isDark) DarkGalaxyColors else LightGalaxyColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            try {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !isDark
                    isAppearanceLightNavigationBars = !isDark
                }
            } catch (_: Exception) {}
        }
    }

    CompositionLocalProvider(LocalGalaxyColors provides galaxyColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
