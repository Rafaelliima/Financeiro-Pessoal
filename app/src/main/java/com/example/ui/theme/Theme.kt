package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryAccentLight,
    onPrimary = Color.White,
    primaryContainer = PrimaryAccentContainerLight,
    onPrimaryContainer = OnPrimaryAccentContainerLight,
    secondary = TextSecondaryLight,
    onSecondary = Color.White,
    background = BackgroundLightLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLightLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SecondaryBackgroundLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = DividerColorLight,
    outlineVariant = DividerColorLight
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryAccentDark,
    onPrimary = Color.Black,
    primaryContainer = PrimaryAccentContainerDark,
    onPrimaryContainer = OnPrimaryAccentContainerDark,
    secondary = TextSecondaryDark,
    onSecondary = Color.Black,
    background = BackgroundLightDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceLightDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SecondaryBackgroundDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = DividerColorDark,
    outlineVariant = DividerColorDark
)

@Composable
fun FinanceiroPessoalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Segue automaticamente o tema claro/escuro definido pelo usuário no sistema.
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
