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

enum class VeroThemePalette(
    val id: String,
    val displayName: String,
    val subtitle: String
) {
    OBSIDIAN("OBSIDIAN", "Preto", "Modo escuro com detalhes em verde"),
    SNOW("SNOW", "Branco", "Interface limpa e minimalista"),
    MIDNIGHT("MIDNIGHT", "Azul", "Tons escuros e profundos de azul"),
    EMERALD("EMERALD", "Verde", "Verde escuro com toque financeiro");

    companion object {
        fun fromId(id: String?): VeroThemePalette =
            entries.find { it.id.equals(id, ignoreCase = true) } ?: OBSIDIAN
    }
}

private val ObsidianColorScheme = darkColorScheme(
    background = Color(0xFF0F1115),
    surface = Color(0xFF181B21),
    surfaceVariant = Color(0xFF13161C),
    outline = Color(0xFF262A35),
    outlineVariant = Color(0xFF262A35),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFF94A3B8),
    primary = Color(0xFF10B981),
    onPrimary = Color(0xFF022C22)
)

private val MidnightColorScheme = darkColorScheme(
    background = Color(0xFF0A0F1D),
    surface = Color(0xFF111A30),
    surfaceVariant = Color(0xFF0D1527),
    outline = Color(0xFF1E2D54),
    outlineVariant = Color(0xFF1E2D54),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFF94A3B8),
    primary = Color(0xFF38BDF8),
    onPrimary = Color(0xFF082F49)
)

private val EmeraldColorScheme = darkColorScheme(
    background = Color(0xFF061A14),
    surface = Color(0xFF0C2D23),
    surfaceVariant = Color(0xFF08231B),
    outline = Color(0xFF174839),
    outlineVariant = Color(0xFF174839),
    onBackground = Color(0xFFF2FBF7),
    onSurface = Color(0xFFF2FBF7),
    onSurfaceVariant = Color(0xFFA7F3D0),
    primary = Color(0xFF34D399),
    onPrimary = Color(0xFF022C22)
)

private val SnowColorScheme = lightColorScheme(
    background = Color(0xFFF8F9FB),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF1F5F9),
    outline = Color(0xFFE2E8F0),
    outlineVariant = Color(0xFFE2E8F0),
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF475569),
    primary = Color(0xFF0284C7),
    onPrimary = Color(0xFFFFFFFF)
)

@Composable
fun FinanceiroPessoalTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    palette: VeroThemePalette = VeroThemePalette.OBSIDIAN,
    content: @Composable () -> Unit
) {
    val colorScheme = when (palette) {
        VeroThemePalette.OBSIDIAN -> ObsidianColorScheme
        VeroThemePalette.MIDNIGHT -> MidnightColorScheme
        VeroThemePalette.EMERALD -> EmeraldColorScheme
        VeroThemePalette.SNOW -> SnowColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
