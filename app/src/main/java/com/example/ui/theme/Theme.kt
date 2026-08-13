package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

private val LightColorScheme = lightColorScheme(
    primary = PrimaryAccentLight,
    onPrimary = Color.White,
    primaryContainer = PrimaryAccentContainerLight,
    onPrimaryContainer = OnPrimaryAccentContainerLight,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = BackgroundLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = DividerColorLight,
    outlineVariant = DividerColorLight,
    secondary = TextSecondaryLight
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryAccentDark,
    onPrimary = Color.Black,
    primaryContainer = PrimaryAccentContainerDark,
    onPrimaryContainer = OnPrimaryAccentContainerDark,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = BackgroundDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = DividerColorDark,
    outlineVariant = DividerColorDark,
    secondary = TextSecondaryDark
)

@Composable
fun FinanceiroPessoalTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
