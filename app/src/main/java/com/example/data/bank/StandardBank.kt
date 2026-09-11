package com.example.data.bank

import androidx.annotation.DrawableRes

/**
 * Representa uma Instituição Financeira padronizada do sistema.
 */
data class StandardBank(
    val id: String,
    val displayName: String,
    val officialName: String,
    val compe: String? = null,
    val slug: String = id,
    val colorHex: String = "#2563EB",
    val isCustom: Boolean = false,
    @DrawableRes val logoResId: Int? = null
)
