package com.example

import com.example.data.*
import org.junit.Assert.*
import org.junit.Test

class DataSyncUtilsTest {

    @Test
    fun testSafeMergeCards_PreservesOfflineCards() {
        val cloudCard = CardItem(id = "card-1", name = "Nubank")
        val localCard = CardItem(id = "card-2", name = "Inter")

        val (merged, shouldUpload) = DataSyncUtils.safeMergeCards(
            localCards = listOf(cloudCard, localCard),
            cloudCards = listOf(cloudCard)
        )

        assertEquals(2, merged.size)
        assertTrue(merged.any { it.id == "card-1" })
        assertTrue(merged.any { it.id == "card-2" })
        assertTrue("Deve solicitar upload porque há cartão local novo", shouldUpload)
    }

    @Test
    fun testSafeMergePurchases_ConflictResolution_PreservesAdvancedState() {
        val cloudPurchase = PurchaseItem(
            id = "purch-1",
            name = "Notebook",
            totalAmount = 3000.0,
            isInstallment = true,
            totalInstallments = 10,
            paidInstallmentsCount = 3,
            isQuitada = false
        )

        val localPurchase = PurchaseItem(
            id = "purch-1",
            name = "Notebook",
            totalAmount = 3000.0,
            isInstallment = true,
            totalInstallments = 10,
            paidInstallmentsCount = 5,
            isQuitada = true,
            completedAt = "2026-05-10"
        )

        val (merged, shouldUpload) = DataSyncUtils.safeMergePurchases(
            localPurchases = listOf(localPurchase),
            cloudPurchases = listOf(cloudPurchase)
        )

        assertEquals(1, merged.size)
        val p = merged.first()
        assertEquals(5, p.paidInstallmentsCount)
        assertTrue(p.isQuitada)
        assertEquals("2026-05-10", p.completedAt)
        assertTrue("Deve atualizar nuvem com o progresso mais avançado", shouldUpload)
    }

    @Test
    fun testSafeMergePurchases_PreservesOfflineCreatedPurchase() {
        val cloudPurchase = PurchaseItem(id = "p-cloud", name = "Mercado")
        val localPurchase = PurchaseItem(id = "p-local", name = "Farmácia")

        val (merged, shouldUpload) = DataSyncUtils.safeMergePurchases(
            localPurchases = listOf(cloudPurchase, localPurchase),
            cloudPurchases = listOf(cloudPurchase)
        )

        assertEquals(2, merged.size)
        assertTrue(merged.any { it.id == "p-cloud" })
        assertTrue(merged.any { it.id == "p-local" })
        assertTrue(shouldUpload)
    }

    @Test
    fun testSafeMergeSubscriptions_PreservesOfflineSubscriptions() {
        val cloudSub = SubscriptionItem(id = "sub-1", name = "Netflix", monthlyValue = 55.90)
        val localSub = SubscriptionItem(id = "sub-2", name = "Spotify", monthlyValue = 21.90)

        val (merged, shouldUpload) = DataSyncUtils.safeMergeSubscriptions(
            localSubs = listOf(cloudSub, localSub),
            cloudSubs = listOf(cloudSub)
        )

        assertEquals(2, merged.size)
        assertTrue(merged.any { it.id == "sub-2" })
        assertTrue(shouldUpload)
    }

    @Test
    fun testSafeMergeDailyExpenses_PreservesOfflineExpenses() {
        val cloudExp = DailyExpense(id = "exp-1", name = "Almoço", value = 35.0)
        val localExp = DailyExpense(id = "exp-2", name = "Café", value = 8.50)

        val (merged, shouldUpload) = DataSyncUtils.safeMergeDailyExpenses(
            localExpenses = listOf(cloudExp, localExp),
            cloudExpenses = listOf(cloudExp)
        )

        assertEquals(2, merged.size)
        assertTrue(merged.any { it.id == "exp-2" })
        assertTrue(shouldUpload)
    }

    @Test
    fun testSafeMergeCardPayments_MergesAndPreservesPayments() {
        val cloudPay = CardPaymentItem(id = "pay-1", cardId = "card-1", month = 7, year = 2026, paid = false)
        val localPay = CardPaymentItem(id = "pay-1", cardId = "card-1", month = 7, year = 2026, paid = true)

        val (merged, shouldUpload) = DataSyncUtils.safeMergeCardPayments(
            localPayments = listOf(localPay),
            cloudPayments = listOf(cloudPay)
        )

        assertEquals(1, merged.size)
        assertTrue("Deve preservar o status pago = true da versão local", merged.first().paid)
        assertTrue(shouldUpload)
    }

    @Test
    fun testSafeMergeStorageData_FullConsolidation() {
        val localData = StorageData(
            cards = listOf(CardItem(id = "c1", name = "Nubank"), CardItem(id = "c2", name = "Offline Card")),
            purchases = listOf(PurchaseItem(id = "p1", name = "Offline Purchase")),
            subscriptions = listOf(SubscriptionItem(id = "s1", name = "Offline Sub")),
            dailyExpenses = listOf(DailyExpense(id = "e1", name = "Offline Expense")),
            cardPayments = emptyList(),
            reminders = listOf(ReminderItem(id = "r1", name = "Offline Reminder", date = "2026-10-15"))
        )

        val cloudData = StorageData(
            cards = listOf(CardItem(id = "c1", name = "Nubank")),
            purchases = emptyList(),
            subscriptions = emptyList(),
            dailyExpenses = emptyList(),
            cardPayments = emptyList(),
            reminders = emptyList()
        )

        val account = GoogleAccountData(accountId = "user-123", userEmail = "test@example.com")
        val (merged, shouldUpload) = DataSyncUtils.safeMergeStorageData(localData, cloudData, account)

        assertEquals(2, merged.cards.size)
        assertEquals(1, merged.purchases.size)
        assertEquals(1, merged.subscriptions.size)
        assertEquals(1, merged.dailyExpenses.size)
        assertEquals(1, merged.reminders.size)
        assertEquals("user-123", merged.googleAccount?.accountId)
        assertTrue(shouldUpload)
    }
}
