package com.example.data

import org.json.JSONArray
import org.json.JSONObject

object DataMigrator {

    /**
     * Examina o JSONObject lido do arquivo e aplica migrações sequenciais
     * para trazê-lo até a CURRENT_VERSION (v2), garantindo a preservação total dos dados do usuário.
     */
    fun migrateIfNeeded(jsonObject: JSONObject): JSONObject {
        var fileVersion = jsonObject.optInt("version", 0)
        var currentObject = jsonObject

        if (fileVersion < 1) {
            currentObject = migrateV0ToV1(currentObject)
            fileVersion = 1
        }

        if (fileVersion < 2) {
            currentObject = migrateV1ToV2(currentObject)
            fileVersion = 2
        }

        if (fileVersion < 3) {
            currentObject = migrateV2ToV3(currentObject)
            fileVersion = 3
        }

        if (fileVersion < 4) {
            currentObject = migrateV3ToV4(currentObject)
            fileVersion = 4
        }

        if (fileVersion < 5) {
            currentObject = migrateV4ToV5(currentObject)
            fileVersion = 5
        }

        if (fileVersion < 6) {
            currentObject = migrateV5ToV6(currentObject)
            fileVersion = 6
        }

        if (fileVersion < 7) {
            currentObject = migrateV6ToV7(currentObject)
            fileVersion = 7
        }

        if (fileVersion < 8) {
            currentObject = migrateV7ToV8(currentObject)
            fileVersion = 8
        }

        if (fileVersion < 9) {
            currentObject = migrateV8ToV9(currentObject)
            fileVersion = 9
        }

        if (fileVersion < 10) {
            currentObject = migrateV9ToV10(currentObject)
            fileVersion = 10
        }

        return currentObject
    }

    private fun migrateV0ToV1(oldJson: JSONObject): JSONObject {
        val migratedJson = JSONObject(oldJson.toString())
        migratedJson.put("version", 1)

        if (!migratedJson.has("cards")) {
            migratedJson.put("cards", JSONArray())
        }
        if (!migratedJson.has("purchases")) {
            migratedJson.put("purchases", JSONArray())
        }
        if (!migratedJson.has("subscriptions")) {
            migratedJson.put("subscriptions", JSONArray())
        }

        return migratedJson
    }

    private fun migrateV1ToV2(oldJson: JSONObject): JSONObject {
        val migratedJson = JSONObject(oldJson.toString())
        migratedJson.put("version", 2)

        // Adiciona a origem "MANUAL" em cartões se inexistente
        val cardsArray = migratedJson.optJSONArray("cards") ?: JSONArray()
        for (i in 0 until cardsArray.length()) {
            val item = cardsArray.optJSONObject(i)
            if (item != null && !item.has("source")) {
                item.put("source", DataSource.MANUAL.name)
            }
        }
        migratedJson.put("cards", cardsArray)

        // Adiciona a origem "MANUAL" em compras se inexistente
        val purchasesArray = migratedJson.optJSONArray("purchases") ?: JSONArray()
        for (i in 0 until purchasesArray.length()) {
            val item = purchasesArray.optJSONObject(i)
            if (item != null && !item.has("source")) {
                item.put("source", DataSource.MANUAL.name)
            }
        }
        migratedJson.put("purchases", purchasesArray)

        // Adiciona a origem "MANUAL" em assinaturas se inexistente
        val subsArray = migratedJson.optJSONArray("subscriptions") ?: JSONArray()
        for (i in 0 until subsArray.length()) {
            val item = subsArray.optJSONObject(i)
            if (item != null && !item.has("source")) {
                item.put("source", DataSource.MANUAL.name)
            }
        }
        migratedJson.put("subscriptions", subsArray)

        // Garante suporte às coleções de expansão futura
        val futureCollections = listOf("dailyExpenses", "categories", "accounts", "investments", "goals")
        futureCollections.forEach { key ->
            if (!migratedJson.has(key)) {
                migratedJson.put(key, JSONArray())
            }
        }

        return migratedJson
    }

    private fun migrateV2ToV3(oldJson: JSONObject): JSONObject {
        val migratedJson = JSONObject(oldJson.toString())
        migratedJson.put("version", 3)

        if (!migratedJson.has("cardPayments")) {
            migratedJson.put("cardPayments", JSONArray())
        }

        // Garante campos cardId e cardName nas assinaturas
        val subsArray = migratedJson.optJSONArray("subscriptions") ?: JSONArray()
        for (i in 0 until subsArray.length()) {
            val item = subsArray.optJSONObject(i)
            if (item != null) {
                if (!item.has("cardId")) item.put("cardId", "")
                if (!item.has("cardName")) item.put("cardName", "")
            }
        }
        migratedJson.put("subscriptions", subsArray)

        // Garante paidInstallmentsCount e isQuitada nas compras
        val purchasesArray = migratedJson.optJSONArray("purchases") ?: JSONArray()
        for (i in 0 until purchasesArray.length()) {
            val item = purchasesArray.optJSONObject(i)
            if (item != null) {
                if (!item.has("paidInstallmentsCount")) item.put("paidInstallmentsCount", 0)
                if (!item.has("isQuitada")) item.put("isQuitada", false)
            }
        }
        migratedJson.put("purchases", purchasesArray)

        return migratedJson
    }

    private fun migrateV3ToV4(oldJson: JSONObject): JSONObject {
        val migratedJson = JSONObject(oldJson.toString())
        migratedJson.put("version", 4)

        if (!migratedJson.has("dailyExpenses")) {
            migratedJson.put("dailyExpenses", JSONArray())
        }

        val expensesArray = migratedJson.optJSONArray("dailyExpenses") ?: JSONArray()
        for (i in 0 until expensesArray.length()) {
            val item = expensesArray.optJSONObject(i)
            if (item != null && !item.has("source")) {
                item.put("source", DataSource.MANUAL.name)
            }
        }
        migratedJson.put("dailyExpenses", expensesArray)

        return migratedJson
    }

    private fun migrateV4ToV5(oldJson: JSONObject): JSONObject {
        val migratedJson = JSONObject(oldJson.toString())
        migratedJson.put("version", 5)

        val purchasesArray = migratedJson.optJSONArray("purchases") ?: JSONArray()
        for (i in 0 until purchasesArray.length()) {
            val item = purchasesArray.optJSONObject(i)
            if (item != null) {
                val isQuitada = item.optBoolean("isQuitada", false)
                val totalInst = item.optInt("totalInstallments", 1)
                val paidInst = item.optInt("paidInstallmentsCount", 0)

                if ((isQuitada || paidInst >= totalInst) && !item.has("completedAt")) {
                    val startYear = item.optInt("startYear", 2026)
                    val startMonth = item.optInt("startMonth", 1)
                    val endTotalMonths = startYear * 12 + (startMonth - 1) + (totalInst - 1)
                    val endYear = endTotalMonths / 12
                    val endMonth = (endTotalMonths % 12) + 1
                    val monthFormatted = if (endMonth < 10) "0$endMonth" else "$endMonth"
                    item.put("completedAt", "$endYear-$monthFormatted-01")
                }
            }
        }
        migratedJson.put("purchases", purchasesArray)

        return migratedJson
    }

    private fun migrateV5ToV6(oldJson: JSONObject): JSONObject {
        val migratedJson = JSONObject(oldJson.toString())
        migratedJson.put("version", 6)
        if (!migratedJson.has("googleAccount")) {
            migratedJson.put("googleAccount", JSONObject().apply {
                put("isConnected", false)
            })
        }
        return migratedJson
    }

    private fun migrateV6ToV7(oldJson: JSONObject): JSONObject {
        val migratedJson = JSONObject(oldJson.toString())
        migratedJson.put("version", 7)
        if (!migratedJson.has("emailSyncState")) {
            val syncJson = JSONObject().apply {
                put("processedMessageIds", JSONArray())
                put("totalEmailsFound", 0)
                put("totalEmailsProcessed", 0)
                put("scopeGranted", false)
                put("statusMessage", "Não testado")
            }
            migratedJson.put("emailSyncState", syncJson)
        }
        return migratedJson
    }

    private fun migrateV7ToV8(oldJson: JSONObject): JSONObject {
        val migratedJson = JSONObject(oldJson.toString())
        migratedJson.put("version", 8)
        val syncObj = migratedJson.optJSONObject("emailSyncState")
        if (syncObj != null) {
            if (!syncObj.has("totalImported")) syncObj.put("totalImported", 0)
            if (!syncObj.has("totalIgnored")) syncObj.put("totalIgnored", 0)
        }
        return migratedJson
    }

    private fun migrateV8ToV9(oldJson: JSONObject): JSONObject {
        val migratedJson = JSONObject(oldJson.toString())
        migratedJson.put("version", 9)
        val syncObj = migratedJson.optJSONObject("emailSyncState")
        if (syncObj != null && !syncObj.has("lastError")) {
            syncObj.put("lastError", JSONObject.NULL)
        }
        return migratedJson
    }

    private fun migrateV9ToV10(oldJson: JSONObject): JSONObject {
        val migratedJson = JSONObject(oldJson.toString())
        migratedJson.put("version", 10)
        val syncObj = migratedJson.optJSONObject("emailSyncState")
        if (syncObj != null && !syncObj.has("diagnosticLogs")) {
            syncObj.put("diagnosticLogs", JSONArray())
        }
        return migratedJson
    }
}
