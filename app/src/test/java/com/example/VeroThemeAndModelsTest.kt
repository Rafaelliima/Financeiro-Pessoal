package com.example

import com.example.data.AtomicFileWriter
import com.example.data.DailyExpense
import com.example.data.DataSource
import com.example.data.DataSyncUtils
import com.example.data.GoogleAccountData
import com.example.data.PaymentMethod
import com.example.data.PurchaseItem
import com.example.data.StorageData
import com.example.data.StorageOperationResult
import com.example.data.StorageReader
import com.example.data.SubscriptionItem
import com.example.ui.theme.VeroThemePalette
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class VeroThemeAndModelsTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testVeroThemePalette_EnumAndFromId() {
        assertEquals(4, VeroThemePalette.entries.size)
        assertEquals("Preto", VeroThemePalette.OBSIDIAN.displayName)
        assertEquals("Branco", VeroThemePalette.SNOW.displayName)
        assertEquals("Azul", VeroThemePalette.MIDNIGHT.displayName)
        assertEquals("Verde", VeroThemePalette.EMERALD.displayName)

        assertEquals(VeroThemePalette.OBSIDIAN, VeroThemePalette.fromId("OBSIDIAN"))
        assertEquals(VeroThemePalette.MIDNIGHT, VeroThemePalette.fromId("midnight"))
        assertEquals(VeroThemePalette.EMERALD, VeroThemePalette.fromId("EMERALD"))
        assertEquals(VeroThemePalette.SNOW, VeroThemePalette.fromId("snow"))
        assertEquals(VeroThemePalette.OBSIDIAN, VeroThemePalette.fromId("INVALID"))
        assertEquals(VeroThemePalette.OBSIDIAN, VeroThemePalette.fromId(null))
    }

    @Test
    fun testSelectedPalette_StorageDataPersistence() {
        val testFile = File(tempFolder.root, "test_data.json")
        val storageData = StorageData(
            selectedPalette = "EMERALD"
        )

        // Write
        val writeResult = AtomicFileWriter.writeAtomic(testFile, storageData)
        assertTrue(writeResult.isSuccess)

        // Read
        val readResult = StorageReader.readStorage(testFile) {
            StorageOperationResult(StorageData(), true, "created")
        }
        assertTrue(readResult.isSuccess)
        assertNotNull(readResult.data)
        assertEquals("EMERALD", readResult.data?.selectedPalette)
    }

    @Test
    fun testSubscriptionItem_FirestoreAnnotations() {
        val propNameGetter = SubscriptionItem::class.java.getMethod("isShared")
            .getAnnotation(PropertyName::class.java)
        assertNotNull("Getter isShared must have @PropertyName", propNameGetter)
        assertEquals("isShared", propNameGetter?.value)

        val excludeGetter = SubscriptionItem::class.java.getMethod("getEffectiveMonthlyValue")
            .getAnnotation(Exclude::class.java)
        assertNotNull("getEffectiveMonthlyValue must have @Exclude", excludeGetter)
    }

    @Test
    fun testPurchaseItem_EffectiveInstallmentDisplay_PortugueseMonthNames() {
        val cal = Calendar.getInstance()
        val curM = cal.get(Calendar.MONTH) + 1
        val curY = cal.get(Calendar.YEAR)
        val months = listOf("", "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro")

        val purchase = PurchaseItem(
            name = "Notebook",
            totalAmount = 1200.0,
            isInstallment = true,
            totalInstallments = 12,
            startMonth = curM,
            startYear = curY,
            paidInstallmentsCount = 0
        )

        val (inst, monthName) = purchase.getEffectiveInstallmentDisplay(isInvoicePaid = false)
        assertEquals(1, inst)
        assertEquals(months[curM], monthName)

        val (instPaid, monthNamePaid) = purchase.getEffectiveInstallmentDisplay(isInvoicePaid = true)
        assertEquals(2, instPaid)
        val nextMonthIndex = if (curM == 12) 1 else curM + 1
        assertEquals(months[nextMonthIndex], monthNamePaid)
    }

    @Test
    fun testDailyExpense_ReminderIdPersistence() {
        val testFile = File(tempFolder.root, "test_daily_expense.json")
        val expense = DailyExpense(
            id = "exp-1",
            name = "Energia Elétrica",
            value = 145.50,
            date = "2026-09-15",
            source = DataSource.MANUAL,
            paymentMethod = PaymentMethod.CONTA,
            observation = "Pago via lembrete",
            reminderId = "rem-123"
        )
        val storageData = StorageData(
            dailyExpenses = listOf(expense)
        )

        // Write
        val writeResult = AtomicFileWriter.writeAtomic(testFile, storageData)
        assertTrue(writeResult.isSuccess)

        // Read
        val readResult = StorageReader.readStorage(testFile) {
            StorageOperationResult(StorageData(), true, "created")
        }
        assertTrue(readResult.isSuccess)
        assertNotNull(readResult.data)
        assertEquals(1, readResult.data?.dailyExpenses?.size)
        val readExpense = readResult.data?.dailyExpenses?.first()
        assertEquals("exp-1", readExpense?.id)
        assertEquals("Energia Elétrica", readExpense?.name)
        assertEquals(145.50, readExpense?.value ?: 0.0, 0.001)
        assertEquals("rem-123", readExpense?.reminderId)
        assertEquals(PaymentMethod.CONTA, readExpense?.paymentMethod)
    }

    @Test
    fun testDataSyncUtils_PreserveProfileInfo() {
        val localAccount = GoogleAccountData(
            accountId = "user-1",
            userEmail = "user@example.com",
            userName = "Google Name",
            customDisplayName = "Nome Personalizado",
            customPhotoPath = "/data/user/0/com.example/files/avatar.jpg"
        )
        val localData = StorageData(googleAccount = localAccount)

        val cloudAccount = GoogleAccountData(
            accountId = "user-1",
            userEmail = "user@example.com",
            userName = "Google Name",
            customDisplayName = null,
            customPhotoPath = null
        )
        val cloudData = StorageData(googleAccount = cloudAccount)

        val sessionAccount = GoogleAccountData(
            accountId = "user-1",
            userEmail = "user@example.com",
            userName = "Google Name"
        )

        val (mergedData, _) = DataSyncUtils.safeMergeStorageData(
            localData = localData,
            cloudData = cloudData,
            account = sessionAccount
        )

        assertEquals("Nome Personalizado", mergedData.googleAccount?.customDisplayName)
        assertEquals("/data/user/0/com.example/files/avatar.jpg", mergedData.googleAccount?.customPhotoPath)
    }
}
