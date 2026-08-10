package com.example.data

import com.example.ui.screens.CardItem
import com.example.ui.screens.PurchaseItem
import com.example.ui.screens.SubscriptionItem
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
                        val sourceStr = cObj.optString("source", DataSource.MANUAL.name)
                        val createdAt = cObj.optString("createdAt", null)
                        val updatedAt = cObj.optString("updatedAt", null)
                        cards.add(
                            CardItem(
                                id = id,
                                name = name,
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
                        val isInstallment = pObj.optBoolean("isInstallment", false)
                        val totalInstallments = pObj.optInt("totalInstallments", 1)
                        val startMonth = pObj.optInt("startMonth", 1)
                        val startYear = pObj.optInt("startYear", 2026)
                        val paidInstallmentsCount = pObj.optInt("paidInstallmentsCount", 0)
                        val isQuitada = pObj.optBoolean("isQuitada", false)
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

                        subscriptions.add(
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
                        val observation = deObj.optString("observation", null)
                        val createdAt = deObj.optString("createdAt", null)
                        val updatedAt = deObj.optString("updatedAt", null)

                        dailyExpenses.add(
                            DailyExpense(
                                id = id,
                                name = name,
                                value = value,
                                date = date,
                                source = DataSource.fromString(sourceStr),
                                observation = observation,
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

        val totalRecovered = cards.size + purchases.size + subscriptions.size + dailyExpenses.size
        val details = if (totalRecovered > 0) {
            "Recuperados com sucesso: ${cards.size} cartões, ${purchases.size} compras, ${subscriptions.size} assinaturas, ${dailyExpenses.size} gastos diários."
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
                dailyExpenses = dailyExpenses
            ),
            recoveredCount = totalRecovered,
            details = details
        )
    }
}
