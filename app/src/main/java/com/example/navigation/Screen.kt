package com.example.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Início", Icons.Outlined.Dashboard)
    data object Purchases : Screen("purchases", "Gastos", Icons.AutoMirrored.Outlined.ReceiptLong)
    data object Settings : Screen("settings", "Ajustes", Icons.Outlined.Settings)

    companion object {
        val items: List<Screen> get() = listOf(Dashboard, Purchases, Settings)
    }
}

