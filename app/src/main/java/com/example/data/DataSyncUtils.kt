package com.example.data

import com.example.ui.theme.ThemeMode

object DataSyncUtils {

    /**
     * Mescla listas de cartões preservando todos os cadastrados em ambas as fontes (local e nuvem).
     * Se o cartão existir em ambos, preserva o local ou o mais recente.
     * Retorna a lista mesclada e se há dados novos locais a subir para a nuvem.
     */
    fun safeMergeCards(
        localCards: List<CardItem>,
        cloudCards: List<CardItem>
    ): Pair<List<CardItem>, Boolean> {
        if (localCards.isEmpty()) return Pair(cloudCards, false)
        if (cloudCards.isEmpty()) return Pair(localCards, true)

        val localMap = localCards.associateBy { it.id }
        val cloudMap = cloudCards.associateBy { it.id }
        val mergedList = mutableListOf<CardItem>()
        var missingInCloud = false

        for (cloudCard in cloudCards) {
            val localCard = localMap[cloudCard.id]
            if (localCard != null) {
                // Preserva o item local (ou pode priorizar updatedAt se disponível)
                mergedList.add(localCard)
            } else {
                mergedList.add(cloudCard)
            }
        }

        for (localCard in localCards) {
            if (!cloudMap.containsKey(localCard.id)) {
                mergedList.add(localCard)
                missingInCloud = true
            }
        }

        val shouldUpload = missingInCloud || (mergedList != cloudCards)
        return Pair(mergedList, shouldUpload)
    }

    /**
     * Mescla compras locais e da nuvem por ID único (UUID).
     * Se uma compra existir em ambos os lados, preserva a versão mais atualizada:
     * - Maior paidInstallmentsCount
     * - isQuitada == true tem precedência
     * - completedAt preenchido
     */
    fun safeMergePurchases(
        localPurchases: List<PurchaseItem>,
        cloudPurchases: List<PurchaseItem>
    ): Pair<List<PurchaseItem>, Boolean> {
        if (localPurchases.isEmpty()) return Pair(cloudPurchases, false)
        if (cloudPurchases.isEmpty()) return Pair(localPurchases, true)

        val localMap = localPurchases.associateBy { it.id }
        val cloudMap = cloudPurchases.associateBy { it.id }
        val mergedList = mutableListOf<PurchaseItem>()
        var missingInCloud = false

        for (cloudPurchase in cloudPurchases) {
            val localPurchase = localMap[cloudPurchase.id]
            if (localPurchase != null) {
                val isQuitada = cloudPurchase.isQuitada || localPurchase.isQuitada
                val maxPaidCount = maxOf(cloudPurchase.paidInstallmentsCount, localPurchase.paidInstallmentsCount)
                val completedAt = cloudPurchase.completedAt ?: localPurchase.completedAt

                // Escolhe base com maior progresso
                val base = if (localPurchase.paidInstallmentsCount >= cloudPurchase.paidInstallmentsCount) localPurchase else cloudPurchase
                val resolvedPurchase = base.copy(
                    isQuitada = isQuitada,
                    paidInstallmentsCount = maxPaidCount,
                    completedAt = completedAt
                )
                mergedList.add(resolvedPurchase)
            } else {
                mergedList.add(cloudPurchase)
            }
        }

        for (localPurchase in localPurchases) {
            if (!cloudMap.containsKey(localPurchase.id)) {
                mergedList.add(localPurchase)
                missingInCloud = true
            }
        }

        val shouldUpload = missingInCloud || (mergedList != cloudPurchases)
        return Pair(mergedList, shouldUpload)
    }

    /**
     * Mescla assinaturas locais e da nuvem por ID único.
     */
    fun safeMergeSubscriptions(
        localSubs: List<SubscriptionItem>,
        cloudSubs: List<SubscriptionItem>
    ): Pair<List<SubscriptionItem>, Boolean> {
        if (localSubs.isEmpty()) return Pair(cloudSubs, false)
        if (cloudSubs.isEmpty()) return Pair(localSubs, true)

        val localMap = localSubs.associateBy { it.id }
        val cloudMap = cloudSubs.associateBy { it.id }
        val mergedList = mutableListOf<SubscriptionItem>()
        var missingInCloud = false

        for (cloudSub in cloudSubs) {
            val localSub = localMap[cloudSub.id]
            if (localSub != null) {
                mergedList.add(localSub)
            } else {
                mergedList.add(cloudSub)
            }
        }

        for (localSub in localSubs) {
            if (!cloudMap.containsKey(localSub.id)) {
                mergedList.add(localSub)
                missingInCloud = true
            }
        }

        val shouldUpload = missingInCloud || (mergedList != cloudSubs)
        return Pair(mergedList, shouldUpload)
    }

    /**
     * Mescla gastos diários por ID único.
     */
    fun safeMergeDailyExpenses(
        localExpenses: List<DailyExpense>,
        cloudExpenses: List<DailyExpense>
    ): Pair<List<DailyExpense>, Boolean> {
        if (localExpenses.isEmpty()) return Pair(cloudExpenses, false)
        if (cloudExpenses.isEmpty()) return Pair(localExpenses, true)

        val localMap = localExpenses.associateBy { it.id }
        val cloudMap = cloudExpenses.associateBy { it.id }
        val mergedList = mutableListOf<DailyExpense>()
        var missingInCloud = false

        for (cloudExp in cloudExpenses) {
            val localExp = localMap[cloudExp.id]
            if (localExp != null) {
                mergedList.add(localExp)
            } else {
                mergedList.add(cloudExp)
            }
        }

        for (localExp in localExpenses) {
            if (!cloudMap.containsKey(localExp.id)) {
                mergedList.add(localExp)
                missingInCloud = true
            }
        }

        val shouldUpload = missingInCloud || (mergedList != cloudExpenses)
        return Pair(mergedList, shouldUpload)
    }

    /**
     * Mescla pagamentos de fatura de cartões (CardPaymentItem) por cardId + mês + ano.
     */
    fun safeMergeCardPayments(
        localPayments: List<CardPaymentItem>,
        cloudPayments: List<CardPaymentItem>
    ): Pair<List<CardPaymentItem>, Boolean> {
        if (localPayments.isEmpty()) return Pair(cloudPayments, false)
        if (cloudPayments.isEmpty()) return Pair(localPayments, true)

        fun paymentKey(p: CardPaymentItem) = "${p.cardId}_${p.month}_${p.year}"

        val localMap = localPayments.associateBy { paymentKey(it) }
        val cloudMap = cloudPayments.associateBy { paymentKey(it) }
        val mergedList = mutableListOf<CardPaymentItem>()
        var missingInCloud = false

        for (cloudPay in cloudPayments) {
            val localPay = localMap[paymentKey(cloudPay)]
            if (localPay != null) {
                val isPaid = cloudPay.paid || localPay.paid
                val paidAt = cloudPay.paidAt ?: localPay.paidAt
                mergedList.add(cloudPay.copy(paid = isPaid, paidAt = paidAt))
            } else {
                mergedList.add(cloudPay)
            }
        }

        for (localPay in localPayments) {
            if (!cloudMap.containsKey(paymentKey(localPay))) {
                mergedList.add(localPay)
                missingInCloud = true
            }
        }

        val shouldUpload = missingInCloud || (mergedList != cloudPayments)
        return Pair(mergedList, shouldUpload)
    }

    /**
     * Executa a mesclagem segura completa de todas as entidades de StorageData.
     * Retorna a versão consolidada e a indicação se uma sincronização para a nuvem deve ser disparada.
     */
    fun safeMergeStorageData(
        localData: StorageData,
        cloudData: StorageData,
        account: GoogleAccountData?
    ): Pair<StorageData, Boolean> {
        val (mergedCards, uploadCards) = safeMergeCards(localData.cards, cloudData.cards)
        val (mergedPurchases, uploadPurchases) = safeMergePurchases(localData.purchases, cloudData.purchases)
        val (mergedSubs, uploadSubs) = safeMergeSubscriptions(localData.subscriptions, cloudData.subscriptions)
        val (mergedExpenses, uploadExpenses) = safeMergeDailyExpenses(localData.dailyExpenses, cloudData.dailyExpenses)
        val (mergedPayments, uploadPayments) = safeMergeCardPayments(localData.cardPayments, cloudData.cardPayments)
        val (mergedReminders, uploadReminders) = ReminderSyncUtils.safeMergeReminders(localData.reminders, cloudData.reminders)

        val shouldUpload = uploadCards || uploadPurchases || uploadSubs || uploadExpenses || uploadPayments || uploadReminders

        val baseAccount = account ?: localData.googleAccount ?: cloudData.googleAccount
        val mergedAccount = baseAccount?.copy(
            customDisplayName = localData.googleAccount?.customDisplayName ?: cloudData.googleAccount?.customDisplayName ?: baseAccount.customDisplayName,
            customPhotoPath = localData.googleAccount?.customPhotoPath ?: cloudData.googleAccount?.customPhotoPath ?: baseAccount.customPhotoPath
        )

        val mergedStorageData = cloudData.copy(
            googleAccount = mergedAccount,
            cards = mergedCards,
            purchases = mergedPurchases,
            subscriptions = mergedSubs,
            dailyExpenses = mergedExpenses,
            cardPayments = mergedPayments,
            reminders = mergedReminders,
            selectedPalette = if (cloudData.selectedPalette.isNotBlank()) cloudData.selectedPalette else localData.selectedPalette,
            themeMode = if (cloudData.themeMode != ThemeMode.SYSTEM) cloudData.themeMode else localData.themeMode
        )

        return Pair(mergedStorageData, shouldUpload)
    }
}
