package com.example

import com.example.data.PurchaseItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuitadaPurchasesCalculationTest {

    @Test
    fun testQuitadaInSeptember_PreservesAugustAndSeptember_ZeroesOctober() {
        // Compra de 3 parcelas de R$ 100,00 iniciada em Agosto/2026
        // Total = R$ 300,00. Parcela = R$ 100,00.
        // Quitada em Setembro/2026 (mês 9)
        val purchase = PurchaseItem(
            name = "Notebook",
            totalAmount = 300.0,
            isInstallment = true,
            totalInstallments = 3,
            startMonth = 8,
            startYear = 2026,
            paidInstallmentsCount = 2,
            isQuitada = true,
            completedAt = "2026-09-15"
        )

        // 1. Consulta em Agosto/2026 (mês anterior à quitação)
        val calcAgosto = purchase.calculateInstallments(queryMonth = 8, queryYear = 2026)
        assertEquals("Em andamento", calcAgosto.status)
        assertEquals(100.0, calcAgosto.installmentValue, 0.001)
        assertEquals(1, calcAgosto.currentInstallment)

        // 2. Consulta em Setembro/2026 (mês em que foi quitada)
        val calcSetembro = purchase.calculateInstallments(queryMonth = 9, queryYear = 2026)
        assertEquals("Em andamento", calcSetembro.status)
        assertEquals(100.0, calcSetembro.installmentValue, 0.001)
        assertEquals(2, calcSetembro.currentInstallment)
        assertEquals(0, calcSetembro.remainingInstallments)

        // 3. Consulta em Outubro/2026 (mês posterior à quitação)
        val calcOutubro = purchase.calculateInstallments(queryMonth = 10, queryYear = 2026)
        assertEquals("Quitado", calcOutubro.status)
        assertEquals(0.0, calcOutubro.installmentValue, 0.001)
        assertEquals(0, calcOutubro.remainingInstallments)
    }

    @Test
    fun testIsCurrentlyQuitada_WhenPaidInstallmentsEqualsTotal() {
        val purchase = PurchaseItem(
            name = "Smartphone",
            totalAmount = 1200.0,
            isInstallment = true,
            totalInstallments = 6,
            startMonth = 1,
            startYear = 2026,
            paidInstallmentsCount = 6,
            isQuitada = false
        )

        assertTrue("Compra com todas parcelas pagas deve ser considerada quitada", purchase.isCurrentlyQuitada())
    }

    @Test
    fun testSinglePurchase_InPurchaseMonth_AndSubsequentMonths() {
        val singlePurchase = PurchaseItem(
            name = "Mercado",
            totalAmount = 250.0,
            isInstallment = false,
            totalInstallments = 1,
            startMonth = 9,
            startYear = 2026,
            isQuitada = true
        )

        // No mês da compra: mostra o valor gasto
        val calcSept = singlePurchase.calculateInstallments(queryMonth = 9, queryYear = 2026)
        assertEquals(250.0, calcSept.installmentValue, 0.001)

        // No mês posterior: valor zerado
        val calcOct = singlePurchase.calculateInstallments(queryMonth = 10, queryYear = 2026)
        assertEquals(0.0, calcOct.installmentValue, 0.001)
    }

    @Test
    fun testInstallmentAdvance_WhenCurrentInvoiceIsPaid() {
        // Compra de 2 parcelas de R$ 100 iniciada no mês 9/2026
        val purchase = PurchaseItem(
            name = "Fone Bluetooth",
            totalAmount = 200.0,
            isInstallment = true,
            totalInstallments = 2,
            startMonth = 9,
            startYear = 2026,
            paidInstallmentsCount = 0
        )

        // Se a fatura ainda não foi paga
        val displayBeforePay = purchase.getEffectiveInstallmentDisplay(isInvoicePaid = false)
        assertEquals(1, displayBeforePay.first)

        // Se a fatura foi marcada como paga, avança para a próxima parcela (2/2)
        val displayAfterPay = purchase.getEffectiveInstallmentDisplay(isInvoicePaid = true)
        assertEquals(2, displayAfterPay.first)

        // Cálculo com isCurrentInvoicePaid = true
        val calcWithPaidInvoice = purchase.calculateInstallments(queryMonth = 9, queryYear = 2026, isCurrentInvoicePaid = true)
        assertEquals("Em andamento", calcWithPaidInvoice.status)
        assertEquals(2, calcWithPaidInvoice.currentInstallment)
        assertEquals(0, calcWithPaidInvoice.remainingInstallments)
        assertEquals(100.0, calcWithPaidInvoice.installmentValue, 0.001)
    }

    @Test
    fun testInstallmentQuitada_WhenLastInstallmentPaidViaInvoice() {
        // Compra de 2 parcelas onde 1 já foi paga e a fatura atual é quitada
        val purchase = PurchaseItem(
            name = "Mouse",
            totalAmount = 100.0,
            isInstallment = true,
            totalInstallments = 2,
            startMonth = 8,
            startYear = 2026,
            paidInstallmentsCount = 1
        )

        // Com a fatura do mês atual paga, 2 + 1 > 2 -> Quitado
        val calc = purchase.calculateInstallments(queryMonth = 9, queryYear = 2026, isCurrentInvoicePaid = true)
        assertEquals("Quitado", calc.status)
        assertEquals(0.0, calc.installmentValue, 0.001)
    }
}
