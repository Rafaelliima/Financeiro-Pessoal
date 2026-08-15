package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CardItem
import com.example.data.DailyExpense
import com.example.data.PurchaseItem
import com.example.data.SubscriptionItem
import com.example.ui.theme.BankBrandRegistry
import com.example.ui.theme.HeroGradientDarkEnd
import com.example.ui.theme.HeroGradientDarkStart
import com.example.ui.theme.HeroGradientEnd
import com.example.ui.theme.HeroGradientStart
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.example.data.ReminderItem
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button

@Composable
fun DashboardScreen(
    cards: List<CardItem> = emptyList(),
    purchases: List<PurchaseItem> = emptyList(),
    subscriptions: List<SubscriptionItem> = emptyList(),
    dailyExpenses: List<DailyExpense> = emptyList(),
    reminders: List<ReminderItem> = emptyList(),
    onUpdateReminders: (List<ReminderItem>) -> Unit = {},
    onPayReminder: (ReminderItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showAddReminderDialog by remember { mutableStateOf(false) }
    var reminderToDelete by remember { mutableStateOf<ReminderItem?>(null) }

    val isDark = isSystemInDarkTheme()
    val heroBrush = Brush.verticalGradient(
        colors = if (isDark) {
            listOf(HeroGradientDarkStart, HeroGradientDarkEnd)
        } else {
            listOf(HeroGradientStart, HeroGradientEnd)
        }
    )

    val realCurrentCal = remember { Calendar.getInstance() }
    val realCurrentMonth = realCurrentCal.get(Calendar.MONTH) + 1
    val realCurrentYear = realCurrentCal.get(Calendar.YEAR)

    var selectedMonth by remember { mutableStateOf(realCurrentMonth) }
    var selectedYear by remember { mutableStateOf(realCurrentYear) }

    val isCurrentPeriod = selectedMonth == realCurrentMonth && selectedYear == realCurrentYear

    val selectedPeriodName = remember(selectedMonth, selectedYear) {
        val monthsList = listOf(
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        )
        "${monthsList[selectedMonth - 1]} de $selectedYear"
    }

    val totalSubscriptions = remember(subscriptions) {
        subscriptions.sumOf { it.monthlyValue }
    }

    val singlePurchasesMonthTotal = remember(purchases, selectedMonth, selectedYear) {
        purchases.sumOf { p ->
            if (!p.isInstallment && p.startMonth == selectedMonth && p.startYear == selectedYear) {
                p.totalAmount
            } else 0.0
        }
    }

    val activeInstallmentsMonthTotal = remember(purchases, selectedMonth, selectedYear) {
        purchases.sumOf { p ->
            if (p.isInstallment) {
                val calc = p.calculateInstallments(selectedMonth, selectedYear)
                if (calc.status == "Em andamento") calc.installmentValue else 0.0
            } else 0.0
        }
    }

    val dailyExpensesMonthTotal = remember(dailyExpenses, selectedMonth, selectedYear) {
        dailyExpenses.sumOf { de ->
            var itemMonth = -1
            var itemYear = -1
            try {
                if (de.date.contains("-")) {
                    val parts = de.date.split("-")
                    itemYear = parts[0].toInt()
                    itemMonth = parts[1].toInt()
                } else if (de.date.contains("/")) {
                    val parts = de.date.split("/")
                    itemMonth = parts[1].toInt()
                    itemYear = parts[2].toInt()
                }
            } catch (_: Exception) {}
            if (itemMonth == selectedMonth && itemYear == selectedYear) de.value else 0.0
        }
    }

    val totalMonthExpenses = singlePurchasesMonthTotal + activeInstallmentsMonthTotal + totalSubscriptions + dailyExpensesMonthTotal

    val activeInstallmentPurchasesCount = remember(purchases, selectedMonth, selectedYear) {
        purchases.count { p ->
            if (p.isInstallment) {
                val calc = p.calculateInstallments(selectedMonth, selectedYear)
                calc.status == "Em andamento"
            } else false
        }
    }

    val totalFutureInstallmentsAmount = remember(purchases, selectedMonth, selectedYear) {
        purchases.sumOf { p ->
            if (p.isInstallment) {
                val calc = p.calculateInstallments(selectedMonth, selectedYear)
                if (calc.status == "Em andamento") calc.remainingInstallments * calc.installmentValue else 0.0
            } else 0.0
        }
    }

    val cardTotalsMap = remember(cards, purchases, subscriptions, selectedMonth, selectedYear) {
        val map = mutableMapOf<String, Double>()
        cards.forEach { map[it.id] = 0.0 }

        purchases.forEach { p ->
            val monthVal = if (p.isInstallment) {
                val calc = p.calculateInstallments(selectedMonth, selectedYear)
                if (calc.status == "Em andamento") calc.installmentValue else 0.0
            } else if (p.startMonth == selectedMonth && p.startYear == selectedYear) {
                p.totalAmount
            } else 0.0

            if (monthVal > 0) {
                val key = if (p.cardId.isNotBlank()) p.cardId else p.cardName
                map[key] = (map[key] ?: 0.0) + monthVal
            }
        }

        subscriptions.forEach { sub ->
            val key = if (sub.cardId.isNotBlank()) sub.cardId else sub.cardName
            map[key] = (map[key] ?: 0.0) + sub.monthlyValue
        }
        map
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen")
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // CABEÇALHO MODERNO
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
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
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (!isCurrentPeriod) {
                    AssistChip(
                        onClick = {
                            selectedMonth = realCurrentMonth
                            selectedYear = realCurrentYear
                        },
                        label = { Text("Hoje", style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Today,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            labelColor = MaterialTheme.colorScheme.primary
                        ),
                        border = null,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 4.dp)
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
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = selectedPeriodName,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
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
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // CARD HERO
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dashboard_hero_card"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(heroBrush)
                    .padding(28.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Total Estimado",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", totalMonthExpenses),
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                    Text(
                        text = "Baseado em compras, assinaturas e gastos diários.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // SEÇÃO: LEMBRETES (Nova funcionalidade)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lembretes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(
                    onClick = { showAddReminderDialog = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Outlined.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(4.dp))
                    Text("Adicionar")
                }
            }

            if (reminders.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "Nenhum lembrete para este período.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            } else {
                val pendingReminders = reminders.filter { !it.isPaid }
                val paidReminders = reminders.filter { it.isPaid }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    pendingReminders.forEach { reminder ->
                        ReminderRow(
                            reminder = reminder,
                            onPay = { onPayReminder(reminder) },
                            onDelete = { reminderToDelete = reminder }
                        )
                    }
                    if (paidReminders.isNotEmpty()) {
                        Text(
                            text = "Pagos",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                        )
                        paidReminders.forEach { reminder ->
                            ReminderRow(
                                reminder = reminder,
                                onPay = {},
                                onDelete = { reminderToDelete = reminder }
                            )
                        }
                    }
                }
            }
        }

        // GASTOS DIA A DIA
        Card(
            modifier = Modifier.fillMaxWidth().testTag("dashboard_daily_expenses_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Payments,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Gastos do Dia a Dia",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Acumulado no mês",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", dailyExpensesMonthTotal),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("dashboard_daily_expenses_total_value")
                    )
                }
            }
        }

        // CARTÕES
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CreditCard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Cartões",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (cards.isEmpty()) {
                    Text(
                        text = "Nenhum cartão cadastrado",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    cards.forEach { card ->
                        val cardTotal = cardTotalsMap[card.id] ?: 0.0
                        val bankBrand = BankBrandRegistry.getBrandForName(card.name, card.colorHex)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(bankBrand.mainColor))
                                Text(text = card.name, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Text(
                                text = String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", cardTotal),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // ASSINATURAS
        Card(
            modifier = Modifier.fillMaxWidth().testTag("dashboard_subscriptions_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Repeat,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Assinaturas",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (subscriptions.isEmpty()) {
                    Text(text = "Nenhuma assinatura", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    subscriptions.forEach { sub ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = sub.name, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                text = String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", sub.monthlyValue),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Total em assinaturas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", totalSubscriptions),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // PARCELAMENTOS
        Card(
            modifier = Modifier.fillMaxWidth().testTag("dashboard_installments_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Parcelamentos",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Compras ativas no mês", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "$activeInstallmentPurchasesCount", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Total das parcelas", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        text = String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", activeInstallmentsMonthTotal),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Saldo devedor total", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", totalFutureInstallmentsAmount),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(88.dp))
    }

    if (showAddReminderDialog) {
        AddReminderDialog(
            onDismiss = { showAddReminderDialog = false },
            onConfirm = { newReminder ->
                onUpdateReminders(reminders + newReminder)
                showAddReminderDialog = false
            }
        )
    }

    reminderToDelete?.let { reminder ->
        AlertDialog(
            onDismissRequest = { reminderToDelete = null },
            title = { Text("Excluir Lembrete") },
            text = { Text("Deseja realmente excluir \"${reminder.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    onUpdateReminders(reminders.filter { it.id != reminder.id })
                    reminderToDelete = null
                }) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { reminderToDelete = null }) {
                    Text("Cancelar")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun ReminderRow(
    reminder: ReminderItem,
    onPay: () -> Unit,
    onDelete: () -> Unit
) {
    val displayDate = remember(reminder.date) {
        if (reminder.date.contains("-")) {
            try {
                val dateObj = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(reminder.date)
                SimpleDateFormat("dd/MM/yyyy", Locale.US).format(dateObj!!)
            } catch (e: Exception) { reminder.date }
        } else {
            reminder.date
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (reminder.isPaid) TextDecoration.LineThrough else null,
                    color = if (reminder.isPaid) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$displayDate • R$ ${String.format(Locale("pt", "BR"), "%.2f", reminder.value)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (!reminder.isPaid) {
                IconButton(onClick = onPay, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = "Pagar", tint = MaterialTheme.colorScheme.primary)
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun AddReminderDialog(
    onDismiss: () -> Unit,
    onConfirm: (ReminderItem) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var valueStr by remember { mutableStateOf("") }
    var date by remember {
        val cal = Calendar.getInstance()
        mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.US).format(cal.time))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Lembrete") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .imePadding()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("O que pagar?") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = valueStr,
                    onValueChange = { valueStr = it.replace(',', '.') },
                    label = { Text("Valor (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Data (DD/MM/AAAA)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val value = valueStr.toDoubleOrNull() ?: 0.0
                if (name.isNotBlank() && value > 0) {
                    val finalDate = try {
                        val dateObj = SimpleDateFormat("dd/MM/yyyy", Locale.US).parse(date)
                        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(dateObj!!)
                    } catch (e: Exception) { date }
                    onConfirm(ReminderItem(name = name, value = value, date = finalDate))
                }
            }) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}
