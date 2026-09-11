package com.example.data

object ReminderSyncUtils {

    /**
     * Converte datas legadas em formato dd/MM/yyyy para o formato padronizado yyyy-MM-dd.
     * Preserva datas já padronizadas ou outros formatos.
     */
    fun normalizeDate(dateStr: String): String {
        val trimmed = dateStr.trim()
        if (trimmed.contains("/")) {
            val parts = trimmed.split("/")
            if (parts.size == 3) {
                val day = parts[0].padStart(2, '0')
                val month = parts[1].padStart(2, '0')
                val year = parts[2]
                return "$year-$month-$day"
            }
        }
        return trimmed
    }

    /**
     * Extrai com segurança o ano e o mês de uma string de data,
     * suportando os formatos yyyy-MM-dd e dd/MM/yyyy.
     */
    fun parseYearMonth(dateStr: String): Pair<Int, Int>? {
        return try {
            val trimmed = dateStr.trim()
            if (trimmed.contains("-")) {
                val parts = trimmed.split("-")
                if (parts.size >= 2) {
                    val year = parts[0].toInt()
                    val month = parts[1].toInt()
                    Pair(year, month)
                } else null
            } else if (trimmed.contains("/")) {
                val parts = trimmed.split("/")
                if (parts.size >= 3) {
                    val month = parts[1].toInt()
                    val year = parts[2].toInt()
                    Pair(year, month)
                } else null
            } else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Executa um merge seguro e bidirecional de lembretes:
     * - Nunca descarta lembretes locais caso a nuvem esteja vazia ou com menos dados.
     * - Atualiza lembretes existentes respeitando o status de pagamento (isPaid).
     * - Normaliza datas legadas para o formato padrão yyyy-MM-dd.
     * - Sinaliza shouldUploadToCloud = true caso existam itens locais não presentes na nuvem
     *   ou alterações que precisem ser sincronizadas de volta ao Firestore.
     */
    fun safeMergeReminders(
        localReminders: List<ReminderItem>,
        cloudReminders: List<ReminderItem>
    ): Pair<List<ReminderItem>, Boolean> {
        val normalizedCloud = cloudReminders.map { it.copy(date = normalizeDate(it.date)) }
        val normalizedLocal = localReminders.map { it.copy(date = normalizeDate(it.date)) }

        if (normalizedLocal.isEmpty()) {
            return Pair(normalizedCloud, false)
        }
        if (normalizedCloud.isEmpty()) {
            return Pair(normalizedLocal, true)
        }

        val localMap = normalizedLocal.associateBy { it.id }
        val cloudMap = normalizedCloud.associateBy { it.id }
        val mergedList = mutableListOf<ReminderItem>()
        var missingInCloud = false

        // Adiciona itens da nuvem, mesclando com o estado local se aplicável
        for (cloudItem in normalizedCloud) {
            val localItem = localMap[cloudItem.id]
            if (localItem != null) {
                val isPaid = cloudItem.isPaid || localItem.isPaid
                mergedList.add(cloudItem.copy(isPaid = isPaid))
            } else {
                mergedList.add(cloudItem)
            }
        }

        // Preserva itens locais ausentes na nuvem
        for (localItem in normalizedLocal) {
            if (!cloudMap.containsKey(localItem.id)) {
                mergedList.add(localItem)
                missingInCloud = true
            }
        }

        val shouldUpload = missingInCloud || (mergedList != cloudReminders)
        return Pair(mergedList, shouldUpload)
    }
}
