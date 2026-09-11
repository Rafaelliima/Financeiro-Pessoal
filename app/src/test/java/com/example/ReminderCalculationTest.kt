package com.example

import com.example.data.ReminderItem
import com.example.data.ReminderSyncUtils
import com.example.data.StorageData
import com.example.data.StorageOperationResult
import com.example.data.StorageReader
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ReminderCalculationTest {

    @Test
    fun testSingleReminderCreation() {
        val reminder = ReminderItem(
            name = "Fatura Internet",
            value = 119.90,
            date = "2026-09-15",
            isRecurring = false,
            paymentMethod = "Pix",
            notifyOnDueDate = true,
            notifyOneDayBefore = true
        )

        assertFalse(reminder.isRecurring)
        assertEquals("Pix", reminder.paymentMethod)
        assertEquals(1, reminder.totalOccurrences)
        assertEquals(1, reminder.currentOccurrence)
        assertTrue(reminder.notifyOnDueDate)
        assertTrue(reminder.notifyOneDayBefore)
        assertFalse(reminder.isPaid)
    }

    @Test
    fun testRecurringMonthlyOccurrencesProjection() {
        val startDateStr = "10/09/2026"
        val sdfInput = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val sdfOutput = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val baseDate = sdfInput.parse(startDateStr)!!

        val occurrences = mutableListOf<String>()
        val cal = Calendar.getInstance().apply { time = baseDate }
        val reps = 3

        for (i in 1..reps) {
            occurrences.add(sdfOutput.format(cal.time))
            cal.add(Calendar.MONTH, 1)
        }

        assertEquals(3, occurrences.size)
        assertEquals("2026-09-10", occurrences[0])
        assertEquals("2026-10-10", occurrences[1])
        assertEquals("2026-11-10", occurrences[2])
    }

    @Test
    fun testReminderJsonSerializationAndDeserialization() {
        val original = ReminderItem(
            id = "rem-abc",
            name = "Aluguel",
            value = 1800.0,
            date = "2026-10-05",
            isPaid = false,
            isRecurring = true,
            recurrenceFrequency = "MONTHLY",
            totalOccurrences = 12,
            currentOccurrence = 2,
            paymentMethod = "Pix",
            recurrenceGroupId = "group-xyz",
            notifyOnDueDate = true,
            notifyOneDayBefore = true
        )

        val json = JSONObject().apply {
            put("id", original.id)
            put("name", original.name)
            put("value", original.value)
            put("date", original.date)
            put("isPaid", original.isPaid)
            put("isRecurring", original.isRecurring)
            put("recurrenceFrequency", original.recurrenceFrequency)
            put("totalOccurrences", original.totalOccurrences)
            put("currentOccurrence", original.currentOccurrence)
            put("paymentMethod", original.paymentMethod)
            put("recurrenceGroupId", original.recurrenceGroupId)
            put("notifyOnDueDate", original.notifyOnDueDate)
            put("notifyOneDayBefore", original.notifyOneDayBefore)
        }

        val restored = ReminderItem(
            id = json.optString("id", original.id),
            name = json.optString("name", original.name),
            value = json.optDouble("value", 0.0),
            date = json.optString("date", ""),
            isPaid = json.optBoolean("isPaid", false),
            isRecurring = json.optBoolean("isRecurring", false),
            recurrenceFrequency = json.optString("recurrenceFrequency", "MONTHLY"),
            totalOccurrences = json.optInt("totalOccurrences", 1),
            currentOccurrence = json.optInt("currentOccurrence", 1),
            paymentMethod = if (json.has("paymentMethod")) json.optString("paymentMethod") else null,
            recurrenceGroupId = if (json.has("recurrenceGroupId")) json.optString("recurrenceGroupId") else null,
            notifyOnDueDate = json.optBoolean("notifyOnDueDate", true),
            notifyOneDayBefore = json.optBoolean("notifyOneDayBefore", false)
        )

        assertEquals(original.id, restored.id)
        assertEquals(original.name, restored.name)
        assertEquals(original.value, restored.value, 0.001)
        assertEquals("2026-10-05", restored.date)
        assertFalse(restored.isPaid)
        assertTrue(restored.isRecurring)
        assertEquals("MONTHLY", restored.recurrenceFrequency)
        assertEquals(12, restored.totalOccurrences)
        assertEquals(2, restored.currentOccurrence)
        assertEquals("Pix", restored.paymentMethod)
        assertEquals("group-xyz", restored.recurrenceGroupId)
        assertTrue(restored.notifyOnDueDate)
        assertTrue(restored.notifyOneDayBefore)
    }

    @Test
    fun testReminderSyncUtilsNormalizeDate() {
        assertEquals("2026-09-15", ReminderSyncUtils.normalizeDate("15/09/2026"))
        assertEquals("2026-03-05", ReminderSyncUtils.normalizeDate("5/3/2026"))
        assertEquals("2026-09-15", ReminderSyncUtils.normalizeDate("2026-09-15"))
        assertEquals("2026-09-15", ReminderSyncUtils.normalizeDate("  2026-09-15  "))
    }

    @Test
    fun testReminderSyncUtilsParseYearMonth() {
        assertEquals(Pair(2026, 9), ReminderSyncUtils.parseYearMonth("2026-09-15"))
        assertEquals(Pair(2026, 9), ReminderSyncUtils.parseYearMonth("15/09/2026"))
        assertEquals(Pair(2026, 3), ReminderSyncUtils.parseYearMonth("05/03/2026"))
        assertEquals(Pair(2026, 3), ReminderSyncUtils.parseYearMonth("5/3/2026"))
        assertNull(ReminderSyncUtils.parseYearMonth("invalid-date"))
    }

    @Test
    fun testSafeMergeRemindersPreservesLocalItems() {
        val local = listOf(
            ReminderItem(id = "r1", name = "Energia", value = 200.0, date = "15/09/2026", isPaid = false)
        )
        val cloud = emptyList<ReminderItem>()

        val (merged, shouldUpload) = ReminderSyncUtils.safeMergeReminders(local, cloud)

        assertEquals(1, merged.size)
        assertEquals("r1", merged[0].id)
        assertEquals("2026-09-15", merged[0].date)
        assertTrue("Deve solicitar upload para a nuvem quando itens locais faltam na nuvem", shouldUpload)
    }

    @Test
    fun testSafeMergeRemindersUpdatesPaymentStatus() {
        val local = listOf(
            ReminderItem(id = "r1", name = "Energia", value = 200.0, date = "2026-09-15", isPaid = true),
            ReminderItem(id = "r2", name = "Internet", value = 100.0, date = "2026-09-20", isPaid = false)
        )
        val cloud = listOf(
            ReminderItem(id = "r1", name = "Energia", value = 200.0, date = "2026-09-15", isPaid = false),
            ReminderItem(id = "r3", name = "Aluguel", value = 1500.0, date = "10/10/2026", isPaid = false)
        )

        val (merged, shouldUpload) = ReminderSyncUtils.safeMergeReminders(local, cloud)

        assertEquals(3, merged.size)
        val mergedR1 = merged.find { it.id == "r1" }!!
        val mergedR2 = merged.find { it.id == "r2" }!!
        val mergedR3 = merged.find { it.id == "r3" }!!

        assertTrue("r1 deve ser marcado como pago", mergedR1.isPaid)
        assertFalse(mergedR2.isPaid)
        assertEquals("2026-10-10", mergedR3.date)
        assertTrue("Deve solicitar upload devido ao novo item r2 e atualização de r1", shouldUpload)
    }

    @Test
    fun testStorageReaderDefensiveDeserializationOfLegacyReminder() {
        val legacyJson = """
            {
                "version": 1,
                "reminders": [
                    {
                        "id": "legacy-rem-1",
                        "name": "Conta de Água",
                        "value": 85.50,
                        "date": "18/09/2026",
                        "isPaid": false
                    }
                ]
            }
        """.trimIndent()

        val tempFile = File.createTempFile("legacy_test", ".json")
        try {
            tempFile.writeText(legacyJson)
            val result = StorageReader.readStorage(tempFile) { StorageOperationResult(StorageData(), true, "") }

            assertTrue(result.isSuccess)
            val reminders = result.data?.reminders
            assertEquals(1, reminders?.size)

            val rem = reminders!![0]
            assertEquals("legacy-rem-1", rem.id)
            assertEquals("Conta de Água", rem.name)
            assertEquals(85.50, rem.value, 0.001)
            assertEquals("2026-09-18", rem.date)
            assertFalse(rem.isPaid)
            assertFalse(rem.isRecurring)
            assertEquals("MONTHLY", rem.recurrenceFrequency)
            assertEquals(1, rem.totalOccurrences)
            assertEquals(1, rem.currentOccurrence)
            assertNull(rem.paymentMethod)
            assertNull(rem.recurrenceGroupId)
            assertTrue(rem.notifyOnDueDate)
            assertFalse(rem.notifyOneDayBefore)
        } finally {
            tempFile.delete()
        }
    }
}
