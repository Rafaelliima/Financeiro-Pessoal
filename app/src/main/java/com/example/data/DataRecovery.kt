package com.example.data

import com.example.data.CardItem
import com.example.data.PurchaseItem
import com.example.data.SubscriptionItem
import com.example.data.PaymentMethod
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class RecoveryResult(
    val data: StorageData,
    val recoveredCount: Int,
    val details: String
)

object DataRecovery {

    /**
     * Tenta recuperar o máximo de itens válidos de uma string JSON parcialmente corrompida.
     * Preserva o arquivo original sem sobrescrevê-lo.
     */
    fun attemptRecovery(rawContent: String): RecoveryResult {
        val cards = mutableListOf<CardItem>()
        val purchases = mutableListOf<PurchaseItem>()
        val subscriptions = mutableListOf<SubscriptionItem>()
        val cardPayments = mutableListOf<CardPaymentItem>()
        val dailyExpenses = mutableListOf<DailyExpense>()
        val reminders = mutableListOf<ReminderItem>()

        try {
            val root = JSONObject(rawContent)

            // Extração defensiva de cartões
            val cardsArray = root.optJSONArray("cards") ?: JSONArray()
            for (i in 0 until cardsArray.length()) {
                try {
                    val cObj = cardsArray.optJSONObject(i) ?: continue
                    val name = cObj.optString("name", "")
                    if (name.isNotBlank()) {
                        val id = cObj.optString("id", UUID.randomUUID().toString())
                        val colorHex = if (cObj.has("colorHex") && !cObj.isNull("colorHex")) cObj.optString("colorHex") else null
                        val sourceStr = cObj.optString("source", DataSource.MANUAL.name)
                        val createdAt = cObj.optString("createdAt", null)
                        val updatedAt = cObj.optString("updatedAt", null)
                        val bankId = if (cObj.has("bankId") && !cObj.isNull("bankId")) {
                            cObj.optString("bankId")
                        } else {
                            com.example.data.bank.BankRegistry.getBankForCard(name, null, colorHex).id
                        }
                        cards.add(
                            CardItem(
                                id = id,
                                name = name,
                                colorHex = colorHex,
                                bankId = bankId,
                                source = DataSource.fromString(sourceStr),
                                createdAt = createdAt,
                                updatedAt = updatedAt
                            )
                        )
                    }
                } catch (_: Exception) {}
            }

            // Extração defensiva de compras
            val purchasesArray = root.optJSONArray("purchases") ?: JSONArray()
            for (i in 0 until purchasesArray.length()) {
                try {
                    val pObj = purchasesArray.optJSONObject(i) ?: continue
                    val name = pObj.optString("name", "")
                    if (name.isNotBlank()) {
                        val id = pObj.optString("id", UUID.randomUUID().toString())
                        val totalAmount = pObj.optDouble("totalAmount", 0.0)
                        val cardId = pObj.optString("cardId", "")
                        val cardName = pObj.optString("cardName", "")
                        val isInstallment = pObj.optBoolean("isInstallment", pObj.optBoolean("installment", false))
                        val totalInstallments = pObj.optInt("totalInstallments", 1)
                        val startMonth = pObj.optInt("startMonth", 1)
                        val startYear = pObj.optInt("startYear", 2026)
                        val paidInstallmentsCount = pObj.optInt("paidInstallmentsCount", 0)
                        val isQuitada = pObj.optBoolean("isQuitada", pObj.optBoolean("quitada", false))
                        val completedAt = pObj.optString("completedAt", null)
                        val sourceStr = pObj.optString("source", DataSource.MANUAL.name)
                        val createdAt = pObj.optString("createdAt", null)
                        val updatedAt = pObj.optString("updatedAt", null)

                        purchases.add(
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
                } catch (_: Exception) {}
            }

            // Extração defensiva de assinaturas
            val subsArray = root.optJSONArray("subscriptions") ?: JSONArray()
            for (i in 0 until subsArray.length()) {
                try {
                    val sObj = subsArray.optJSONObject(i) ?: continue
                    val name = sObj.optString("name", "")
                    if (name.isNotBlank()) {
                        val id = sObj.optString("id", UUID.randomUUID().toString())
                        val monthlyValue = sObj.optDouble("monthlyValue", 0.0)
                        val cardId = sObj.optString("cardId", "")
                        val cardName = sObj.optString("cardName", "")
                        val sourceStr = sObj.optString("source", DataSource.MANUAL.name)
                        val createdAt = sObj.optString("createdAt", null)
                        val updatedAt = sObj.optString("updatedAt", null)
                        val isShared = sObj.optBoolean("isShared", sObj.optBoolean("shared", false))
                        val sharedWith = if (sObj.has("sharedWith")) sObj.optString("sharedWith", null) else null
                        val receivedAmount = sObj.optDouble("receivedAmount", 0.0)

                        subscriptions.add(
                            SubscriptionItem(
                                id = id,
                                name = name,
                                monthlyValue = monthlyValue,
                                cardId = cardId,
                                cardName = cardName,
                                source = DataSource.fromString(sourceStr),
                                createdAt = createdAt,
                                updatedAt = updatedAt,
                                isShared = isShared,
                                sharedWith = sharedWith,
                                receivedAmount = receivedAmount
                            )
                        )
                    }
                } catch (_: Exception) {}
            }

            // Extração defensiva de pagamentos de fatura
            val cpArray = root.optJSONArray("cardPayments") ?: JSONArray()
            for (i in 0 until cpArray.length()) {
                try {
                    val cpObj = cpArray.optJSONObject(i) ?: continue
                    val cardId = cpObj.optString("cardId", "")
                    if (cardId.isNotBlank()) {
                        val id = cpObj.optString("id", UUID.randomUUID().toString())
                        val month = cpObj.optInt("month", 1)
                        val year = cpObj.optInt("year", 2026)
                        val paid = cpObj.optBoolean("paid", true)
                        val paidAt = cpObj.optString("paidAt", null)

                        cardPayments.add(
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
                } catch (_: Exception) {}
            }
            // Extração defensiva de gastos do dia a dia
            val deArray = root.optJSONArray("dailyExpenses") ?: JSONArray()
            for (i in 0 until deArray.length()) {
                try {
                    val deObj = deArray.optJSONObject(i) ?: continue
                    val name = deObj.optString("name", "")
                    if (name.isNotBlank()) {
                        val id = deObj.optString("id", UUID.randomUUID().toString())
                        val value = deObj.optDouble("value", 0.0)
                        val date = deObj.optString("date", "")
                        val sourceStr = deObj.optString("source", DataSource.MANUAL.name)
                        val pmStr = deObj.optString("paymentMethod", PaymentMethod.CONTA.name)
                        val observation = deObj.optString("observation", null)
                        val reminderId = deObj.optString("reminderId", null)
                        val createdAt = deObj.optString("createdAt", null)
                        val updatedAt = deObj.optString("updatedAt", null)

                        dailyExpenses.add(
                            DailyExpense(
                                id = id,
                                name = name,
                                value = value,
                                date = date,
                                source = DataSource.fromString(sourceStr),
                                paymentMethod = PaymentMethod.fromString(pmStr),
                                observation = observation,
                                reminderId = reminderId,
                                createdAt = createdAt,
                                updatedAt = updatedAt
                            )
                        )
                    }
                } catch (_: Exception) {}
            }

            // Extração defensiva de lembretes
            val remArray = root.optJSONArray("reminders") ?: JSONArray()
            for (i in 0 until remArray.length()) {
                try {
                    val rObj = remArray.optJSONObject(i) ?: continue
                    val name = rObj.optString("name", "")
                    if (name.isNotBlank()) {
                        val id = rObj.optString("id", UUID.randomUUID().toString())
                        val value = rObj.optDouble("value", 0.0)
                        val rawDate = rObj.optString("date", "")
                        val normalizedDate = ReminderSyncUtils.normalizeDate(rawDate)
                        val isPaid = rObj.optBoolean("isPaid", false)
                        val isRecurring = rObj.optBoolean("isRecurring", false)
                        val recurrenceFrequency = rObj.optString("recurrenceFrequency", "MONTHLY")
                        val totalOccurrences = rObj.optInt("totalOccurrences", 1)
                        val currentOccurrence = rObj.optInt("currentOccurrence", 1)
                        val paymentMethod = if (rObj.has("paymentMethod")) rObj.optString("paymentMethod", null) else null
                        val recurrenceGroupId = if (rObj.has("recurrenceGroupId")) rObj.optString("recurrenceGroupId", null) else null
                        val notifyOnDueDate = rObj.optBoolean("notifyOnDueDate", true)
                        val notifyOneDayBefore = rObj.optBoolean("notifyOneDayBefore", false)
                        val createdAt = rObj.optString("createdAt", null)
                        val updatedAt = rObj.optString("updatedAt", null)

                        reminders.add(
                            ReminderItem(
                                id = id,
                                name = name,
                                value = value,
                                date = normalizedDate,
                                isPaid = isPaid,
                                isRecurring = isRecurring,
                                recurrenceFrequency = recurrenceFrequency,
                                totalOccurrences = totalOccurrences,
                                currentOccurrence = currentOccurrence,
                                paymentMethod = paymentMethod,
                                recurrenceGroupId = recurrenceGroupId,
                                notifyOnDueDate = notifyOnDueDate,
                                notifyOneDayBefore = notifyOneDayBefore,
                                createdAt = createdAt,
                                updatedAt = updatedAt
                            )
                        )
                    }
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {
            // Caso a raiz do JSON esteja inacessível
        }

        val totalRecovered = cards.size + purchases.size + subscriptions.size + dailyExpenses.size + reminders.size
        val details = if (totalRecovered > 0) {
            "Recuperados com sucesso: ${cards.size} cartões, ${purchases.size} compras, ${subscriptions.size} assinaturas, ${dailyExpenses.size} gastos diários, ${reminders.size} lembretes."
        } else {
            "Não foi possível recuperar dados legíveis do arquivo."
        }

        return RecoveryResult(
            data = StorageData(
                version = CURRENT_VERSION,
                cards = cards,
                purchases = purchases,
                subscriptions = subscriptions,
                cardPayments = cardPayments,
                dailyExpenses = dailyExpenses,
                reminders = reminders
            ),
            recoveredCount = totalRecovered,
            details = details
        )
    }
}
