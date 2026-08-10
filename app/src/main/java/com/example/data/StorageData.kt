package com.example.data

import com.example.ui.screens.CardItem
import com.example.ui.screens.PurchaseItem
import com.example.ui.screens.SubscriptionItem
import java.util.UUID

const val CURRENT_VERSION = 10

data class SyncEmailDiagnosticLog(
    val messageId: String,
    val subject: String,
    val sender: String,
    val date: String,
    val snippet: String,
    val extractedValue: String,
    val extractedName: String,
    val extractedDate: String,
    val ignoreReason: String? = null,
    val finalResult: String,
    val debugCapturedFragment: String? = null
)

data class EmailSyncState(
    val lastSyncedAt: String? = null,
    val processedMessageIds: List<String> = emptyList(),
    val lastHistoryId: String? = null,
    val totalEmailsFound: Int = 0,
    val totalEmailsProcessed: Int = 0,
    val totalImported: Int = 0,
    val totalIgnored: Int = 0,
    val lastImportedExpense: String? = null,
    val lastError: String? = null,
    val scopeGranted: Boolean = false,
    val statusMessage: String? = "Não testado",
    val lastEmailSubject: String? = null,
    val lastEmailSender: String? = null,
    val lastEmailDate: String? = null,
    val diagnosticLogs: List<SyncEmailDiagnosticLog> = emptyList()
)

data class GoogleAccountData(
    val isConnected: Boolean = false,
    val userEmail: String? = null,
    val userName: String? = null,
    val photoUrl: String? = null,
    val connectedAt: String? = null,
    val accountId: String? = null,
    val idToken: String? = null,
    val emailSyncState: EmailSyncState? = null
)

data class CardPaymentItem(
    val id: String = UUID.randomUUID().toString(),
    val cardId: String,
    val month: Int,
    val year: Int,
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
    val googleAccount: GoogleAccountData? = null,
    val emailSyncState: EmailSyncState? = null,
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
