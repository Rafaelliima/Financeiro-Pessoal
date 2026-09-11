package com.example.data

import com.google.firebase.firestore.Exclude
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
    val bankId: String? = null,
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
     * Retorna o ano e mês em que a compra foi concluída / quitada, se aplicável.
     */
    fun getCompletionYearMonth(): Pair<Int, Int>? {
        if (!completedAt.isNullOrBlank()) {
            try {
                if (completedAt.contains("-")) {
                    val parts = completedAt.split("-")
                    return Pair(parts[0].toInt(), parts[1].toInt())
                }
            } catch (_: Exception) {}
        }

        if (isQuitada || (isInstallment && totalInstallments > 0 && paidInstallmentsCount >= totalInstallments)) {
            val paidIndex = (if (paidInstallmentsCount in 1..totalInstallments) paidInstallmentsCount else totalInstallments) - 1
            val totalMonths = (startYear * 12 + (startMonth - 1)) + paidIndex
            val y = totalMonths / 12
            val m = (totalMonths % 12) + 1
            return Pair(y, m)
        }

        return null
    }

    /**
     * Calcula o status das parcelas baseado no calendário e na data de consulta.
     * Preserva o histórico financeiro:
     * - Meses anteriores mantêm o valor da parcela.
     * - O mês em que a compra foi quitada mantém o valor pago.
     * - Meses posteriores à quitação não mostram valores futuros (0.0).
     */
    fun calculateInstallments(
        queryMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
        queryYear: Int = Calendar.getInstance().get(Calendar.YEAR),
        isCurrentInvoicePaid: Boolean = false
    ): InstallmentCalculation {
        val instVal = if (totalInstallments > 0) totalAmount / totalInstallments else totalAmount

        // 1. Compra à vista
        if (!isInstallment || totalInstallments <= 1) {
            val isSameMonth = queryYear == startYear && queryMonth == startMonth
            val isPastMonth = (queryYear > startYear) || (queryYear == startYear && queryMonth > startMonth)
            val isFutureMonth = (queryYear < startYear) || (queryYear == startYear && queryMonth < startMonth)

            return when {
                isFutureMonth -> InstallmentCalculation(1, 1, 0, 1, 0.0, "Futura")
                isSameMonth -> {
                    val isPaid = isQuitada || paidInstallmentsCount >= 1 || isCurrentInvoicePaid
                    InstallmentCalculation(1, 1, if (isPaid) 1 else 0, if (isPaid) 0 else 1, totalAmount, if (isPaid) "Quitado" else "Em andamento")
                }
                else -> {
                    // Meses posteriores: o gasto ocorreu no mês da compra e não se repete no futuro
                    InstallmentCalculation(1, 1, 1, 0, 0.0, "Quitado")
                }
            }
        }

        // 2. Compra parcelada
        val monthsDiff = (queryYear - startYear) * 12 + (queryMonth - startMonth)

        // Se a data de consulta for anterior à data de início, a compra é Futura para aquele mês
        if (monthsDiff < 0) {
            return InstallmentCalculation(1, totalInstallments, 0, totalInstallments, 0.0, "Futura")
        }

        // Verifica se a compra foi quitada e em qual período
        val completion = getCompletionYearMonth()
        if (completion != null) {
            val (compYear, compMonth) = completion
            val isAfterCompletion = (queryYear > compYear) || (queryYear == compYear && queryMonth > compMonth)
            if (isAfterCompletion) {
                // Meses posteriores à quitação: valor zerado
                return InstallmentCalculation(
                    currentInstallment = totalInstallments,
                    totalInstallments = totalInstallments,
                    paidInstallments = totalInstallments,
                    remainingInstallments = 0,
                    installmentValue = 0.0,
                    status = "Quitado"
                )
            }
        }

        // Se a consulta for posterior a todas as parcelas naturais
        if (monthsDiff >= totalInstallments) {
            return InstallmentCalculation(
                currentInstallment = totalInstallments,
                totalInstallments = totalInstallments,
                paidInstallments = totalInstallments,
                remainingInstallments = 0,
                installmentValue = 0.0,
                status = "Quitado"
            )
        }

        // Se a fatura atual do cartão foi paga
        if (isCurrentInvoicePaid) {
            val effectiveCurrent = maxOf(monthsDiff + 2, paidInstallmentsCount + 1)
            if (effectiveCurrent > totalInstallments) {
                return InstallmentCalculation(
                    currentInstallment = totalInstallments,
                    totalInstallments = totalInstallments,
                    paidInstallments = totalInstallments,
                    remainingInstallments = 0,
                    installmentValue = 0.0,
                    status = "Quitado"
                )
            }
            val remaining = (totalInstallments - effectiveCurrent).coerceAtLeast(0)
            return InstallmentCalculation(
                currentInstallment = effectiveCurrent,
                totalInstallments = totalInstallments,
                paidInstallments = effectiveCurrent - 1,
                remainingInstallments = remaining,
                installmentValue = instVal,
                status = "Em andamento"
            )
        }

        // O mês consultado está dentro do período de parcelamento ativo e não após a quitação
        val current = monthsDiff + 1
        val isQuitadaInThisMonth = completion != null && completion.first == queryYear && completion.second == queryMonth

        val remaining = if (completion != null) {
            val (compYear, compMonth) = completion
            val compMonthsDiff = (compYear - startYear) * 12 + (compMonth - startMonth)
            (compMonthsDiff - monthsDiff).coerceAtLeast(0)
        } else {
            (totalInstallments - current).coerceAtLeast(0)
        }

        return InstallmentCalculation(
            currentInstallment = current,
            totalInstallments = totalInstallments,
            paidInstallments = current - 1 + (if (isQuitadaInThisMonth) 1 else 0),
            remainingInstallments = remaining,
            installmentValue = instVal,
            status = "Em andamento"
        )
    }

    /**
     * Determina se a compra está atualmente quitada (para separação lógica entre compras ativas e histórico).
     */
    fun isCurrentlyQuitada(): Boolean {
        if (isQuitada) return true
        if (!isInstallment || totalInstallments <= 0) {
            return isQuitada || paidInstallmentsCount >= 1
        }
        if (paidInstallmentsCount >= totalInstallments) return true

        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH) + 1
        val currentYear = cal.get(Calendar.YEAR)
        val monthsDiff = (currentYear - startYear) * 12 + (currentMonth - startMonth)
        return monthsDiff >= totalInstallments
    }

    /**
     * Retorna o nome do mês de referência da parcela EXIBIDA.
     * Segue a lógica 100% calendário: Mês Início + (Parcela Atual - 1).
     */
    fun getNextInstallmentReference(isCurrentInvoicePaid: Boolean = false): String {
        val months = listOf(
            "", "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        )
        
        val calc = calculateInstallments(isCurrentInvoicePaid = isCurrentInvoicePaid)
        val totalMonths = (startYear * 12 + (startMonth - 1)) + (calc.currentInstallment - 1)
        
        val targetYear = totalMonths / 12
        val targetMonthIndex = (((totalMonths % 12) + 12) % 12) + 1
        
        return "${months[targetMonthIndex]}/$targetYear"
    }

    /**
     * Retorna o par (número da parcela efetiva, mês abreviado de vencimento) levando em conta se a fatura do mês atual já foi paga.
     */
    fun getEffectiveInstallmentDisplay(isInvoicePaid: Boolean): Pair<Int, String> {
        val currentCal = Calendar.getInstance()
        val curM = currentCal.get(Calendar.MONTH) + 1
        val curY = currentCal.get(Calendar.YEAR)
        val naturalProgress = (curY - startYear) * 12 + (curM - startMonth) + 1
        val currentInst = if (isInvoicePaid) {
            maxOf(naturalProgress, paidInstallmentsCount) + 1
        } else {
            maxOf(naturalProgress, paidInstallmentsCount + 1)
        }
        val totalMonths = (startYear * 12 + (startMonth - 1)) + (currentInst - 1)
        val targetMonthIndex = (((totalMonths % 12) + 12) % 12) + 1
        val months = listOf("", "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro")
        return Pair(currentInst, months[targetMonthIndex])
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
    val updatedAt: String? = null,
    @get:PropertyName("isShared") @set:PropertyName("isShared") var isShared: Boolean = false,
    @get:PropertyName("sharedWith") @set:PropertyName("sharedWith") var sharedWith: String? = null,
    @get:PropertyName("receivedAmount") @set:PropertyName("receivedAmount") var receivedAmount: Double = 0.0
) {
    @get:Exclude
    val effectiveMonthlyValue: Double
        get() = if (isShared) (monthlyValue - receivedAmount).coerceAtLeast(0.0) else monthlyValue
}

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
    val reminderId: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

/**
 * Representa um Lembrete de Pagamento.
 */
data class ReminderItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val value: Double = 0.0,
    val date: String = "",
    @get:PropertyName("isPaid") @set:PropertyName("isPaid") var isPaid: Boolean = false,
    val isRecurring: Boolean = false,
    val recurrenceFrequency: String = "MONTHLY",
    val totalOccurrences: Int = 1,
    val currentOccurrence: Int = 1,
    val paymentMethod: String? = null,
    val recurrenceGroupId: String? = null,
    val notifyOnDueDate: Boolean = true,
    val notifyOneDayBefore: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
