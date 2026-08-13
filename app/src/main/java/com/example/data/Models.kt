package com.example.data

import com.google.firebase.firestore.PropertyName
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

/**
 * Representa um Cartão de Crédito.
 */
data class CardItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val colorHex: String? = null,
    val source: DataSource = DataSource.MANUAL,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

/**
 * Resultado do cálculo de parcelas.
 */
data class InstallmentCalculation(
    val currentInstallment: Int,
    val totalInstallments: Int,
    val paidInstallments: Int,
    val remainingInstallments: Int,
    val installmentValue: Double,
    val status: String // "Em andamento", "Quitado", "Futura"
)

/**
 * Representa uma Compra (Parcelada ou à Vista).
 */
data class PurchaseItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val totalAmount: Double = 0.0,
    val cardId: String = "",
    val cardName: String = "",
    @get:PropertyName("isInstallment") @set:PropertyName("isInstallment") var isInstallment: Boolean = false,
    val totalInstallments: Int = 1,
    val startMonth: Int = 1, // 1-12
    val startYear: Int = 2026,
    val paidInstallmentsCount: Int = 0,
    @get:PropertyName("isQuitada") @set:PropertyName("isQuitada") var isQuitada: Boolean = false,
    val completedAt: String? = null,
    val source: DataSource = DataSource.MANUAL,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    /**
     * Calcula o status das parcelas baseado 100% no calendário atual.
     * Uma compra iniciada em Abril, consultada em Agosto, mostrará a 5ª parcela.
     */
    fun calculateInstallments(
        queryMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
        queryYear: Int = Calendar.getInstance().get(Calendar.YEAR)
    ): InstallmentCalculation {
        val instVal = if (totalInstallments > 0) totalAmount / totalInstallments else totalAmount

        if (isQuitada || (isInstallment && paidInstallmentsCount >= totalInstallments)) {
            return InstallmentCalculation(totalInstallments, totalInstallments, totalInstallments, 0, instVal, "Quitado")
        }

        if (!isInstallment || totalInstallments <= 1) {
            val isPaid = (queryYear > startYear) || (queryYear == startYear && queryMonth >= startMonth) || paidInstallmentsCount >= 1
            return InstallmentCalculation(1, 1, if (isPaid) 1 else 0, if (isPaid) 0 else 1, totalAmount, if (isPaid) "Quitado" else "Em andamento")
        }

        // Diferença de meses entre o início e o mês da consulta (Progresso Natural do Calendário)
        val monthsDiff = (queryYear - startYear) * 12 + (queryMonth - startMonth)
        val calendarProgress = monthsDiff + 1

        // A parcela atual é o progresso do calendário OU o próximo após o que já foi pago manualmente
        // Isso permite "pular" para o próximo mês ao clicar em Pagar Fatura
        val current = if (calendarProgress > paidInstallmentsCount) calendarProgress else paidInstallmentsCount + 1

        if (current <= 0) {
            return InstallmentCalculation(1, totalInstallments, 0, totalInstallments, instVal, "Futura")
        }

        return if (current > totalInstallments) {
            InstallmentCalculation(totalInstallments, totalInstallments, totalInstallments, 0, instVal, "Quitado")
        } else {
            InstallmentCalculation(current, totalInstallments, current - 1, totalInstallments - current, instVal, "Em andamento")
        }
    }

    /**
     * Determina se a compra está quitada pelo calendário.
     */
    fun isCurrentlyQuitada(): Boolean {
        if (isQuitada) return true
        if (!isInstallment || totalInstallments <= 0) return false
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH) + 1
        val currentYear = cal.get(Calendar.YEAR)
        val monthsDiff = (currentYear - startYear) * 12 + (currentMonth - startMonth)
        return monthsDiff >= totalInstallments // Aqui >= está correto: se já passou o número de meses, está quitada.
    }

    /**
     * Retorna o nome do mês de referência da parcela EXIBIDA.
     * Segue a lógica 100% calendário: Mês Início + (Parcela Atual - 1).
     */
    fun getNextInstallmentReference(): String {
        val months = listOf(
            "", "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        )
        
        val calc = calculateInstallments()
        val totalMonths = (startYear * 12 + (startMonth - 1)) + (calc.currentInstallment - 1)
        
        val targetYear = totalMonths / 12
        val targetMonthIndex = (totalMonths % 12) + 1
        
        return "${months[targetMonthIndex]}/$targetYear"
    }
}

/**
 * Representa uma Assinatura Mensal.
 */
data class SubscriptionItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val monthlyValue: Double = 0.0,
    val cardId: String = "",
    val cardName: String = "",
    val source: DataSource = DataSource.MANUAL,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

/**
 * Meio de pagamento para gastos diários.
 */
enum class PaymentMethod {
    CONTA, ESPECIE;

    companion object {
        fun fromString(value: String?): PaymentMethod {
            return try {
                if (!value.isNullOrBlank()) valueOf(value.uppercase()) else CONTA
            } catch (_: Exception) {
                CONTA
            }
        }
    }
}

/**
 * Representa um Gasto do Dia a Dia.
 */
data class DailyExpense(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val value: Double = 0.0,
    val date: String = "", // Ex: "2026-07-29" ou "29/07/2026"
    val source: DataSource = DataSource.MANUAL,
    val paymentMethod: PaymentMethod = PaymentMethod.CONTA,
    val observation: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
