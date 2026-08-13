package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Design System Colors - Financeiro Pessoal (Modern Rebirth)

// Paleta Clara
val BackgroundLight = Color(0xFFF8FAFC)
val SurfaceLight = Color(0xFFFFFFFF)
val PrimaryAccentLight = Color(0xFF0284C7)
val PrimaryAccentContainerLight = Color(0xFFE0F2FE)
val OnPrimaryAccentContainerLight = Color(0xFF0369A1)
val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF64748B)
val DividerColorLight = Color(0xFFE2E8F0)

// Paleta Escura
val BackgroundDark = Color(0xFF0D1117)
val SurfaceDark = Color(0xFF161B22)
val PrimaryAccentDark = Color(0xFF38BDF8)
val PrimaryAccentContainerDark = Color(0xFF0C4A6E)
val OnPrimaryAccentContainerDark = Color(0xFFBAE6FD)
val TextPrimaryDark = Color(0xFFF8FAFC)
val TextSecondaryDark = Color(0xFF94A3B8)
val DividerColorDark = Color(0xFF21262D)

// Compatibilidade com código existente (Propriedades Composable baseadas no Esquema de Cores do MaterialTheme)
val PrimaryAccent: Color @Composable get() = MaterialTheme.colorScheme.primary
val TextPrimary: Color @Composable get() = MaterialTheme.colorScheme.onBackground
val TextSecondary: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
val DividerColor: Color @Composable get() = MaterialTheme.colorScheme.outlineVariant

// Cores Funcionais
val SuccessGreen = Color(0xFF10B981)
val ErrorRed = Color(0xFFEF4444)
val WarningOrange = Color(0xFFF59E0B)

// Gradientes para Cards Hero
val HeroGradientStart = Color(0xFF0284C7)
val HeroGradientEnd = Color(0xFF0369A1)
val HeroGradientDarkStart = Color(0xFF075985)
val HeroGradientDarkEnd = Color(0xFF0C4A6E)
