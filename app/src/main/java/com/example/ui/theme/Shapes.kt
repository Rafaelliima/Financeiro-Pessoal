package com.example.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Escala oficial de formas e curvaturas do aplicativo Financeiro Pessoal.
 * Padroniza os cantos arredondados (8dp, 16dp, 24dp, Full).
 */
object AppShapes {
    val Small: Shape = RoundedCornerShape(8.dp)
    val Medium: Shape = RoundedCornerShape(16.dp)
    val Large: Shape = RoundedCornerShape(24.dp)
    val Full: Shape = CircleShape
}
