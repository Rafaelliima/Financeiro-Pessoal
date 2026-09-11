package com.example.data

import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter

object AtomicFileWriter {

    /**
     * Grava com segurança atômica os dados em um arquivo temporário (.tmp),
     * valida a integridade da gravação e substitui o arquivo principal (.json)
     * somente após a validação bem-sucedida.
     */
    fun writeAtomic(targetFile: File, data: StorageData): StorageOperationResult<Boolean> {
        val tempFile = File(targetFile.parentFile, "${targetFile.name}.tmp")

        return try {
            val rootObject = JSONObject().apply {
                put("version", data.version)

                // Cards
                val cardsArray = JSONArray()
                data.cards.forEach { card ->
                    cardsArray.put(JSONObject().apply {
                        put("id", card.id)
                        put("name", card.name)
                        card.colorHex?.let { put("colorHex", it) }
                        card.bankId?.let { put("bankId", it) }
                        put("source", card.source.name)
                        card.createdAt?.let { put("createdAt", it) }
                        card.updatedAt?.let { put("updatedAt", it) }
                    })
                }
                put("cards", cardsArray)

                // Purchases
                val purchasesArray = JSONArray()
                data.purchases.forEach { purchase ->
                    purchasesArray.put(JSONObject().apply {
                        put("id", purchase.id)
                        put("name", purchase.name)
                        put("totalAmount", purchase.totalAmount)
                        put("cardId", purchase.cardId)
                        put("cardName", purchase.cardName)
                        put("isInstallment", purchase.isInstallment)
                        put("totalInstallments", purchase.totalInstallments)
                        put("startMonth", purchase.startMonth)
                        put("startYear", purchase.startYear)
                        put("paidInstallmentsCount", purchase.paidInstallmentsCount)
                        put("isQuitada", purchase.isQuitada)
                        purchase.completedAt?.let { put("completedAt", it) }
                        put("source", purchase.source.name)
                        purchase.createdAt?.let { put("createdAt", it) }
                        purchase.updatedAt?.let { put("updatedAt", it) }
                    })
                }
                put("purchases", purchasesArray)

                // Subscriptions
                val subscriptionsArray = JSONArray()
                data.subscriptions.forEach { sub ->
                    subscriptionsArray.put(JSONObject().apply {
                        put("id", sub.id)
                        put("name", sub.name)
                        put("monthlyValue", sub.monthlyValue)
                        put("cardId", sub.cardId)
                        put("cardName", sub.cardName)
                        put("source", sub.source.name)
                        put("isShared", sub.isShared)
                        sub.sharedWith?.let { put("sharedWith", it) }
                        put("receivedAmount", sub.receivedAmount)
                        sub.createdAt?.let { put("createdAt", it) }
                        sub.updatedAt?.let { put("updatedAt", it) }
                    })
                }
                put("subscriptions", subscriptionsArray)

                // Card Payments
                val cardPaymentsArray = JSONArray()
                data.cardPayments.forEach { cp ->
                    cardPaymentsArray.put(JSONObject().apply {
                        put("id", cp.id)
                        put("cardId", cp.cardId)
                        put("month", cp.month)
                        put("year", cp.year)
                        put("paid", cp.paid)
                        cp.paidAt?.let { put("paidAt", it) }
                    })
                }
                put("cardPayments", cardPaymentsArray)

                // Daily Expenses
                val dailyExpensesArray = JSONArray()
                data.dailyExpenses.forEach { de ->
                    dailyExpensesArray.put(JSONObject().apply {
                        put("id", de.id)
                        put("name", de.name)
                        put("value", de.value)
                        put("date", de.date)
                        put("source", de.source.name)
                        put("paymentMethod", de.paymentMethod.name)
                        de.observation?.let { put("observation", it) }
                        de.reminderId?.let { put("reminderId", it) }
                        de.createdAt?.let { put("createdAt", it) }
                        de.updatedAt?.let { put("updatedAt", it) }
                    })
                }
                put("dailyExpenses", dailyExpensesArray)

                // Conta Google (usada apenas para backup/sincronização na nuvem via Firestore)
                data.googleAccount?.let { acc ->
                    put("googleAccount", JSONObject().apply {
                        put("isConnected", acc.isConnected)
                        acc.userEmail?.let { put("userEmail", it) }
                        acc.userName?.let { put("userName", it) }
                        acc.photoUrl?.let { put("photoUrl", it) }
                        acc.connectedAt?.let { put("connectedAt", it) }
                        acc.accountId?.let { put("accountId", it) }
                        acc.idToken?.let { put("idToken", it) }
                    })
                }

                put("reminders", JSONArray().apply {
                    data.reminders.forEach { r ->
                        put(JSONObject().apply {
                            put("id", r.id)
                            put("name", r.name)
                            put("value", r.value)
                            put("date", r.date)
                            put("isPaid", r.isPaid)
                            put("isRecurring", r.isRecurring)
                            put("recurrenceFrequency", r.recurrenceFrequency)
                            put("totalOccurrences", r.totalOccurrences)
                            put("currentOccurrence", r.currentOccurrence)
                            r.paymentMethod?.let { put("paymentMethod", it) }
                            r.recurrenceGroupId?.let { put("recurrenceGroupId", it) }
                            put("notifyOnDueDate", r.notifyOnDueDate)
                            put("notifyOneDayBefore", r.notifyOneDayBefore)
                            r.createdAt?.let { put("createdAt", it) }
                            r.updatedAt?.let { put("updatedAt", it) }
                        })
                    }
                })

                // Theme
                put("themeMode", data.themeMode.name)
                put("selectedPalette", data.selectedPalette)

                // Coleções de expansão futura
                put("categories", JSONArray())
                put("accounts", JSONArray())
                put("investments", JSONArray())
                put("goals", JSONArray())
            }

            val jsonContent = rootObject.toString(2)

            // 2. Grava no arquivo temporário (.tmp)
            FileWriter(tempFile, false).use { writer ->
                writer.write(jsonContent)
            }

            // 3. Valida a gravação do arquivo temporário
            if (!tempFile.exists() || tempFile.length() == 0L) {
                if (tempFile.exists()) tempFile.delete()
                return StorageOperationResult(
                    data = false,
                    isSuccess = false,
                    message = "Falha ao validar gravação no arquivo temporário."
                )
            }

            val verifyString = tempFile.readText().trim()
            val verifyJson = JSONObject(verifyString)
            if (!verifyJson.has("version") || !verifyJson.has("cards")) {
                tempFile.delete()
                return StorageOperationResult(
                    data = false,
                    isSuccess = false,
                    message = "Estrutura do arquivo temporário inconsistente."
                )
            }

            // 4. Substituição atômica segura do arquivo principal
            val replaced = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                try {
                    java.nio.file.Files.move(
                        tempFile.toPath(),
                        targetFile.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                        java.nio.file.StandardCopyOption.ATOMIC_MOVE
                    )
                    true
                } catch (_: Exception) {
                    if (targetFile.exists()) targetFile.delete()
                    tempFile.renameTo(targetFile)
                }
            } else {
                if (targetFile.exists()) targetFile.delete()
                tempFile.renameTo(targetFile)
            }

            if (replaced && targetFile.exists()) {
                StorageOperationResult(
                    data = true,
                    isSuccess = true,
                    message = "Dados v${data.version} salvos com sucesso (gravação atômica)."
                )
            } else {
                StorageOperationResult(
                    data = false,
                    isSuccess = false,
                    message = "Não foi possível renomear o arquivo temporário para o destino final."
                )
            }
        } catch (e: Exception) {
            if (tempFile.exists()) {
                tempFile.delete()
            }
            StorageOperationResult(
                data = false,
                isSuccess = false,
                message = "Erro durante a gravação atômica: ${e.localizedMessage ?: "Falha de E/S"}"
            )
        }
    }
}
