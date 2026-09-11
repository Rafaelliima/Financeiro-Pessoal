package com.example

import com.example.data.SubscriptionItem
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SubscriptionCalculationTest {

    @Test
    fun testNotSharedSubscription_EffectiveValueEqualsMonthlyValue() {
        val sub = SubscriptionItem(
            name = "Netflix",
            monthlyValue = 55.90,
            cardId = "card_1",
            cardName = "Nubank",
            isShared = false
        )

        assertEquals(55.90, sub.effectiveMonthlyValue, 0.001)
        assertFalse(sub.isShared)
    }

    @Test
    fun testSharedSubscription_CalculatesAbatementCorrectly() {
        val sub = SubscriptionItem(
            name = "Spotify Familiar",
            monthlyValue = 34.90,
            cardId = "card_1",
            cardName = "Nubank",
            isShared = true,
            sharedWith = "Carlos",
            receivedAmount = 17.45
        )

        assertTrue(sub.isShared)
        assertEquals("Carlos", sub.sharedWith)
        assertEquals(17.45, sub.receivedAmount, 0.001)
        assertEquals(17.45, sub.effectiveMonthlyValue, 0.001)
    }

    @Test
    fun testSharedSubscription_AbatementExceedingMonthlyValue_CoercesToZero() {
        val sub = SubscriptionItem(
            name = "YouTube Premium",
            monthlyValue = 41.90,
            cardId = "card_1",
            cardName = "Nubank",
            isShared = true,
            sharedWith = "Ana",
            receivedAmount = 50.00
        )

        assertEquals(0.0, sub.effectiveMonthlyValue, 0.001)
    }

    @Test
    fun testSubscriptionJsonSerializationAndDeserialization() {
        val original = SubscriptionItem(
            id = "sub-123",
            name = "iCloud+",
            monthlyValue = 14.90,
            cardId = "card-apple",
            cardName = "Mastercard",
            isShared = true,
            sharedWith = "Mariana",
            receivedAmount = 7.45
        )

        val json = JSONObject().apply {
            put("id", original.id)
            put("name", original.name)
            put("monthlyValue", original.monthlyValue)
            put("cardId", original.cardId)
            put("cardName", original.cardName)
            put("isShared", original.isShared)
            put("sharedWith", original.sharedWith)
            put("receivedAmount", original.receivedAmount)
        }

        val restored = SubscriptionItem(
            id = json.optString("id", original.id),
            name = json.optString("name", original.name),
            monthlyValue = json.optDouble("monthlyValue", 0.0),
            cardId = json.optString("cardId", ""),
            cardName = json.optString("cardName", ""),
            isShared = json.optBoolean("isShared", false),
            sharedWith = if (json.isNull("sharedWith")) null else json.optString("sharedWith").ifBlank { null },
            receivedAmount = json.optDouble("receivedAmount", 0.0)
        )

        assertEquals(original.id, restored.id)
        assertEquals(original.name, restored.name)
        assertEquals(original.monthlyValue, restored.monthlyValue, 0.001)
        assertTrue(restored.isShared)
        assertEquals("Mariana", restored.sharedWith)
        assertEquals(7.45, restored.receivedAmount, 0.001)
        assertEquals(7.45, restored.effectiveMonthlyValue, 0.001)
    }

    @Test
    fun testLegacySharedPropertyFallback() {
        val legacyJson = JSONObject().apply {
            put("id", "sub-legacy")
            put("name", "Disney+")
            put("monthlyValue", 40.0)
            put("shared", true)
            put("sharedWith", "Pedro")
            put("receivedAmount", 20.0)
        }

        val isShared = legacyJson.optBoolean("isShared", legacyJson.optBoolean("shared", false))
        assertTrue(isShared)
    }

    @Test
    fun testSubscriptionRegistry_MatchesKnownBrandsAndOfficialColors() {
        val spotify = com.example.data.subscription.SubscriptionRegistry.getBrandForSubscription("Spotify Premium")
        assertEquals("spotify", spotify.id)
        assertEquals("#1DB954", spotify.colorHex)

        val netflix = com.example.data.subscription.SubscriptionRegistry.getBrandForSubscription("Netflix 4K")
        assertEquals("netflix", netflix.id)
        assertEquals("#E50914", netflix.colorHex)

        val chatgpt = com.example.data.subscription.SubscriptionRegistry.getBrandForSubscription("ChatGPT Plus")
        assertEquals("chatgpt", chatgpt.id)
        assertEquals("#10A37F", chatgpt.colorHex)

        val meli = com.example.data.subscription.SubscriptionRegistry.getBrandForSubscription("Meli+")
        assertEquals("meliplus", meli.id)
        assertEquals("#FFE600", meli.colorHex)
    }

    @Test
    fun testSubscriptionRegistry_CaseAndAccentInsensitiveMatching() {
        val spoti = com.example.data.subscription.SubscriptionRegistry.getBrandForSubscription("spoti")
        assertEquals("spotify", spoti.id)

        val prime = com.example.data.subscription.SubscriptionRegistry.getBrandForSubscription("AMAZON PRIME VIDEO")
        assertEquals("primevideo", prime.id)

        val crunchy = com.example.data.subscription.SubscriptionRegistry.getBrandForSubscription("Crunchyroll Mega Fan")
        assertEquals("crunchyroll", crunchy.id)
    }

    @Test
    fun testSubscriptionRegistry_UnknownBrandFallback() {
        val unknown = com.example.data.subscription.SubscriptionRegistry.getBrandForSubscription("Academia SmartFit")
        assertTrue(unknown.id.startsWith("custom_"))
        assertEquals("Academia SmartFit", unknown.displayName)
        assertTrue(unknown.colorHex.startsWith("#"))
    }

    @Test
    fun testSubscriptionRegistry_CatalogAndSearch() {
        val all = com.example.data.subscription.SubscriptionRegistry.getAllSubscriptions()
        assertTrue(all.size >= 30)

        val spotifyById = com.example.data.subscription.SubscriptionRegistry.getSubscriptionById("spotify")
        assertEquals("Spotify", spotifyById?.displayName)
        assertEquals("spotify.com", spotifyById?.domain)

        val searchStream = com.example.data.subscription.SubscriptionRegistry.searchSubscriptions("stream")
        assertTrue(searchStream.any { it.id == "netflix" })

        val searchDuo = com.example.data.subscription.SubscriptionRegistry.searchSubscriptions("duo")
        assertTrue(searchDuo.any { it.id == "duolingo" })
    }
}

