package com.example.data

import com.google.firebase.firestore.PropertyName
import java.util.UUID

import com.example.ui.theme.ThemeMode

const val CURRENT_VERSION = 12

data class GoogleAccountData(
    @get:PropertyName("isConnected") @set:PropertyName("isConnected") var isConnected: Boolean = false,
    val userEmail: String? = null,
    val userName: String? = null,
    val photoUrl: String? = null,
    val connectedAt: String? = null,
    val accountId: String? = null,
    val idToken: String? = null
)

data class CardPaymentItem(
    val id: String = UUID.randomUUID().toString(),
    val cardId: String = "",
    val month: Int = 1,
    val year: Int = 2026,
    val paid: Boolean = true,
    val paidAt: String? = null
)

/**
 * Estrutura principal dos dados financeiros persistidos no arquivo JSON.
 * Preparada para suportar novas coleções futuras sem quebrar dados existentes.
 */
data class StorageData(
    val version: Int = CURRENT_VERSION,
    val cards: List<CardItem> = emptyList(),
    val purchases: List<PurchaseItem> = emptyList(),
    val subscriptions: List<SubscriptionItem> = emptyList(),
    val cardPayments: List<CardPaymentItem> = emptyList(),
    val dailyExpenses: List<DailyExpense> = emptyList(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val googleAccount: GoogleAccountData? = null,
    // Coleções preparadas para expansões futuras
    val categories: List<Any> = emptyList(),
    val accounts: List<Any> = emptyList(),
    val investments: List<Any> = emptyList(),
    val goals: List<Any> = emptyList()
)

data class StorageOperationResult<T>(
    val data: T?,
    val isSuccess: Boolean,
    val message: String
)
