package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Design System Colors - Financeiro Pessoal
// Paleta clara (padrão)
val PrimaryAccentLight = Color(0xFF2563EB)
val PrimaryAccentContainerLight = Color(0xFFEFF6FF)
val OnPrimaryAccentContainerLight = Color(0xFF1E40AF)
val TextPrimaryLight = Color(0xFF111111)
val TextSecondaryLight = Color(0xFF6B7280)
val DividerColorLight = Color(0xFFE5E7EB)
val BackgroundLightLight = Color(0xFFFFFFFF)
val SecondaryBackgroundLight = Color(0xFFF8F9FA)
val SurfaceLightLight = Color(0xFFFFFFFF)

// Paleta escura
val PrimaryAccentDark = Color(0xFF60A5FA)
val PrimaryAccentContainerDark = Color(0xFF1E3A5F)
val OnPrimaryAccentContainerDark = Color(0xFFBFDBFE)
val TextPrimaryDark = Color(0xFFF3F4F6)
val TextSecondaryDark = Color(0xFF9CA3AF)
val DividerColorDark = Color(0xFF2D3136)
val BackgroundLightDark = Color(0xFF121212)
val SecondaryBackgroundDark = Color(0xFF1E2124)
val SurfaceLightDark = Color(0xFF1A1C1E)

// Cores dinâmicas resolvidas conforme o tema do sistema
val PrimaryAccent: Color @Composable get() = if (isSystemInDarkTheme()) PrimaryAccentDark else PrimaryAccentLight
val PrimaryAccentContainer: Color @Composable get() = if (isSystemInDarkTheme()) PrimaryAccentContainerDark else PrimaryAccentContainerLight
val OnPrimaryAccentContainer: Color @Composable get() = if (isSystemInDarkTheme()) OnPrimaryAccentContainerDark else OnPrimaryAccentContainerLight
val TextPrimary: Color @Composable get() = if (isSystemInDarkTheme()) TextPrimaryDark else TextPrimaryLight
val TextSecondary: Color @Composable get() = if (isSystemInDarkTheme()) TextSecondaryDark else TextSecondaryLight
val DividerColor: Color @Composable get() = if (isSystemInDarkTheme()) DividerColorDark else DividerColorLight
val BackgroundLight: Color @Composable get() = if (isSystemInDarkTheme()) BackgroundLightDark else BackgroundLightLight
val SecondaryBackground: Color @Composable get() = if (isSystemInDarkTheme()) SecondaryBackgroundDark else SecondaryBackgroundLight
val SurfaceLight: Color @Composable get() = if (isSystemInDarkTheme()) SurfaceLightDark else SurfaceLightLight
