package com.example.data

import com.example.ui.screens.CardItem
import com.example.ui.screens.PurchaseItem
import com.example.ui.screens.SubscriptionItem
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

object StorageReader {

    /**
     * Leitura resiliente do arquivo JSON com verificação de versão,
     * migração automática e suporte a recuperação de dados corrompidos sem apagar o arquivo original.
     */
    fun readStorage(
        file: File,
        createInitialIfMissing: (File) -> StorageOperationResult<StorageData>
    ): StorageOperationResult<StorageData> {
        if (!file.exists()) {
            return createInitialIfMissing(file)
        }

        val rawContent = try {
            file.readText().trim()
        } catch (e: Exception) {
            return StorageOperationResult(
                data = StorageData(),
                isSuccess = false,
                message = "Erro ao acessar o arquivo JSON: ${e.localizedMessage}. O arquivo original foi preservado."
            )
        }

        if (rawContent.isEmpty()) {
            return StorageOperationResult(
                data = StorageData(version = CURRENT_VERSION),
                isSuccess = true,
                message = "Arquivo JSON vazio detectado. Estrutura inicializada."
            )
        }

        return try {
            val rawJsonObject = JSONObject(rawContent)

            // Passa pelo migrador automático de versão (ex: v0 -> v1 -> v2)
            val jsonObject = DataMigrator.migrateIfNeeded(rawJsonObject)

            val version = jsonObject.optInt("version", CURRENT_VERSION)

            // Cards
            val cardsArray = jsonObject.optJSONArray("cards") ?: JSONArray()
            val cardsList = mutableListOf<CardItem>()
            for (i in 0 until cardsArray.length()) {
                val cardObj = cardsArray.optJSONObject(i) ?: continue
                val id = cardObj.optString("id", UUID.randomUUID().toString())
                val name = cardObj.optString("name", "")
                val sourceStr = cardObj.optString("source", DataSource.MANUAL.name)
                val createdAt = if (cardObj.has("createdAt")) cardObj.optString("createdAt", null) else null
                val updatedAt = if (cardObj.has("updatedAt")) cardObj.optString("updatedAt", null) else null

                if (name.isNotBlank()) {
                    cardsList.add(
                        CardItem(
                            id = id,
                            name = name,
                            source = DataSource.fromString(sourceStr),
                            createdAt = createdAt,
                            updatedAt = updatedAt
                        )
                    )
                }
            }

            // Purchases
            val purchasesArray = jsonObject.optJSONArray("purchases") ?: JSONArray()
            val purchasesList = mutableListOf<PurchaseItem>()
            for (i in 0 until purchasesArray.length()) {
                val pObj = purchasesArray.optJSONObject(i) ?: continue
                val id = pObj.optString("id", UUID.randomUUID().toString())
                val name = pObj.optString("name", "")
                val totalAmount = pObj.optDouble("totalAmount", 0.0)
                val cardId = pObj.optString("cardId", "")
                val cardName = pObj.optString("cardName", "")
                val isInstallment = pObj.optBoolean("isInstallment", false)
                val totalInstallments = pObj.optInt("totalInstallments", 1)
                val startMonth = pObj.optInt("startMonth", 1)
                val startYear = pObj.optInt("startYear", 2026)
                val paidInstallmentsCount = pObj.optInt("paidInstallmentsCount", 0)
                val isQuitada = pObj.optBoolean("isQuitada", false)
                val completedAt = if (pObj.has("completedAt") && !pObj.isNull("completedAt")) pObj.optString("completedAt") else null
                val sourceStr = pObj.optString("source", DataSource.MANUAL.name)
                val createdAt = if (pObj.has("createdAt")) pObj.optString("createdAt", null) else null
                val updatedAt = if (pObj.has("updatedAt")) pObj.optString("updatedAt", null) else null

                if (name.isNotBlank()) {
                    purchasesList.add(
                        PurchaseItem(
                            id = id,
                            name = name,
                            totalAmount = totalAmount,
                            cardId = cardId,
                            cardName = cardName,
                            isInstallment = isInstallment,
                            totalInstallments = totalInstallments,
                            startMonth = startMonth,
                            startYear = startYear,
                            paidInstallmentsCount = paidInstallmentsCount,
                            isQuitada = isQuitada,
                            completedAt = completedAt,
                            source = DataSource.fromString(sourceStr),
                            createdAt = createdAt,
                            updatedAt = updatedAt
                        )
                    )
                }
            }

            // Subscriptions
            val subscriptionsArray = jsonObject.optJSONArray("subscriptions") ?: JSONArray()
            val subscriptionsList = mutableListOf<SubscriptionItem>()
            for (i in 0 until subscriptionsArray.length()) {
                val subObj = subscriptionsArray.optJSONObject(i) ?: continue
                val id = subObj.optString("id", UUID.randomUUID().toString())
                val name = subObj.optString("name", "")
                val monthlyValue = subObj.optDouble("monthlyValue", 0.0)
                val cardId = subObj.optString("cardId", "")
                val cardName = subObj.optString("cardName", "")
                val sourceStr = subObj.optString("source", DataSource.MANUAL.name)
                val createdAt = if (subObj.has("createdAt")) subObj.optString("createdAt", null) else null
                val updatedAt = if (subObj.has("updatedAt")) subObj.optString("updatedAt", null) else null

                if (name.isNotBlank()) {
                    subscriptionsList.add(
                        SubscriptionItem(
                            id = id,
                            name = name,
                            monthlyValue = monthlyValue,
                            cardId = cardId,
                            cardName = cardName,
                            source = DataSource.fromString(sourceStr),
                            createdAt = createdAt,
                            updatedAt = updatedAt
                        )
                    )
                }
            }

            // Card Payments
            val cardPaymentsArray = jsonObject.optJSONArray("cardPayments") ?: JSONArray()
            val cardPaymentsList = mutableListOf<CardPaymentItem>()
            for (i in 0 until cardPaymentsArray.length()) {
                val cpObj = cardPaymentsArray.optJSONObject(i) ?: continue
                val id = cpObj.optString("id", UUID.randomUUID().toString())
                val cardId = cpObj.optString("cardId", "")
                val month = cpObj.optInt("month", 1)
                val year = cpObj.optInt("year", 2026)
                val paid = cpObj.optBoolean("paid", true)
                val paidAt = if (cpObj.has("paidAt")) cpObj.optString("paidAt", null) else null

                if (cardId.isNotBlank()) {
                    cardPaymentsList.add(
                        CardPaymentItem(
                            id = id,
                            cardId = cardId,
                            month = month,
                            year = year,
                            paid = paid,
                            paidAt = paidAt
                        )
                    )
                }
            }

            // Daily Expenses
            val dailyExpensesArray = jsonObject.optJSONArray("dailyExpenses") ?: JSONArray()
            val dailyExpensesList = mutableListOf<DailyExpense>()
            for (i in 0 until dailyExpensesArray.length()) {
                val deObj = dailyExpensesArray.optJSONObject(i) ?: continue
                val id = deObj.optString("id", UUID.randomUUID().toString())
                val name = deObj.optString("name", "")
                val value = deObj.optDouble("value", 0.0)
                val date = deObj.optString("date", "")
                val sourceStr = deObj.optString("source", DataSource.MANUAL.name)
                val observation = if (deObj.has("observation") && !deObj.isNull("observation")) deObj.optString("observation") else null
                val sourceMessageId = if (deObj.has("sourceMessageId") && !deObj.isNull("sourceMessageId")) deObj.optString("sourceMessageId") else null
                val emailSubject = if (deObj.has("emailSubject") && !deObj.isNull("emailSubject")) deObj.optString("emailSubject") else null
                val emailSender = if (deObj.has("emailSender") && !deObj.isNull("emailSender")) deObj.optString("emailSender") else null
                val providerStr = if (deObj.has("importProvider") && !deObj.isNull("importProvider")) deObj.optString("importProvider") else null
                val importProvider = ImportProvider.fromString(providerStr)
                val confidence = if (deObj.has("confidence") && !deObj.isNull("confidence")) deObj.optDouble("confidence", 1.0) else null
                val createdAt = if (deObj.has("createdAt")) deObj.optString("createdAt", null) else null
                val updatedAt = if (deObj.has("updatedAt")) deObj.optString("updatedAt", null) else null

                if (name.isNotBlank()) {
                    dailyExpensesList.add(
                        DailyExpense(
                            id = id,
                            name = name,
                            value = value,
                            date = date,
                            source = DataSource.fromString(sourceStr),
                            observation = observation,
                            sourceMessageId = sourceMessageId,
                            emailSubject = emailSubject,
                            emailSender = emailSender,
                            importProvider = importProvider,
                            confidence = confidence,
                            createdAt = createdAt,
                            updatedAt = updatedAt
                        )
                    )
                }
            }

            // Google Account (Sprint 15, 16, 17 & 18 Real OAuth + Gmail + Auto Expense Import + Diagnostics)
            val googleObj = jsonObject.optJSONObject("googleAccount")
            val rootSyncObj = jsonObject.optJSONObject("emailSyncState")
            val googleSyncObj = googleObj?.optJSONObject("emailSyncState")
            val syncObj = rootSyncObj ?: googleSyncObj

            val emailSyncState = if (syncObj != null) {
                val msgIdsArray = syncObj.optJSONArray("processedMessageIds")
                val processedIds = mutableListOf<String>()
                if (msgIdsArray != null) {
                    for (i in 0 until msgIdsArray.length()) {
                        processedIds.add(msgIdsArray.optString(i))
                    }
                }
                val logsArray = syncObj.optJSONArray("diagnosticLogs")
                val diagnosticLogsList = mutableListOf<SyncEmailDiagnosticLog>()
                if (logsArray != null) {
                    for (i in 0 until logsArray.length()) {
                        val logObj = logsArray.optJSONObject(i) ?: continue
                        diagnosticLogsList.add(
                            SyncEmailDiagnosticLog(
                                messageId = logObj.optString("messageId", ""),
                                subject = logObj.optString("subject", ""),
                                sender = logObj.optString("sender", ""),
                                date = logObj.optString("date", ""),
                                snippet = logObj.optString("snippet", ""),
                                extractedValue = logObj.optString("extractedValue", "não encontrado"),
                                extractedName = logObj.optString("extractedName", "não encontrado"),
                                extractedDate = logObj.optString("extractedDate", "não encontrada"),
                                ignoreReason = if (logObj.has("ignoreReason") && !logObj.isNull("ignoreReason")) logObj.optString("ignoreReason") else null,
                                finalResult = logObj.optString("finalResult", "Ignorado"),
                                debugCapturedFragment = if (logObj.has("debugCapturedFragment") && !logObj.isNull("debugCapturedFragment")) logObj.optString("debugCapturedFragment") else null
                            )
                        )
                    }
                }
                EmailSyncState(
                    lastSyncedAt = if (syncObj.has("lastSyncedAt") && !syncObj.isNull("lastSyncedAt")) syncObj.optString("lastSyncedAt") else null,
                    processedMessageIds = processedIds,
                    lastHistoryId = if (syncObj.has("lastHistoryId") && !syncObj.isNull("lastHistoryId")) syncObj.optString("lastHistoryId") else null,
                    totalEmailsFound = syncObj.optInt("totalEmailsFound", 0),
                    totalEmailsProcessed = syncObj.optInt("totalEmailsProcessed", 0),
                    totalImported = syncObj.optInt("totalImported", 0),
                    totalIgnored = syncObj.optInt("totalIgnored", 0),
                    lastImportedExpense = if (syncObj.has("lastImportedExpense") && !syncObj.isNull("lastImportedExpense")) syncObj.optString("lastImportedExpense") else null,
                    lastError = if (syncObj.has("lastError") && !syncObj.isNull("lastError")) syncObj.optString("lastError") else null,
                    scopeGranted = syncObj.optBoolean("scopeGranted", false),
                    statusMessage = if (syncObj.has("statusMessage") && !syncObj.isNull("statusMessage")) syncObj.optString("statusMessage") else "Não testado",
                    lastEmailSubject = if (syncObj.has("lastEmailSubject") && !syncObj.isNull("lastEmailSubject")) syncObj.optString("lastEmailSubject") else null,
                    lastEmailSender = if (syncObj.has("lastEmailSender") && !syncObj.isNull("lastEmailSender")) syncObj.optString("lastEmailSender") else null,
                    lastEmailDate = if (syncObj.has("lastEmailDate") && !syncObj.isNull("lastEmailDate")) syncObj.optString("lastEmailDate") else null,
                    diagnosticLogs = diagnosticLogsList
                )
            } else null

            val googleAccount = if (googleObj != null) {
                GoogleAccountData(
                    isConnected = googleObj.optBoolean("isConnected", false),
                    userEmail = if (googleObj.has("userEmail") && !googleObj.isNull("userEmail")) googleObj.optString("userEmail") else null,
                    userName = if (googleObj.has("userName") && !googleObj.isNull("userName")) googleObj.optString("userName") else null,
                    photoUrl = if (googleObj.has("photoUrl") && !googleObj.isNull("photoUrl")) googleObj.optString("photoUrl") else null,
                    connectedAt = if (googleObj.has("connectedAt") && !googleObj.isNull("connectedAt")) googleObj.optString("connectedAt") else null,
                    accountId = if (googleObj.has("accountId") && !googleObj.isNull("accountId")) googleObj.optString("accountId") else null,
                    idToken = if (googleObj.has("idToken") && !googleObj.isNull("idToken")) googleObj.optString("idToken") else null,
                    emailSyncState = emailSyncState
                )
            } else null

            val loadedData = StorageData(
                version = version,
                cards = cardsList,
                purchases = purchasesList,
                subscriptions = subscriptionsList,
                cardPayments = cardPaymentsList,
                dailyExpenses = dailyExpensesList,
                googleAccount = googleAccount,
                emailSyncState = emailSyncState
            )

            StorageOperationResult(
                data = loadedData,
                isSuccess = true,
                message = "Dados v$version carregados do arquivo JSON com sucesso."
            )
        } catch (e: Exception) {
            // Em caso de erro na leitura ou JSON corrompido: Tenta recuperação parcial sem apagar o arquivo original
            val recoveryResult = DataRecovery.attemptRecovery(rawContent)
            if (recoveryResult.recoveredCount > 0) {
                StorageOperationResult(
                    data = recoveryResult.data,
                    isSuccess = false,
                    message = "Arquivo JSON corrompido detectado. ${recoveryResult.details} O arquivo original foi preservado."
                )
            } else {
                StorageOperationResult(
                    data = StorageData(),
                    isSuccess = false,
                    message = "Arquivo JSON corrompido: ${e.localizedMessage ?: "Formato inválido"}. O arquivo original foi preservado."
                )
            }
        }
    }
}
