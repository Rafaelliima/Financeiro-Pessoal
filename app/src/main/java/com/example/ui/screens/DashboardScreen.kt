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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.PrimaryAccent
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
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import androidx.compose.ui.text.style.TextOverflow
import com.example.data.GoogleAccountData
import com.example.data.CardPaymentItem
import com.example.ui.theme.SuccessGreen
import com.example.data.bank.BankRegistry
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
import com.example.ui.theme.AppShapes
import com.example.ui.theme.Spacing
import com.example.ui.theme.HeroGradientDarkEnd
import com.example.ui.theme.HeroGradientDarkStart
import com.example.ui.theme.HeroGradientEnd
import com.example.ui.theme.HeroGradientStart
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import com.example.data.ReminderItem
import com.example.data.ReminderSyncUtils
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Check
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Repeat

@Composable
fun DashboardScreen(
    googleAccount: GoogleAccountData? = null,
    cards: List<CardItem> = emptyList(),
    cardPayments: List<CardPaymentItem> = emptyList(),
    purchases: List<PurchaseItem> = emptyList(),
    subscriptions: List<SubscriptionItem> = emptyList(),
    dailyExpenses: List<DailyExpense> = emptyList(),
    reminders: List<ReminderItem> = emptyList(),
    onUpdateReminders: (List<ReminderItem>) -> Unit = {},
    onPayReminder: (ReminderItem) -> Unit = {},
    onAddPurchase: (PurchaseItem) -> Unit = {},
    onAddDailyExpense: (DailyExpense) -> Unit = {},
    onAddSubscription: (SubscriptionItem) -> Unit = {},
    onNavigateToPurchases: (tabIndex: Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showAddReminderDialog by remember { mutableStateOf(false) }
    var showAllReminders by remember { mutableStateOf(false) }
    var reminderToDelete by remember { mutableStateOf<ReminderItem?>(null) }
    var isFabExpanded by remember { mutableStateOf(false) }
    var showAddPurchaseSheet by remember { mutableStateOf(false) }
    var showAddDailyExpenseSheet by remember { mutableStateOf(false) }
    var showAddSubscriptionSheet by remember { mutableStateOf(false) }

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
        subscriptions.sumOf { it.effectiveMonthlyValue }
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

    val dailyExpensesMonthCount = remember(dailyExpenses, selectedMonth, selectedYear) {
        dailyExpenses.count { de ->
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
            itemMonth == selectedMonth && itemYear == selectedYear
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
                val matchingCard = cards.find { c -> (p.cardId.isNotBlank() && c.id == p.cardId) || (c.name.isNotBlank() && c.name == p.cardName) }
                if (matchingCard != null) {
                    map[matchingCard.id] = (map[matchingCard.id] ?: 0.0) + monthVal
                }
            }
        }

        subscriptions.forEach { sub ->
            val matchingCard = cards.find { c -> (sub.cardId.isNotBlank() && c.id == sub.cardId) || (c.name.isNotBlank() && c.name == sub.cardName) }
            if (matchingCard != null) {
                map[matchingCard.id] = (map[matchingCard.id] ?: 0.0) + sub.effectiveMonthlyValue
            }
        }
        map
    }

    val totalCardsInvoices = remember(cardTotalsMap) {
        cardTotalsMap.values.sum()
    }

    val totalMonthRealized = totalCardsInvoices + dailyExpensesMonthTotal

    val periodReminders = remember(reminders, selectedMonth, selectedYear) {
        reminders.filter { r ->
            val ym = ReminderSyncUtils.parseYearMonth(r.date)
            if (ym != null) {
                ym.first == selectedYear && ym.second == selectedMonth
            } else true
        }
    }

    val overdueReminders = remember(reminders, selectedMonth, selectedYear) {
        reminders.filter { r ->
            if (r.isPaid) return@filter false
            val ym = ReminderSyncUtils.parseYearMonth(r.date)
            if (ym != null) {
                val (y, m) = ym
                (y < selectedYear) || (y == selectedYear && m < selectedMonth)
            } else false
        }
    }

    val totalRemindersMonth = remember(periodReminders) {
        periodReminders.sumOf { it.value }
    }

    var isRemindersExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("dashboard_screen")
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // CABEÇALHO VERO (2 linhas limpas)
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_header_card")
            ) {
                // Linha 1: Avatar + Saudação à esquerda, Chip "Hoje" à direita
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val localPhoto = googleAccount?.customPhotoPath?.let { java.io.File(it) }
                        if (localPhoto != null && localPhoto.exists()) {
                            AsyncImage(
                                model = localPhoto,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else if (!googleAccount?.photoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = googleAccount?.photoUrl,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            val displayName = googleAccount?.customDisplayName?.ifBlank { null }
                                ?: googleAccount?.userName?.ifBlank { null }
                                ?: "Usuário Grana+"
                            val initials = displayName.split(" ")
                                .filter { it.isNotBlank() }
                                .take(2)
                                .mapNotNull { it.firstOrNull()?.uppercase() }
                                .joinToString("")

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                if (initials.isNotBlank()) {
                                    Text(
                                        text = initials,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.Person,
                                        contentDescription = "Foto de perfil",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Column {
                            Text(
                                text = "Grana+",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            val greetingName = remember(googleAccount) {
                                googleAccount?.customDisplayName?.ifBlank { null }
                                    ?: googleAccount?.userName?.split(" ")?.firstOrNull()?.ifBlank { null }
                                    ?: "Você"
                            }
                            Text(
                                text = "Olá, $greetingName",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

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
                            shape = AppShapes.Small
                        )
                    }
                }

                // Linha 2: Seletor de mês centralizado [ < Mês de Ano > ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(AppShapes.Medium)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
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
                                contentDescription = "Mês anterior",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = selectedPeriodName,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp),
                            color = MaterialTheme.colorScheme.onSurface
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
                                contentDescription = "Próximo mês",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // CARD HERO DE VALORES (Fintech Minimalist - sem quebra de linha)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_hero_card"),
                shape = AppShapes.Medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.Medium),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall)) {
                        Text(
                            text = "Total Consolidado Realizado",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", totalMonthRealized),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Linha 1: Ponto indicador primário + Faturas de Cartões
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = "Faturas de Cartões",
                                maxLines = 1,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", totalCardsInvoices),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Linha 2: Ponto indicador secundário + Gastos do Dia a Dia
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary)
                            )
                            Text(
                                text = "Gastos do Dia a Dia",
                                maxLines = 1,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", dailyExpensesMonthTotal),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Linha sutil: Previsão a pagar em Lembretes
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "A pagar em Lembretes",
                                maxLines = 1,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", totalRemindersMonth),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // SEÇÃO: LEMBRETES (2ª Seção mais importante, logo após o Card de Valores)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Lembretes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        val counterText = if (periodReminders.size > 3 && !isRemindersExpanded) {
                            "3 de ${periodReminders.size} no mês"
                        } else {
                            "${periodReminders.size} no mês"
                        }
                        Text(
                            text = counterText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(
                        onClick = { showAddReminderDialog = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(4.dp))
                        Text("Adicionar")
                    }
                }

                if (showAllReminders) {
                    val allPending = reminders.filter { !it.isPaid }
                    val allPaid = reminders.filter { it.isPaid }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Exibindo todos os períodos (${reminders.size})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            TextButton(
                                onClick = { showAllReminders = false }
                            ) {
                                Text("Filtrar por $selectedPeriodName", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        if (allPending.isNotEmpty()) {
                            Text(
                                text = "Pendentes",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            allPending.forEach { reminder ->
                                ReminderRow(
                                    reminder = reminder,
                                    onPay = { onPayReminder(reminder) },
                                    onDelete = { reminderToDelete = reminder }
                                )
                            }
                        }

                        if (allPaid.isNotEmpty()) {
                            Text(
                                text = "Pagos",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 6.dp, start = 4.dp)
                            )
                            allPaid.forEach { reminder ->
                                ReminderRow(
                                    reminder = reminder,
                                    onPay = {},
                                    onDelete = { reminderToDelete = reminder }
                                )
                            }
                        }
                    }
                } else {
                    if (periodReminders.isEmpty() && overdueReminders.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = AppShapes.Medium,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "Nenhum lembrete para $selectedPeriodName",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (overdueReminders.isNotEmpty()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Atrasados de meses anteriores (${overdueReminders.size})",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                overdueReminders.forEach { reminder ->
                                    ReminderRow(
                                        reminder = reminder,
                                        onPay = { onPayReminder(reminder) },
                                        onDelete = { reminderToDelete = reminder }
                                    )
                                }
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )
                            }

                            val displayedReminders = if (isRemindersExpanded) periodReminders else periodReminders.take(3)
                            displayedReminders.forEach { reminder ->
                                ReminderRow(
                                    reminder = reminder,
                                    onPay = { onPayReminder(reminder) },
                                    onDelete = { reminderToDelete = reminder }
                                )
                            }

                            if (periodReminders.size > 3) {
                                val remaining = periodReminders.size - 3
                                TextButton(
                                    onClick = { isRemindersExpanded = !isRemindersExpanded },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text(
                                        text = if (isRemindersExpanded) "Mostrar menos ↑" else "Mostrar mais ($remaining restantes) ↓",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SEÇÃO: CARTÕES (Posicionada imediatamente após Lembretes, com status real de fatura)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_cards_section")
                    .clickable { onNavigateToPurchases(0) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = AppShapes.Medium,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.Medium),
                    verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CreditCard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Cartões",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
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
                            val isPaidThisMonth = cardPayments.any {
                                it.cardId == card.id && it.month == selectedMonth && it.year == selectedYear && it.paid
                            }
                            val bankBrand = BankBrandRegistry.getBrandForName(card.name, card.colorHex)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    val standardBank = remember(card) {
                                        BankRegistry.getBankForCard(card.name, card.bankId, card.colorHex)
                                    }
                                    if (standardBank.logoResId != null) {
                                        Image(
                                            painter = painterResource(id = standardBank.logoResId),
                                            contentDescription = standardBank.displayName,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(bankBrand.mainColor)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = card.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (isPaidThisMonth) "✓ Fatura Paga" else "• Em aberto",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isPaidThisMonth) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = if (isPaidThisMonth) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                                Text(
                                    text = String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", cardTotal),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.testTag("dashboard_card_value_${card.id}")
                                )
                            }
                        }
                    }
                }
            }
            // NOVO RESUMO VERO (GRID 2x2 COMPACTO)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_summary_grid"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryMiniCard(
                        title = "Dia a Dia",
                        value = String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", dailyExpensesMonthTotal),
                        subtitle = "$dailyExpensesMonthCount ${if (dailyExpensesMonthCount == 1) "lançamento" else "lançamentos"}",
                        icon = Icons.Outlined.Payments,
                        modifier = Modifier.weight(1f),
                        testTag = "dashboard_daily_expenses_card",
                        valueTestTag = "dashboard_daily_expenses_total_value",
                        onClick = { onNavigateToPurchases(1) }
                    )
                    SummaryMiniCard(
                        title = "Assinaturas",
                        value = String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", totalSubscriptions),
                        subtitle = "${subscriptions.size} ${if (subscriptions.size == 1) "ativa" else "ativas"}",
                        icon = Icons.Outlined.Repeat,
                        modifier = Modifier.weight(1f),
                        testTag = "dashboard_subscriptions_card",
                        onClick = { onNavigateToPurchases(2) }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryMiniCard(
                        title = "Parcelamentos",
                        value = String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", activeInstallmentsMonthTotal),
                        subtitle = "$activeInstallmentPurchasesCount ${if (activeInstallmentPurchasesCount == 1) "compra ativa" else "compras ativas"}",
                        icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                        modifier = Modifier.weight(1f),
                        testTag = "dashboard_installments_card",
                        onClick = { onNavigateToPurchases(0) }
                    )
                    SummaryMiniCard(
                        title = "Saldo Devedor",
                        value = String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", totalFutureInstallmentsAmount),
                        subtitle = "Total a quitar",
                        icon = Icons.AutoMirrored.Outlined.TrendingUp,
                        modifier = Modifier.weight(1f),
                        testTag = "dashboard_summary_future_installments",
                        onClick = { onNavigateToPurchases(0) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(88.dp))
        }

        // Scrim escuro ao expandir o Speed Dial
        if (isFabExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isFabExpanded = false }
            )
        }

        // Speed Dial FAB
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AnimatedVisibility(
                visible = isFabExpanded,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Opção 1: Compra no Cartão
                    QuickActionItem(
                        label = "Compra no Cartão",
                        icon = Icons.Outlined.CreditCard,
                        testTag = "dashboard_fab_purchase",
                        onClick = {
                            isFabExpanded = false
                            showAddPurchaseSheet = true
                        }
                    )

                    // Opção 2: Despesa Dia a Dia
                    QuickActionItem(
                        label = "Dia a Dia",
                        icon = Icons.Outlined.Today,
                        testTag = "dashboard_fab_daily_expense",
                        onClick = {
                            isFabExpanded = false
                            showAddDailyExpenseSheet = true
                        }
                    )

                    // Opção 3: Assinatura
                    QuickActionItem(
                        label = "Assinatura",
                        icon = Icons.Outlined.Repeat,
                        testTag = "dashboard_fab_subscription",
                        onClick = {
                            isFabExpanded = false
                            showAddSubscriptionSheet = true
                        }
                    )
                }
            }

            val rotation by animateFloatAsState(
                targetValue = if (isFabExpanded) 45f else 0f,
                label = "fab_rotation"
            )

            FloatingActionButton(
                onClick = { isFabExpanded = !isFabExpanded },
                containerColor = PrimaryAccent,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("dashboard_fab_expand")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = if (isFabExpanded) "Fechar ações rápidas" else "Ações rápidas",
                    modifier = Modifier.rotate(rotation).size(26.dp)
                )
            }
        }
    }

    if (showAddPurchaseSheet) {
        PurchaseBottomSheet(
            availableCards = cards,
            initialPurchase = null,
            onDismiss = { showAddPurchaseSheet = false },
            onConfirm = { purchase ->
                onAddPurchase(purchase)
                showAddPurchaseSheet = false
            }
        )
    }

    if (showAddDailyExpenseSheet) {
        DailyExpenseBottomSheet(
            initialExpense = null,
            onDismiss = { showAddDailyExpenseSheet = false },
            onConfirm = { expense ->
                onAddDailyExpense(expense)
                showAddDailyExpenseSheet = false
            }
        )
    }

    if (showAddSubscriptionSheet) {
        SubscriptionBottomSheet(
            availableCards = cards,
            initialSubscription = null,
            onDismiss = { showAddSubscriptionSheet = false },
            onConfirm = { subscription ->
                onAddSubscription(subscription)
                showAddSubscriptionSheet = false
            }
        )
    }

    if (showAddReminderDialog) {
        AddReminderDialog(
            onDismiss = { showAddReminderDialog = false },
            onConfirm = { newReminders ->
                onUpdateReminders(reminders + newReminders)
                showAddReminderDialog = false
            }
        )
    }

    reminderToDelete?.let { reminder ->
        AlertDialog(
            onDismissRequest = { reminderToDelete = null },
            title = { Text("Excluir Lembrete", fontWeight = FontWeight.Bold) },
            text = {
                if (reminder.isRecurring && reminder.recurrenceGroupId != null) {
                    Text("Este lembrete faz parte de uma série recorrente (${reminder.currentOccurrence}/${if (reminder.totalOccurrences > 0) reminder.totalOccurrences else "∞"}). Deseja excluir apenas esta parcela ou toda a série?")
                } else {
                    Text("Deseja realmente excluir \"${reminder.name}\"?")
                }
            },
            confirmButton = {
                if (reminder.isRecurring && reminder.recurrenceGroupId != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            onUpdateReminders(reminders.filter { it.id != reminder.id })
                            reminderToDelete = null
                        }) {
                            Text("Apenas esta", color = MaterialTheme.colorScheme.error)
                        }
                        Button(
                            onClick = {
                                onUpdateReminders(reminders.filter { it.recurrenceGroupId != reminder.recurrenceGroupId })
                                reminderToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Toda a série")
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            onUpdateReminders(reminders.filter { it.id != reminder.id })
                            reminderToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Excluir")
                    }
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
private fun QuickActionItem(
    label: String,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 4.dp
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            shape = CircleShape,
            modifier = Modifier.size(42.dp)
        ) {
            Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(20.dp))
        }
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

    val isOverdue = remember(reminder.date, reminder.isPaid) {
        if (reminder.isPaid) false
        else {
            try {
                val normalizedDate = ReminderSyncUtils.normalizeDate(reminder.date)
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val dueDate = sdf.parse(normalizedDate)
                val calToday = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                dueDate != null && dueDate.before(calToday.time)
            } catch (_: Exception) { false }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.12f)
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isOverdue) MaterialTheme.colorScheme.error.copy(alpha = 0.45f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = reminder.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (reminder.isPaid) TextDecoration.LineThrough else null,
                        color = if (reminder.isPaid) MaterialTheme.colorScheme.onSurfaceVariant
                        else if (isOverdue) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (reminder.isRecurring) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "${reminder.currentOccurrence}/${if (reminder.totalOccurrences > 0) reminder.totalOccurrences else "∞"}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (!reminder.paymentMethod.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = reminder.paymentMethod,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Text(
                    text = if (isOverdue) "Atrasado ($displayDate)" else "Vence $displayDate",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isOverdue) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", reminder.value),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (reminder.isPaid) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )

            if (!reminder.isPaid) {
                IconButton(onClick = onPay, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = "Pagar", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AddReminderDialog(
    onDismiss: () -> Unit,
    onConfirm: (List<ReminderItem>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var valueStr by remember { mutableStateOf("") }
    val cal = remember { Calendar.getInstance() }
    var date by remember {
        mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.US).format(cal.time))
    }

    var isRecurring by remember { mutableStateOf(false) }
    var recurrenceFrequency by remember { mutableStateOf("MONTHLY") }
    var repsInput by remember { mutableStateOf("12") }
    var isContinuous by remember { mutableStateOf(false) }
    var paymentMethod by remember { mutableStateOf("Saldo em conta") }
    var notifyOnDueDate by remember { mutableStateOf(true) }
    var notifyOneDayBefore by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var valueError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    val hasUnsavedChanges = remember(name, valueStr, isRecurring) {
        name.isNotBlank() || valueStr.isNotBlank() || isRecurring
    }

    val attemptDismiss = {
        if (hasUnsavedChanges) {
            showUnsavedDialog = true
        } else {
            onDismiss()
        }
    }

    androidx.activity.compose.BackHandler(enabled = hasUnsavedChanges) {
        showUnsavedDialog = true
    }

    val projectedEndDate = remember(date, isRecurring, recurrenceFrequency, repsInput, isContinuous) {
        if (!isRecurring) null
        else {
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                val startDate = sdf.parse(date.trim())
                if (startDate != null) {
                    val c = Calendar.getInstance().apply { time = startDate }
                    val count = if (isContinuous) 12 else (repsInput.toIntOrNull()?.coerceAtLeast(1) ?: 1)
                    for (i in 1 until count) {
                        when (recurrenceFrequency) {
                            "WEEKLY" -> c.add(Calendar.WEEK_OF_YEAR, 1)
                            "BIWEEKLY" -> c.add(Calendar.WEEK_OF_YEAR, 2)
                            "YEARLY" -> c.add(Calendar.YEAR, 1)
                            else -> c.add(Calendar.MONTH, 1)
                        }
                    }
                    sdf.format(c.time)
                } else null
            } catch (_: Exception) { null }
        }
    }

    fun validateAndSave(): Boolean {
        var isValid = true

        if (name.trim().isBlank()) {
            nameError = "Informe o que deve ser pago."
            isValid = false
        } else {
            nameError = null
        }

        val amount = valueStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            valueError = "Informe um valor maior que zero."
            isValid = false
        } else {
            valueError = null
        }

        val parsedBaseDate = try {
            SimpleDateFormat("dd/MM/yyyy", Locale.US).parse(date.trim())
        } catch (_: Exception) {
            dateError = "Informe uma data válida (DD/MM/AAAA)."
            isValid = false
            null
        }

        if (isValid && parsedBaseDate != null && amount != null) {
            val items = mutableListOf<ReminderItem>()
            val groupId = if (isRecurring) UUID.randomUUID().toString() else null
            val parsedReps = repsInput.toIntOrNull()?.coerceAtLeast(1) ?: 12
            val repsCount = if (!isRecurring) 1 else if (isContinuous) 12 else parsedReps
            val totalOccurrences = if (!isRecurring) 1 else if (isContinuous) 0 else parsedReps

            val sdfOutput = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val c = Calendar.getInstance().apply { time = parsedBaseDate }

            for (i in 1..repsCount) {
                val dateStr = sdfOutput.format(c.time)
                items.add(
                    ReminderItem(
                        name = name.trim(),
                        value = amount,
                        date = dateStr,
                        isRecurring = isRecurring,
                        recurrenceFrequency = recurrenceFrequency,
                        totalOccurrences = totalOccurrences,
                        currentOccurrence = i,
                        paymentMethod = paymentMethod,
                        recurrenceGroupId = groupId,
                        notifyOnDueDate = notifyOnDueDate,
                        notifyOneDayBefore = notifyOneDayBefore
                    )
                )
                when (recurrenceFrequency) {
                    "WEEKLY" -> c.add(Calendar.WEEK_OF_YEAR, 1)
                    "BIWEEKLY" -> c.add(Calendar.WEEK_OF_YEAR, 2)
                    "YEARLY" -> c.add(Calendar.YEAR, 1)
                    else -> c.add(Calendar.MONTH, 1)
                }
            }

            onConfirm(items)
            return true
        }
        return false
    }

    AlertDialog(
        onDismissRequest = attemptDismiss,
        title = { Text("Novo Lembrete", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError != null) nameError = null
                    },
                    label = { Text("O que pagar?") },
                    placeholder = { Text("Ex: Fatura de energia, Aluguel") },
                    isError = nameError != null,
                    supportingText = {
                        nameError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = valueStr,
                    onValueChange = {
                        valueStr = it.replace(',', '.')
                        if (valueError != null) valueError = null
                    },
                    label = { Text("Valor (R$)") },
                    placeholder = { Text("Ex: 120.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = valueError != null,
                    supportingText = {
                        valueError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = {
                        date = it
                        if (dateError != null) dateError = null
                    },
                    label = { Text("Data do 1º vencimento (DD/MM/AAAA)") },
                    placeholder = { Text("DD/MM/AAAA") },
                    isError = dateError != null,
                    supportingText = {
                        dateError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Forma de pagamento prevista
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Forma de pagamento prevista",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Saldo em conta", "Dinheiro").forEach { method ->
                            FilterChip(
                                selected = paymentMethod == method,
                                onClick = { paymentMethod = method },
                                label = { Text(method, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }

                // Recorrência
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Lembrete recorrente?",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Repete periodicamente",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isRecurring,
                                onCheckedChange = { isRecurring = it }
                            )
                        }

                        if (isRecurring) {
                            // Frequência
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Frequência",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(
                                        "Mensal" to "MONTHLY",
                                        "Semanal" to "WEEKLY",
                                        "Quinzenal" to "BIWEEKLY",
                                        "Anual" to "YEARLY"
                                    ).forEach { (label, freq) ->
                                        FilterChip(
                                            selected = recurrenceFrequency == freq,
                                            onClick = { recurrenceFrequency = freq },
                                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                                        )
                                    }
                                }
                            }

                            // Quantidade de repetições
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = repsInput,
                                    onValueChange = { input ->
                                        if (input.all { it.isDigit() } && input.length <= 3) {
                                            repsInput = input
                                        }
                                    },
                                    label = { Text("Quantidade de parcelas/vezes") },
                                    placeholder = { Text("Ex: 6") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    enabled = !isContinuous,
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                FilterChip(
                                    selected = isContinuous,
                                    onClick = { isContinuous = !isContinuous },
                                    label = { Text("Repetir continuamente (sem limite)", style = MaterialTheme.typography.labelSmall) },
                                    leadingIcon = if (isContinuous) {
                                        { Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                            }

                            // Previsão de término
                            val currentRepsDisplay = repsInput.toIntOrNull()?.coerceAtLeast(1) ?: 1
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isContinuous) "🔄 Repetição contínua (12 ocorrências iniciais)"
                                    else "📅 Previsão de término: ${projectedEndDate ?: "--"} ($currentRepsDisplay parcela${if (currentRepsDisplay > 1) "s" else ""})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }

                // Notificações locais
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Notificações",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "No dia do vencimento (09:00)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Switch(
                                checked = notifyOnDueDate,
                                onCheckedChange = { notifyOnDueDate = it }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "1 dia antes (09:00)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Switch(
                                checked = notifyOneDayBefore,
                                onCheckedChange = { notifyOneDayBefore = it }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { validateAndSave() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = attemptDismiss) { Text("Cancelar") }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )

    if (showUnsavedDialog) {
        com.example.ui.components.UnsavedChangesConfirmationDialog(
            onDismissRequest = { showUnsavedDialog = false },
            onDiscard = {
                showUnsavedDialog = false
                onDismiss()
            },
            onSaveAndExit = {
                if (validateAndSave()) {
                    showUnsavedDialog = false
                } else {
                    showUnsavedDialog = false
                }
            }
        )
    }
}

@Composable
private fun SummaryMiniCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    testTag: String? = null,
    valueTestTag: String? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = AppShapes.Medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = if (valueTestTag != null) Modifier.testTag(valueTestTag) else Modifier
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

