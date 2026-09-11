package com.example.data.subscription

import androidx.annotation.DrawableRes

data class StandardSubscription(
    val id: String,
    val displayName: String,
    val colorHex: String,
    val category: String = "Streaming",
    val aliases: List<String> = emptyList(),
    val domain: String? = null,
    val isCustom: Boolean = false,
    @DrawableRes val logoResId: Int? = null
)
