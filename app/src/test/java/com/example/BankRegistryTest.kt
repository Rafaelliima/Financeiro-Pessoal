package com.example

import com.example.data.bank.BankRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BankRegistryTest {

    @Test
    fun testSearchBanks_ByNameWithAndWithoutAccents() {
        val result1 = BankRegistry.searchBanks("itau")
        assertTrue(result1.any { it.id == "itau" })

        val result2 = BankRegistry.searchBanks("Itaú")
        assertTrue(result2.any { it.id == "itau" })

        val result3 = BankRegistry.searchBanks("brasil")
        assertTrue(result3.any { it.id == "bancodobrasil" })
    }

    @Test
    fun testSearchBanks_ByNameAndAliases() {
        val nu = BankRegistry.searchBanks("nu")
        assertTrue(nu.any { it.id == "nubank" })

        val bb = BankRegistry.searchBanks("bb")
        assertTrue(bb.any { it.id == "bancodobrasil" })

        val mp = BankRegistry.searchBanks("mp")
        assertTrue(mp.any { it.id == "mercadopago" })

        val pan = BankRegistry.searchBanks("pan")
        assertTrue(pan.any { it.id == "bancopan" })
    }

    @Test
    fun testSearchBanks_DoesNotMatchCompeCode() {
        // A busca deve ser restrita a nomes e apelidos, ignorando códigos numéricos de compensação
        val nubankResult = BankRegistry.searchBanks("260")
        assertTrue(nubankResult.isEmpty())

        val itauResult = BankRegistry.searchBanks("341")
        assertTrue(itauResult.isEmpty())
    }

    @Test
    fun testStandardBanks_HaveLogosConfigured() {
        val nubank = BankRegistry.getBankById("nubank")
        assertNotNull(nubank)
        assertNotNull(nubank?.logoResId)

        val itau = BankRegistry.getBankById("itau")
        assertNotNull(itau)
        assertNotNull(itau?.logoResId)

        val bb = BankRegistry.getBankById("bancodobrasil")
        assertNotNull(bb)
        assertNotNull(bb?.logoResId)
    }

    @Test
    fun testGetBankForCard_AutoAssociationExistingCards() {
        val bankNu = BankRegistry.getBankForCard("Meu Nubank Roxo")
        assertEquals("nubank", bankNu.id)

        val bankItau = BankRegistry.getBankForCard("Cartão Itaucard")
        assertEquals("itau", bankItau.id)

        // Custom Bank fallback
        val bankUnknown = BankRegistry.getBankForCard("Banco do Bairro", null, "#123456")
        assertTrue(bankUnknown.isCustom)
        assertEquals("Banco do Bairro", bankUnknown.displayName)
        assertEquals("#123456", bankUnknown.colorHex)
    }

    @Test
    fun testCreateCustomBank() {
        val custom = BankRegistry.createCustomBank("Cooperativa Local", "#00FF00")
        assertTrue(custom.isCustom)
        assertEquals("Cooperativa Local", custom.displayName)
        assertEquals("#00FF00", custom.colorHex)
    }
}
