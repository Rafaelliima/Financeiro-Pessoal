package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.DailyExpense
import com.example.data.PurchaseItem
import com.example.data.CardItem
import com.example.data.SubscriptionItem
import com.example.ui.theme.BankBrandRegistry
import com.example.ui.theme.DividerColor
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DashboardScreen(
    cards: List<CardItem> = emptyList(),
    purchases: List<PurchaseItem> = emptyList(),
    subscriptions: List<SubscriptionItem> = emptyList(),
    dailyExpenses: List<DailyExpense> = emptyList(),
    modifier: Modifier = Modifier
) {
    val realCurrentCal = remember { Calendar.getInstance() }
    val realCurrentMonth = realCurrentCal.get(Calendar.MONTH) + 1
    val realCurrentYear = realCurrentCal.get(Calendar.YEAR)

    // Estado para Seletor Discreto de Período
    var selectedMonth by remember { mutableStateOf(realCurrentMonth) }
    var selectedYear by remember { mutableStateOf(realCurrentYear) }

    val isCurrentPeriod = selectedMonth == realCurrentMonth && selectedYear == realCurrentYear

    // Nome do Mês Selecionado por extenso (ex: "Julho de 2026")
    val selectedPeriodName = remember(selectedMonth, selectedYear) {
        val months = listOf(
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        )
        "${months[selectedMonth - 1]} de $selectedYear"
    }

    // 1. Total de Assinaturas Mensais
    val totalSubscriptions = remember(subscriptions) {
        subscriptions.sumOf { it.monthlyValue }
    }

    // 2. Compras à vista no mês selecionado
    val singlePurchasesMonthTotal = remember(purchases, selectedMonth, selectedYear) {
        purchases.sumOf { p ->
            if (!p.isInstallment) {
                if (p.startMonth == selectedMonth && p.startYear == selectedYear) {
                    p.totalAmount
                } else 0.0
            } else 0.0
        }
    }

    // 3. Valor total das parcelas referentes ao mês selecionado
    val activeInstallmentsMonthTotal = remember(purchases, selectedMonth, selectedYear) {
        purchases.sumOf { p ->
            if (p.isInstallment) {
                val calc = p.calculateInstallments(selectedMonth, selectedYear)
                if (calc.status == "Em andamento" && calc.currentInstallment in 1..calc.totalInstallments) {
                    calc.installmentValue
                } else 0.0
            } else 0.0
        }
    }

    // 4. Gastos do Dia a Dia no mês selecionado
    val dailyExpensesMonthTotal = remember(dailyExpenses, selectedMonth, selectedYear) {
        dailyExpenses.sumOf { de ->
            // de.date está no formato YYYY-MM-DD ou DD/MM/AAAA
            var itemMonth = -1
            var itemYear = -1
            try {
                if (de.date.contains("-")) {
                    val parts = de.date.split("-")
                    if (parts.size >= 2) {
                        itemYear = parts[0].toIntOrNull() ?: -1
                        itemMonth = parts[1].toIntOrNull() ?: -1
                    }
                } else if (de.date.contains("/")) {
                    val parts = de.date.split("/")
                    if (parts.size >= 3) {
                        itemMonth = parts[1].toIntOrNull() ?: -1
                        itemYear = parts[2].toIntOrNull() ?: -1
                    }
                }
            } catch (_: Exception) {}

            if (itemMonth == selectedMonth && itemYear == selectedYear) {
                de.value
            } else 0.0
        }
    }

    // 5. Total de despesas do mês selecionado (Compras à vista + Parcelas + Assinaturas + Gastos do Dia a Dia)
    val totalMonthExpenses = singlePurchasesMonthTotal + activeInstallmentsMonthTotal + totalSubscriptions + dailyExpensesMonthTotal

    // 6. Quantidade de compras parceladas ativas no mês selecionado
    val activeInstallmentPurchasesCount = remember(purchases, selectedMonth, selectedYear) {
        purchases.count { p ->
            if (p.isInstallment) {
                val calc = p.calculateInstallments(selectedMonth, selectedYear)
                calc.status == "Em andamento" && calc.currentInstallment in 1..calc.totalInstallments
            } else false
        }
    }

    // Estatísticas globais discretas de compras (Sprint 13)
    val activePurchasesTotalCount = remember(purchases) {
        purchases.count { p -> !p.isQuitada && p.paidInstallmentsCount < p.totalInstallments }
    }
    val quitadasPurchasesTotalCount = remember(purchases) {
        purchases.count { p -> p.isQuitada || p.paidInstallmentsCount >= p.totalInstallments }
    }

    // 7. Valor total comprometido em parcelas futuras (parcelas ainda não pagas)
    val totalFutureInstallmentsAmount = remember(purchases, selectedMonth, selectedYear) {
        purchases.sumOf { p ->
            if (p.isInstallment) {
                val calc = p.calculateInstallments(selectedMonth, selectedYear)
                if (calc.status == "Em andamento") {
                    calc.remainingInstallments * calc.installmentValue
                } else 0.0
            } else 0.0
        }
    }

    // 8. Total por Cartão de Crédito (Mês Selecionado)
    val cardTotalsMap = remember(cards, purchases, subscriptions, selectedMonth, selectedYear) {
        val map = mutableMapOf<String, Double>()
        cards.forEach { map[it.id] = 0.0 }

        purchases.forEach { p ->
            val monthVal = if (p.isInstallment) {
                val calc = p.calculateInstallments(selectedMonth, selectedYear)
                if (calc.status == "Em andamento" && calc.currentInstallment in 1..calc.totalInstallments) {
                    calc.installmentValue
                } else 0.0
            } else {
                if (p.startMonth == selectedMonth && p.startYear == selectedYear) {
                    p.totalAmount
                } else 0.0
            }

            if (monthVal > 0) {
                val key = if (p.cardId.isNotBlank()) p.cardId else p.cardName
                map[key] = (map[key] ?: 0.0) + monthVal
            }
        }

        subscriptions.forEach { sub ->
            if (sub.cardId.isNotBlank() || sub.cardName.isNotBlank()) {
                val key = if (sub.cardId.isNotBlank()) sub.cardId else sub.cardName
                map[key] = (map[key] ?: 0.0) + sub.monthlyValue
            }
        }

        map
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen")
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // CABEÇALHO LIMPO COM SELETOR DISCRETO DE PERÍODO
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.testTag("dashboard_header_card")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Resumo",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )

                if (!isCurrentPeriod) {
                    AssistChip(
                        onClick = {
                            selectedMonth = realCurrentMonth
                            selectedYear = realCurrentYear
                        },
                        label = { Text("Voltar a Hoje", style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Today,
                                contentDescription = null,
                                tint = PrimaryAccent,
                                modifier = Modifier.padding(end = 2.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = null
                    )
                }
            }

            // Seletor Discreto de Mês (< Mês/Ano >)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("period_selector")
            ) {
                IconButton(
                    onClick = {
                        if (selectedMonth == 1) {
                            selectedMonth = 12
                            selectedYear -= 1
                        } else {
                            selectedMonth -= 1
                        }
                    },
                    modifier = Modifier.testTag("prev_month_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Mês anterior",
                        tint = TextSecondary
                    )
                }

                Text(
                    text = selectedPeriodName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.testTag("selected_period_text")
                )

                IconButton(
                    onClick = {
                        if (selectedMonth == 12) {
                            selectedMonth = 1
                            selectedYear += 1
                        } else {
                            selectedMonth += 1
                        }
                    },
                    modifier = Modifier.testTag("next_month_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = "Próximo mês",
                        tint = TextSecondary
                    )
                }
            }
        }

        // DESTAQUE PRINCIPAL (Valor com 48sp Bold)
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Text(
                text = String.format(Locale("pt", "BR"), "R$ %.2f", totalMonthExpenses),
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                modifier = Modifier.testTag("dashboard_total_expected_value")
            )
            Text(
                text = "Referente a $selectedPeriodName.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                modifier = Modifier.testTag("dashboard_header_summary_text")
            )
        }

        HorizontalDivider(color = DividerColor)

        // SEÇÃO: GASTOS DO DIA A DIA
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.testTag("dashboard_daily_expenses_card")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Payments,
                    contentDescription = null,
                    tint = TextSecondary
                )
                Text(
                    text = "Gastos do Dia a Dia",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total no mês",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
                Text(
                    text = String.format(Locale("pt", "BR"), "R$ %.2f", dailyExpensesMonthTotal),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.testTag("dashboard_daily_expenses_total_value")
                )
            }
        }

        HorizontalDivider(color = DividerColor)

        // SEÇÃO: CARTÕES DE CRÉDITO
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CreditCard,
                    contentDescription = null,
                    tint = TextSecondary
                )
                Text(
                    text = "Cartões",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            if (cards.isEmpty()) {
                Text(
                    text = "Nenhum cartão cadastrado",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                cards.forEachIndexed { index, card ->
                    val cardTotal = cardTotalsMap[card.id] ?: 0.0
                    val bankBrand = BankBrandRegistry.getBrandForName(card.name, card.colorHex)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .testTag("dashboard_card_total_${card.id}"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CreditCard,
                                contentDescription = null,
                                tint = bankBrand.mainColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = card.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = String.format(Locale("pt", "BR"), "R$ %.2f", cardTotal),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            modifier = Modifier.testTag("dashboard_card_value_${card.id}")
                        )
                    }
                    if (index < cards.size - 1) {
                        HorizontalDivider(color = DividerColor)
                    }
                }
            }
        }

        HorizontalDivider(color = DividerColor)

        // SEÇÃO: ASSINATURAS
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.testTag("dashboard_subscriptions_card")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Repeat,
                    contentDescription = null,
                    tint = TextSecondary
                )
                Text(
                    text = "Assinaturas",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            if (subscriptions.isEmpty()) {
                Text(
                    text = "Nenhuma assinatura cadastrada",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                subscriptions.forEachIndexed { index, sub ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sub.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = String.format(Locale("pt", "BR"), "R$ %.2f/mês", sub.monthlyValue),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    if (index < subscriptions.size - 1) {
                        HorizontalDivider(color = DividerColor)
                    }
                }

                HorizontalDivider(color = DividerColor, modifier = Modifier.padding(top = 4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total em assinaturas",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = String.format(Locale("pt", "BR"), "R$ %.2f/mês", totalSubscriptions),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        modifier = Modifier.testTag("dashboard_subscriptions_value")
                    )
                }
            }
        }

        HorizontalDivider(color = DividerColor)

        // SEÇÃO: PARCELAMENTOS
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.testTag("dashboard_installments_card")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ReceiptLong,
                    contentDescription = null,
                    tint = TextSecondary
                )
                Text(
                    text = "Parcelamentos",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Compras parceladas ativas",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
                Text(
                    text = "$activeInstallmentPurchasesCount compras",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.testTag("dashboard_active_installments_count_value")
                )
            }

            HorizontalDivider(color = DividerColor)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Neste mês",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
                Text(
                    text = String.format(Locale("pt", "BR"), "R$ %.2f", activeInstallmentsMonthTotal),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.testTag("dashboard_month_installments_value")
                )
            }

            HorizontalDivider(color = DividerColor)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Parcelas futuras (restantes)",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
                Text(
                    text = String.format(Locale("pt", "BR"), "R$ %.2f", totalFutureInstallmentsAmount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary,
                    modifier = Modifier.testTag("dashboard_future_installments_value")
                )
            }
        }

        Spacer(modifier = Modifier.height(88.dp))
    }
}
