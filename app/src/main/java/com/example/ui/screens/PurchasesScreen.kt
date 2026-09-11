package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.ui.graphics.Color
import androidx.activity.compose.BackHandler
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.SheetState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.style.TextOverflow
import com.example.ui.components.SubscriptionBrandIcon
import com.example.data.subscription.SubscriptionRegistry
import com.example.ui.components.SubscriptionSelectionDialog
import com.example.ui.components.UnsavedChangesConfirmationDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.rememberUpdatedState
import com.example.data.bank.BankRegistry
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.CardPaymentItem
import com.example.data.DailyExpense
import com.example.data.DataSource
import com.example.data.PaymentMethod
import com.example.data.PurchaseItem
import com.example.data.CardItem
import com.example.data.InstallmentCalculation
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.StatusFeedbackBanner
import com.example.ui.theme.DividerColor
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.SuccessGreen
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

import androidx.compose.material.icons.outlined.Repeat
import com.example.data.SubscriptionItem
import androidx.compose.foundation.layout.IntrinsicSize

import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchasesScreen(
    purchases: List<PurchaseItem>,
    cards: List<CardItem>,
    cardPayments: List<CardPaymentItem> = emptyList(),
    dailyExpenses: List<DailyExpense> = emptyList(),
    subscriptions: List<SubscriptionItem> = emptyList(),
    onUpdatePurchases: (List<PurchaseItem>) -> Unit,
    onUpdateDailyExpenses: (List<DailyExpense>) -> Unit = {},
    onDeleteDailyExpense: ((DailyExpense) -> Unit)? = null,
    onUpdateSubscriptions: (List<SubscriptionItem>) -> Unit = {},
    onUpdateCards: (List<CardItem>) -> Unit = {},
    onRegisterInvoicePayment: (CardItem, Int, Int) -> Unit = { _, _, _ -> },
    statusMessage: String? = null,
    isErrorStatus: Boolean = false,
    initialTabIndex: Int = 0,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableStateOf(initialTabIndex) } // 0: Por Cartão, 1: Dia a Dia, 2: Assinatura
    androidx.compose.runtime.LaunchedEffect(initialTabIndex) {
        selectedTabIndex = initialTabIndex
    }
    var purchasesFilter by remember { mutableStateOf("Todas") } // "Todas", "Ativas", "Quitadas"
    var isHistoryExpanded by remember { mutableStateOf(false) } // Recolhido por padrão
    var cardForPaymentsHistory by remember { mutableStateOf<CardItem?>(null) }
    var expandedCardNames by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showCardSettings by remember { mutableStateOf(false) }

    var showAddPurchaseDialog by remember { mutableStateOf(false) }
    var purchaseToEdit by remember { mutableStateOf<PurchaseItem?>(null) }
    var purchaseToDelete by remember { mutableStateOf<PurchaseItem?>(null) }

    var showAddDailyExpenseDialog by remember { mutableStateOf(false) }
    var dailyExpenseToEdit by remember { mutableStateOf<DailyExpense?>(null) }
    var dailyExpenseToDelete by remember { mutableStateOf<DailyExpense?>(null) }

    var showAddSubscriptionDialog by remember { mutableStateOf(false) }
    var subscriptionToEdit by remember { mutableStateOf<SubscriptionItem?>(null) }
    var subscriptionToDelete by remember { mutableStateOf<SubscriptionItem?>(null) }

    val scope = rememberCoroutineScope()
    val cardSettingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val currentCalForFilter = Calendar.getInstance()
    val curM = currentCalForFilter.get(Calendar.MONTH) + 1
    val curY = currentCalForFilter.get(Calendar.YEAR)

    val activePurchases = remember(purchases) {
        purchases.filter { !it.isCurrentlyQuitada() }
    }
    val quitadasPurchases = remember(purchases) {
        purchases.filter { it.isCurrentlyQuitada() }
    }

    // Filtragem de compras por cartão respeitando compras futuras e apenas compras vinculadas a cartão
    val groupedActivePurchases = remember(activePurchases) {
        activePurchases
            .filter { p ->
                val hasCard = p.cardId.isNotBlank() || (p.cardName.isNotBlank() && p.cardName != "Sem cartão")
                // Só mostra na lista se a compra já começou (ou se já foi paga alguma parcela)
                val monthsDiff = (curY - p.startYear) * 12 + (curM - p.startMonth)
                hasCard && (monthsDiff >= 0 || p.paidInstallmentsCount > 0)
            }
            .groupBy { if (it.cardName.isNotBlank()) it.cardName else "Sem cartão" }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("purchases_screen")
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gastos",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.ExtraBold
                )
                IconButton(
                    onClick = { showCardSettings = true },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("purchases_header_card_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Configurações de cartões",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Abas de navegação interna: Por Cartão vs Dia a Dia vs Assinatura
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PrimaryAccent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            text = "Por Cartão",
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    modifier = Modifier.testTag("tab_cartoes")
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            text = "Dia a Dia",
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    modifier = Modifier.testTag("tab_dia_a_dia")
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = {
                        Text(
                            text = "Assinatura",
                            fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Normal,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    modifier = Modifier.testTag("tab_assinaturas")
                )
            }

            if (selectedTabIndex == 0) {
                // Filtros Simples na Aba Cartões (Sprint 13)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = purchasesFilter == "Todas",
                        onClick = { purchasesFilter = "Todas" },
                        label = { Text("Todas") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = PrimaryAccent
                        ),
                        modifier = Modifier.testTag("filter_todas")
                    )
                    FilterChip(
                        selected = purchasesFilter == "Ativas",
                        onClick = { purchasesFilter = "Ativas" },
                        label = { Text("Ativas (${activePurchases.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = PrimaryAccent
                        ),
                        modifier = Modifier.testTag("filter_ativas")
                    )
                    FilterChip(
                        selected = purchasesFilter == "Quitadas",
                        onClick = { purchasesFilter = "Quitadas" },
                        label = { Text("Quitadas (${quitadasPurchases.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = PrimaryAccent
                        ),
                        modifier = Modifier.testTag("filter_quitadas")
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (purchases.isEmpty()) {
                        item(key = "empty_purchases") {
                            EmptyStateCard(
                                icon = Icons.Outlined.ShoppingBag,
                                title = "Nenhuma compra registrada",
                                description = "Clique no botão abaixo para cadastrar uma nova compra de cartão."
                            )
                        }
                    } else {
                        // 1. Seção de Compras Ativas (Cartões como menus expansíveis)
                        if (purchasesFilter == "Todas" || purchasesFilter == "Ativas") {
                            if (activePurchases.isEmpty()) {
                                item(key = "empty_active_purchases") {
                                    EmptyStateCard(
                                        icon = Icons.Outlined.ShoppingBag,
                                        title = "Nenhuma compra ativa",
                                        description = "Todas as suas compras cadastradas já foram quitadas."
                                    )
                                }
                            } else {
                                groupedActivePurchases.forEach { (cardName, cardPurchases) ->
                                    item(key = "card_accordion_$cardName") {
                                        val linkedCard = cards.find { it.name == cardName || it.id == cardPurchases.firstOrNull()?.cardId }
                                        val isExpanded = expandedCardNames.contains(cardName)
                                        val currentCal = Calendar.getInstance()
                                        val currentMonth = currentCal.get(Calendar.MONTH) + 1
                                        val currentYear = currentCal.get(Calendar.YEAR)
                                        val isCardPaidThisMonth = cardPayments.any {
                                            it.cardId == linkedCard?.id && it.month == currentMonth && it.year == currentYear && it.paid
                                        }
                                        val curMonthName = remember(currentMonth) {
                                            val cal = Calendar.getInstance()
                                            cal.set(Calendar.MONTH, currentMonth - 1)
                                            SimpleDateFormat("MMMM", Locale("pt", "BR")).format(cal.time)
                                                .replaceFirstChar { it.uppercase() }
                                        }
                                        val nextMonthName = remember(currentMonth) {
                                            val cal = Calendar.getInstance()
                                            cal.set(Calendar.MONTH, currentMonth % 12)
                                            SimpleDateFormat("MMMM", Locale("pt", "BR")).format(cal.time)
                                                .replaceFirstChar { it.uppercase() }
                                        }
                                        val cardMonthlyTotal = cardPurchases.sumOf { p -> p.calculateInstallments(isCurrentInvoicePaid = isCardPaidThisMonth).installmentValue }

                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp)
                                                .animateContentSize(),
                                            shape = RoundedCornerShape(24.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surface
                                            ),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(20.dp)
                                             ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            expandedCardNames = if (isExpanded) expandedCardNames - cardName else expandedCardNames + cardName
                                                        },
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        val standardBank = remember(linkedCard) {
                                                            BankRegistry.getBankForCard(linkedCard?.name, linkedCard?.bankId, linkedCard?.colorHex)
                                                        }
                                                        Box(
                                                            modifier = Modifier
                                                                .size(44.dp)
                                                                .clip(RoundedCornerShape(12.dp))
                                                                .background(
                                                                    if (standardBank.logoResId != null) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                                    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                                                ),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            if (standardBank.logoResId != null) {
                                                                Image(
                                                                    painter = painterResource(id = standardBank.logoResId),
                                                                    contentDescription = standardBank.displayName,
                                                                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                                                                )
                                                            } else {
                                                                Icon(
                                                                    imageVector = Icons.Outlined.CreditCard,
                                                                    contentDescription = null,
                                                                    tint = MaterialTheme.colorScheme.primary
                                                                )
                                                            }
                                                        }
                                                        Column {
                                                            Text(
                                                                text = cardName,
                                                                style = MaterialTheme.typography.titleMedium,
                                                                color = MaterialTheme.colorScheme.onSurface,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            Text(
                                                                text = if (isCardPaidThisMonth) "Fatura de $curMonthName Paga" else "Em aberto",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = if (isCardPaidThisMonth) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                                                fontWeight = if (isCardPaidThisMonth) FontWeight.SemiBold else FontWeight.Normal
                                                            )
                                                            Text(
                                                                text = "${cardPurchases.size} compra${if (cardPurchases.size > 1) "s" else ""} • R$ ${String.format(Locale("pt", "BR"), "%.2f", cardMonthlyTotal)} /mês",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                                            )
                                                        }
                                                    }

                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        if (linkedCard != null) {
                                                            IconButton(
                                                                onClick = { cardForPaymentsHistory = linkedCard },
                                                                modifier = Modifier.testTag("card_history_button_${linkedCard.id}")
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Outlined.History,
                                                                    contentDescription = "Histórico de faturas",
                                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                                )
                                                            }
                                                        }

                                                        Icon(
                                                            imageVector = if (isExpanded) Icons.Outlined.KeyboardArrowDown else Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                                            contentDescription = if (isExpanded) "Recolher" else "Expandir",
                                                            tint = MaterialTheme.colorScheme.onSurface,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }

                                                if (isExpanded) {
                                                    Spacer(modifier = Modifier.height(16.dp))
                                                    HorizontalDivider(
                                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                                    )
                                                    Spacer(modifier = Modifier.height(12.dp))

                                                    Column(
                                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        cardPurchases.forEachIndexed { index, purchase ->
                                                            PurchaseRowItem(
                                                                purchase = purchase,
                                                                isCardPaidThisMonth = isCardPaidThisMonth,
                                                                onEdit = { purchaseToEdit = purchase },
                                                                onDelete = { purchaseToDelete = purchase }
                                                            )
                                                            if (index < cardPurchases.size - 1) {
                                                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                                                            }
                                                        }

                                                        if (linkedCard != null) {
                                                            Spacer(modifier = Modifier.height(12.dp))
                                                            Button(
                                                                onClick = { onRegisterInvoicePayment(linkedCard, curM, curY) },
                                                                enabled = !isCardPaidThisMonth,
                                                                colors = ButtonDefaults.buttonColors(
                                                                    containerColor = if (isCardPaidThisMonth) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                                                                    contentColor = if (isCardPaidThisMonth) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                                                                ),
                                                                shape = RoundedCornerShape(12.dp),
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .height(48.dp)
                                                                    .testTag("pay_invoice_button_${linkedCard.id}")
                                                            ) {
                                                                Icon(
                                                                    imageVector = if (isCardPaidThisMonth) Icons.Outlined.CheckCircle else Icons.Outlined.Payments,
                                                                    contentDescription = null,
                                                                    modifier = Modifier.size(20.dp)
                                                                )
                                                                Spacer(modifier = Modifier.size(10.dp))
                                                                Text(
                                                                    text = if (isCardPaidThisMonth) "Fatura de $curMonthName Paga" else "Pagar Fatura de $curMonthName",
                                                                    style = MaterialTheme.typography.bodyLarge,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Seção de Histórico (Compras Quitadas)
                        if (purchasesFilter == "Todas" || purchasesFilter == "Quitadas") {
                            item(key = "history_section_header") {
                                val isForceExpanded = purchasesFilter == "Quitadas"
                                val expanded = isForceExpanded || isHistoryExpanded

                                Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                                    HorizontalDivider(color = DividerColor)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("history_header_toggle")
                                            .clickable {
                                                if (!isForceExpanded) {
                                                    isHistoryExpanded = !isHistoryExpanded
                                                }
                                            }
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (expanded) Icons.Outlined.KeyboardArrowDown else Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onBackground
                                            )
                                            Text(
                                                text = "Histórico",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                            SuggestionChip(
                                                onClick = {},
                                                label = {
                                                    Text(
                                                        text = "${quitadasPurchases.size} compras",
                                                        style = MaterialTheme.typography.labelSmall
                                                    )
                                                },
                                                colors = SuggestionChipDefaults.suggestionChipColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                    labelColor = TextSecondary
                                                ),
                                                border = null
                                            )
                                        }
                                    }
                                }
                            }

                            if (purchasesFilter == "Quitadas" || isHistoryExpanded) {
                                if (quitadasPurchases.isEmpty()) {
                                    item(key = "empty_quitadas_purchases") {
                                        EmptyStateCard(
                                            icon = Icons.Outlined.ShoppingBag,
                                            title = "Nenhuma compra quitada",
                                            description = "O histórico de compras quitadas aparecerá aqui."
                                        )
                                    }
                                } else {
                                    itemsIndexed(quitadasPurchases, key = { _, purchase -> "quitada_${purchase.id}" }) { index, purchase ->
                                        QuitadaPurchaseRowItem(
                                            purchase = purchase,
                                            onEdit = { purchaseToEdit = purchase },
                                            onDelete = { purchaseToDelete = purchase }
                                        )
                                        if (index < quitadasPurchases.size - 1) {
                                            HorizontalDivider(color = DividerColor)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (selectedTabIndex == 1) {
                // Conteúdo da Aba 1: Dia a Dia
                if (dailyExpenses.isEmpty()) {
                    EmptyStateCard(
                        icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                        title = "Nenhum gasto diário registrado",
                        description = "Clique no botão abaixo para cadastrar um novo gasto do dia a dia."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(dailyExpenses, key = { _, expense -> expense.id }) { index, expense ->
                            DailyExpenseRowItem(
                                expense = expense,
                                onEdit = { dailyExpenseToEdit = expense },
                                onDelete = { dailyExpenseToDelete = expense }
                            )
                            if (index < dailyExpenses.size - 1) {
                                HorizontalDivider(color = DividerColor)
                            }
                        }
                    }
                }
            } else {
                // Conteúdo da Aba 2: Assinatura
                if (subscriptions.isEmpty()) {
                    EmptyStateCard(
                        icon = Icons.Outlined.Repeat,
                        title = "Nenhuma assinatura cadastrada",
                        description = "Clique no botão abaixo para cadastrar um serviço recorrente."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        items(subscriptions, key = { it.id }) { sub ->
                            SubscriptionRowItem(
                                subscription = sub,
                                onEdit = { subscriptionToEdit = sub },
                                onDelete = { subscriptionToDelete = sub }
                            )
                        }
                    }
                }
            }
        }

        // FAB Único e Adaptável conforme a aba selecionada
        ExtendedFloatingActionButton(
            onClick = {
                when (selectedTabIndex) {
                    0 -> showAddPurchaseDialog = true
                    1 -> showAddDailyExpenseDialog = true
                    2 -> showAddSubscriptionDialog = true
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null
                )
            },
            text = {
                Text(
                    text = when (selectedTabIndex) {
                        0 -> "Cadastrar Compra"
                        1 -> "Cadastrar Gasto"
                        2 -> "Cadastrar Assin."
                        else -> "Cadastrar"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            containerColor = PrimaryAccent,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("universal_add_button")
        )
    }

    // Card Settings Sheet
    if (showCardSettings) {
        ModalBottomSheet(
            onDismissRequest = { showCardSettings = false },
            sheetState = cardSettingsSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            CardSettingsContent(
                cards = cards,
                onUpdateCards = onUpdateCards,
                onDismiss = {
                    scope.launch { cardSettingsSheetState.hide() }.invokeOnCompletion { showCardSettings = false }
                }
            )
        }
    }

    // Add / Edit Purchase Sheet
    if (showAddPurchaseDialog || purchaseToEdit != null) {
        PurchaseBottomSheet(
            availableCards = cards,
            initialPurchase = purchaseToEdit,
            onDismiss = {
                showAddPurchaseDialog = false
                purchaseToEdit = null
            },
            onConfirm = { purchase ->
                if (purchaseToEdit != null) {
                    onUpdatePurchases(purchases.map { if (it.id == purchase.id) purchase else it })
                } else {
                    onUpdatePurchases(purchases + purchase)
                }
                showAddPurchaseDialog = false
                purchaseToEdit = null
            }
        )
    }

    // Add / Edit Daily Expense Sheet
    if (showAddDailyExpenseDialog || dailyExpenseToEdit != null) {
        DailyExpenseBottomSheet(
            initialExpense = dailyExpenseToEdit,
            onDismiss = {
                showAddDailyExpenseDialog = false
                dailyExpenseToEdit = null
            },
            onConfirm = { expense ->
                if (dailyExpenseToEdit != null) {
                    onUpdateDailyExpenses(dailyExpenses.map { if (it.id == expense.id) expense else it })
                } else {
                    onUpdateDailyExpenses(dailyExpenses + expense)
                }
                showAddDailyExpenseDialog = false
                dailyExpenseToEdit = null
            }
        )
    }

    // Add / Edit Subscription Sheet
    if (showAddSubscriptionDialog || subscriptionToEdit != null) {
        SubscriptionBottomSheet(
            availableCards = cards,
            initialSubscription = subscriptionToEdit,
            onDismiss = {
                showAddSubscriptionDialog = false
                subscriptionToEdit = null
            },
            onConfirm = { sub ->
                if (subscriptionToEdit != null) {
                    onUpdateSubscriptions(subscriptions.map { if (it.id == sub.id) sub else it })
                } else {
                    onUpdateSubscriptions(subscriptions + sub)
                }
                showAddSubscriptionDialog = false
                subscriptionToEdit = null
            },
            onDelete = if (subscriptionToEdit != null) {
                {
                    val toDelete = subscriptionToEdit
                    showAddSubscriptionDialog = false
                    subscriptionToEdit = null
                    subscriptionToDelete = toDelete
                }
            } else null
        )
    }

    // Delete Confirmation Dialogs
    purchaseToDelete?.let { purchase ->
        AlertDialog(
            onDismissRequest = { purchaseToDelete = null },
            title = { Text("Excluir Compra", style = MaterialTheme.typography.titleMedium) },
            text = { Text("Deseja realmente excluir a compra \"${purchase.name}\"?", style = MaterialTheme.typography.bodyLarge) },
            confirmButton = {
                Button(
                    onClick = {
                        val updatedList = purchases.filter { it.id != purchase.id }
                        onUpdatePurchases(updatedList)
                        purchaseToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.testTag("confirm_delete_purchase_button")
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { purchaseToDelete = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    dailyExpenseToDelete?.let { expense ->
        AlertDialog(
            onDismissRequest = { dailyExpenseToDelete = null },
            title = { Text("Excluir Gasto Diário", style = MaterialTheme.typography.titleMedium) },
            text = { Text("Deseja realmente excluir o gasto \"${expense.name}\"?", style = MaterialTheme.typography.bodyLarge) },
            confirmButton = {
                Button(
                    onClick = {
                        if (onDeleteDailyExpense != null) {
                            onDeleteDailyExpense(expense)
                        } else {
                            val updatedList = dailyExpenses.filter { it.id != expense.id }
                            onUpdateDailyExpenses(updatedList)
                        }
                        dailyExpenseToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.testTag("confirm_delete_daily_expense_button")
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { dailyExpenseToDelete = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    subscriptionToDelete?.let { sub ->
        AlertDialog(
            onDismissRequest = { subscriptionToDelete = null },
            title = { Text("Excluir Assinatura", style = MaterialTheme.typography.titleMedium) },
            text = { Text("Deseja realmente excluir a assinatura \"${sub.name}\"?", style = MaterialTheme.typography.bodyLarge) },
            confirmButton = {
                Button(
                    onClick = {
                        val updatedList = subscriptions.filter { it.id != sub.id }
                        onUpdateSubscriptions(updatedList)
                        subscriptionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.testTag("confirm_delete_subscription_button")
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { subscriptionToDelete = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Card Payments History Dialog
    cardForPaymentsHistory?.let { card ->
        CardPaymentsHistoryDialog(
            cardName = card.name,
            cardId = card.id,
            cardPayments = cardPayments,
            onDismiss = { cardForPaymentsHistory = null }
        )
    }
}

@Composable
private fun CardSettingsContent(
    cards: List<CardItem>,
    onUpdateCards: (List<CardItem>) -> Unit,
    onDismiss: () -> Unit
) {
    var cardToEdit by remember { mutableStateOf<CardItem?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var cardToDelete by remember { mutableStateOf<CardItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Configurações de Cartões", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Outlined.Add, contentDescription = "Novo Cartão")
            }
        }

        if (cards.isEmpty()) {
            Text("Nenhum cartão cadastrado.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                itemsIndexed(cards) { index, card ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val standardBank = remember(card) {
                            BankRegistry.getBankForCard(card.name, card.bankId, card.colorHex)
                        }
                        if (standardBank.logoResId != null) {
                            Image(
                                painter = painterResource(id = standardBank.logoResId),
                                contentDescription = standardBank.displayName,
                                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(android.graphics.Color.parseColor(card.colorHex ?: "#CCCCCC")))
                            )
                        }
                        Text(card.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        IconButton(onClick = { cardToEdit = card }) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Editar", modifier = Modifier.size(20.dp))
                        }
                        IconButton(onClick = { cardToDelete = card }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                        }
                    }
                    if (index < cards.size - 1) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
        
        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("Concluído")
        }
    }

    if (showAddDialog || cardToEdit != null) {
        com.example.ui.components.CardFormDialog(
            title = if (cardToEdit != null) "Editar Cartão" else "Novo Cartão",
            initialName = cardToEdit?.name ?: "",
            initialColorHex = cardToEdit?.colorHex,
            initialBankId = cardToEdit?.bankId,
            onDismiss = {
                showAddDialog = false
                cardToEdit = null
            },
            onConfirm = { name, color, bankId ->
                if (cardToEdit != null) {
                    onUpdateCards(cards.map { if (it.id == cardToEdit!!.id) it.copy(name = name, colorHex = color, bankId = bankId) else it })
                } else {
                    onUpdateCards(cards + CardItem(name = name, colorHex = color, bankId = bankId))
                }
                showAddDialog = false
                cardToEdit = null
            }
        )
    }

    cardToDelete?.let { card ->
        AlertDialog(
            onDismissRequest = { cardToDelete = null },
            title = { Text("Excluir Cartão") },
            text = { Text("Deseja realmente excluir o cartão \"${card.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    onUpdateCards(cards.filter { it.id != card.id })
                    cardToDelete = null
                }) { Text("Excluir", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { cardToDelete = null }) { Text("Cancelar") }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun PurchaseRowItem(
    purchase: PurchaseItem,
    isCardPaidThisMonth: Boolean = false,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val calc = remember(purchase, isCardPaidThisMonth) {
        purchase.calculateInstallments(isCurrentInvoicePaid = isCardPaidThisMonth)
    }
    val (effectiveInst, effectiveMonth) = remember(purchase, isCardPaidThisMonth) {
        purchase.getEffectiveInstallmentDisplay(isCardPaidThisMonth)
    }
    val isQuitado = purchase.isQuitada ||
        calc.status == "Quitado" ||
        (purchase.isInstallment && (purchase.paidInstallmentsCount >= purchase.totalInstallments || effectiveInst > purchase.totalInstallments)) ||
        (!purchase.isInstallment && isCardPaidThisMonth)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .testTag("purchase_item_${purchase.id}"),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = purchase.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (purchase.isInstallment && !isQuitado) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "$effectiveInst/${purchase.totalInstallments}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = if (isQuitado) "Quitada" else if (purchase.isInstallment) "Parcela de $effectiveMonth" else "À vista",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCardPaidThisMonth && purchase.isInstallment && !isQuitado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        val instVal = if (purchase.isInstallment && purchase.totalInstallments > 0) purchase.totalAmount / purchase.totalInstallments else purchase.totalAmount
        val displayAmount = if (purchase.isInstallment) {
            if (isQuitado && calc.installmentValue == 0.0) instVal else calc.installmentValue
        } else {
            purchase.totalAmount
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", displayAmount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp).testTag("edit_purchase_${purchase.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp).testTag("delete_purchase_${purchase.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Excluir",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseBottomSheet(
    availableCards: List<CardItem>,
    initialPurchase: PurchaseItem?,
    onDismiss: () -> Unit,
    onConfirm: (PurchaseItem) -> Unit
) {
    val scope = rememberCoroutineScope()
    val currentCal = remember { Calendar.getInstance() }
    val currentM = currentCal.get(Calendar.MONTH) + 1
    val currentY = currentCal.get(Calendar.YEAR)

    var name by remember { mutableStateOf(initialPurchase?.name ?: "") }
    var totalAmountStr by remember {
        mutableStateOf(initialPurchase?.let { String.format(Locale.US, "%.2f", it.totalAmount) } ?: "")
    }
    var selectedCardId by remember { mutableStateOf(initialPurchase?.cardId ?: (availableCards.firstOrNull()?.id ?: "")) }
    var selectedCardName by remember {
        mutableStateOf(
            initialPurchase?.cardName
                ?: availableCards.firstOrNull { it.id == selectedCardId }?.name
                ?: if (availableCards.isNotEmpty()) availableCards.first().name else "Cartão Geral"
        )
    }

    var isInstallment by remember { mutableStateOf(initialPurchase?.isInstallment ?: false) }
    var totalInstallmentsStr by remember { mutableStateOf(initialPurchase?.totalInstallments?.toString() ?: "2") }
    var startMonthYearStr by remember {
        mutableStateOf(
            initialPurchase?.let { String.format(Locale.ROOT, "%02d/%04d", it.startMonth, it.startYear) }
                ?: String.format(Locale.ROOT, "%02d/%04d", currentM, currentY)
        )
    }

    var isDropdownExpanded by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var cardError by remember { mutableStateOf<String?>(null) }
    var installmentsError by remember { mutableStateOf<String?>(null) }
    var startDateError by remember { mutableStateOf<String?>(null) }
    var isQuitadaManual by remember { mutableStateOf(initialPurchase?.isCurrentlyQuitada() ?: false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    val hasUnsavedChanges = remember(
        name, totalAmountStr, selectedCardId, isInstallment, totalInstallmentsStr, startMonthYearStr, isQuitadaManual
    ) {
        if (initialPurchase == null) {
            name.isNotBlank() || totalAmountStr.isNotBlank() || isInstallment
        } else {
            name != initialPurchase.name ||
                totalAmountStr != String.format(Locale.US, "%.2f", initialPurchase.totalAmount) ||
                selectedCardId != initialPurchase.cardId ||
                isInstallment != initialPurchase.isInstallment ||
                (isInstallment && totalInstallmentsStr != initialPurchase.totalInstallments.toString()) ||
                startMonthYearStr != String.format(Locale.ROOT, "%02d/%04d", initialPurchase.startMonth, initialPurchase.startYear) ||
                isQuitadaManual != initialPurchase.isCurrentlyQuitada()
        }
    }

    val currentHasUnsavedChanges by rememberUpdatedState(hasUnsavedChanges)

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue ->
            if (sheetValue == SheetValue.Hidden && currentHasUnsavedChanges) {
                showUnsavedDialog = true
                false
            } else {
                true
            }
        }
    )

    fun closeSheet(onClosed: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            onClosed()
        }
    }

    val attemptDismiss = {
        if (currentHasUnsavedChanges) {
            showUnsavedDialog = true
        } else {
            closeSheet(onDismiss)
        }
    }

    androidx.activity.compose.BackHandler(enabled = currentHasUnsavedChanges) {
        showUnsavedDialog = true
    }

    fun validateAndSave(): Boolean {
        var isValid = true

        if (name.trim().isBlank()) {
            nameError = "Informe o nome da compra para continuar."
            isValid = false
        } else {
            nameError = null
        }

        val amount = totalAmountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            amountError = "Informe um valor total válido maior que zero."
            isValid = false
        } else {
            amountError = null
        }

        if (availableCards.isNotEmpty() && selectedCardId.isBlank()) {
            cardError = "Selecione um cartão para a compra."
            isValid = false
        } else {
            cardError = null
        }

        var totalInst = 1
        var month = currentM
        var year = currentY

        if (isInstallment) {
            val parsedInst = totalInstallmentsStr.toIntOrNull()
            if (parsedInst == null || parsedInst < 1) {
                installmentsError = "Informe pelo menos 1 parcela."
                isValid = false
            } else {
                installmentsError = null
                totalInst = parsedInst
            }

            val dateParts = startMonthYearStr.trim().split('/')
            if (dateParts.size != 2) {
                startDateError = "Informe no formato MM/AAAA (ex: 03/2026)."
                isValid = false
            } else {
                val parsedM = dateParts[0].toIntOrNull()
                val parsedY = dateParts[1].toIntOrNull()
                if (parsedM == null || parsedM !in 1..12 || parsedY == null || parsedY < 2000) {
                    startDateError = "Mês (1-12) ou Ano inválido no formato MM/AAAA."
                    isValid = false
                } else {
                    startDateError = null
                    month = parsedM
                    year = parsedY
                }
            }
        }

        if (isValid && amount != null) {
            val paidCount = if (isQuitadaManual) {
                if (isInstallment) totalInst else 1
            } else {
                if (initialPurchase?.isQuitada == true) {
                    if (isInstallment) (initialPurchase.paidInstallmentsCount.coerceAtMost(totalInst - 1)).coerceAtLeast(0) else 0
                } else {
                    initialPurchase?.paidInstallmentsCount ?: 0
                }
            }
            val isNowQuitada = if (initialPurchase != null) {
                isQuitadaManual
            } else {
                isInstallment && paidCount >= totalInst
            }
            val nowCompletedAt = if (isNowQuitada) {
                initialPurchase?.completedAt ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)
            } else {
                null
            }

            val finalPurchase = PurchaseItem(
                id = initialPurchase?.id ?: UUID.randomUUID().toString(),
                name = name.trim(),
                totalAmount = amount,
                cardId = selectedCardId,
                cardName = selectedCardName,
                isInstallment = isInstallment,
                totalInstallments = totalInst,
                startMonth = month,
                startYear = year,
                paidInstallmentsCount = paidCount,
                isQuitada = isNowQuitada,
                completedAt = nowCompletedAt,
                source = initialPurchase?.source ?: DataSource.MANUAL
            )
            closeSheet { onConfirm(finalPurchase) }
            return true
        }
        return false
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (currentHasUnsavedChanges) {
                showUnsavedDialog = true
            } else {
                closeSheet(onDismiss)
            }
        },
        sheetState = sheetState,
        properties = ModalBottomSheetDefaults.properties(
            shouldDismissOnBackPress = false
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (initialPurchase != null) "Editar Compra" else "Nova Compra",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

        // Nome
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                if (nameError != null) nameError = null
            },
            label = { Text("Nome da compra", style = MaterialTheme.typography.bodySmall) },
            placeholder = { Text("Ex: Supermercado, Eletrônicos...") },
            isError = nameError != null,
            supportingText = {
                nameError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryAccent,
                focusedLabelColor = PrimaryAccent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("purchase_name_input")
        )

        // Valor total
        OutlinedTextField(
            value = totalAmountStr,
            onValueChange = {
                totalAmountStr = it.replace(',', '.')
                if (amountError != null) amountError = null
            },
            label = { Text("Valor total (R$)", style = MaterialTheme.typography.bodySmall) },
            placeholder = { Text("Ex: 150.00") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = amountError != null,
            supportingText = {
                amountError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryAccent,
                focusedLabelColor = PrimaryAccent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("purchase_amount_input")
        )

        // Cartão associado
        ExposedDropdownMenuBox(
            expanded = isDropdownExpanded,
            onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
        ) {
            OutlinedTextField(
                value = selectedCardName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Cartão associado", style = MaterialTheme.typography.bodySmall) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                isError = cardError != null,
                supportingText = {
                    cardError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryAccent,
                    focusedLabelColor = PrimaryAccent
                ),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .testTag("purchase_card_dropdown")
            )

            ExposedDropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false }
            ) {
                if (availableCards.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Nenhum cartão cadastrado", style = MaterialTheme.typography.bodyMedium) },
                        onClick = { isDropdownExpanded = false }
                    )
                } else {
                    availableCards.forEach { card ->
                        DropdownMenuItem(
                            text = { Text(card.name, style = MaterialTheme.typography.bodyMedium) },
                            onClick = {
                                selectedCardId = card.id
                                selectedCardName = card.name
                                isDropdownExpanded = false
                                if (cardError != null) cardError = null
                            }
                        )
                    }
                }
            }
        }

        // Compra parcelada (Sim ou Não)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Compra parcelada?",
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
            Switch(
                checked = isInstallment,
                onCheckedChange = { isInstallment = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PrimaryAccent
                ),
                modifier = Modifier.testTag("purchase_installment_switch")
            )
        }

        if (isInstallment) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Quantidade total de parcelas
                OutlinedTextField(
                    value = totalInstallmentsStr,
                    onValueChange = {
                        totalInstallmentsStr = it
                        if (installmentsError != null) installmentsError = null
                    },
                    label = { Text("Parcelas", style = MaterialTheme.typography.bodySmall) },
                    placeholder = { Text("Ex: 10") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = installmentsError != null,
                    supportingText = {
                        installmentsError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryAccent,
                        focusedLabelColor = PrimaryAccent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("purchase_installments_count_input")
                )

                // Mês e ano da primeira parcela (MM/AAAA)
                OutlinedTextField(
                    value = startMonthYearStr,
                    onValueChange = {
                        startMonthYearStr = it
                        if (startDateError != null) startDateError = null
                    },
                    label = { Text("Início (MM/AAAA)", style = MaterialTheme.typography.bodySmall) },
                    placeholder = { Text("03/2026") },
                    singleLine = true,
                    isError = startDateError != null,
                    supportingText = {
                        startDateError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryAccent,
                        focusedLabelColor = PrimaryAccent
                    ),
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("purchase_start_date_input")
                )
            }
        }

        // Se estiver editando compra existente, permite marcar/desmarcar quitada diretamente
        if (initialPurchase != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Marcar como quitada",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "Move esta compra para o Histórico",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isQuitadaManual,
                    onCheckedChange = { isQuitadaManual = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = SuccessGreen
                    ),
                    modifier = Modifier.testTag("purchase_quitada_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = attemptDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar", color = TextSecondary)
            }
            Button(
                onClick = { validateAndSave() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryAccent
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(2f)
                    .height(48.dp)
                    .testTag("save_purchase_button")
            ) {
                Text("Salvar Compra", fontWeight = FontWeight.Bold)
            }
        }
    }
    }

    if (showUnsavedDialog) {
        UnsavedChangesConfirmationDialog(
            onDismissRequest = { showUnsavedDialog = false },
            onDiscard = {
                showUnsavedDialog = false
                closeSheet(onDismiss)
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
private fun QuitadaPurchaseRowItem(
    purchase: PurchaseItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDetailsDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDetailsDialog = true }
            .padding(vertical = 12.dp)
            .testTag("quitada_purchase_item_${purchase.id}"),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = purchase.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                fontWeight = FontWeight.Bold
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = purchase.cardName.ifBlank { "Sem cartão" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (purchase.isInstallment) {
                        "${purchase.totalInstallments} parcelas (${String.format(Locale.ROOT, "%02d/%04d", purchase.startMonth, purchase.startYear)})"
                    } else {
                        "À vista (${String.format(Locale.ROOT, "%02d/%04d", purchase.startMonth, purchase.startYear)})"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", purchase.totalAmount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp).testTag("edit_quitada_purchase_${purchase.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp).testTag("delete_quitada_purchase_${purchase.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Excluir",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }

    if (showDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showDetailsDialog = false },
            title = {
                Text(
                    text = "Detalhes da Compra Quitada",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Nome: ${purchase.name}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Cartão: ${purchase.cardName.ifBlank { "Sem cartão" }}", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "Valor Total: R$ ${String.format(Locale.forLanguageTag("pt-BR"), "%.2f", purchase.totalAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (purchase.isInstallment) {
                        val instVal = if (purchase.totalInstallments > 0) purchase.totalAmount / purchase.totalInstallments else purchase.totalAmount
                        Text(text = "Parcelamento: ${purchase.totalInstallments}x de R$ ${String.format(Locale.forLanguageTag("pt-BR"), "%.2f", instVal)}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Mês de Início: ${String.format(Locale.ROOT, "%02d/%04d", purchase.startMonth, purchase.startYear)}", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        Text(text = "Tipo: À vista", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Mês do Gasto: ${String.format(Locale.ROOT, "%02d/%04d", purchase.startMonth, purchase.startYear)}", style = MaterialTheme.typography.bodyMedium)
                    }
                    if (!purchase.completedAt.isNullOrBlank()) {
                        Text(text = "Quitada em: ${purchase.completedAt}", style = MaterialTheme.typography.bodySmall, color = SuccessGreen)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDetailsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent)
                ) {
                    Text("Fechar")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun CardPaymentsHistoryDialog(
    cardName: String,
    cardId: String,
    cardPayments: List<CardPaymentItem>,
    onDismiss: () -> Unit
) {
    val filteredPayments = remember(cardPayments, cardId) {
        cardPayments
            .filter { it.cardId == cardId }
            .sortedWith(compareByDescending<CardPaymentItem> { it.year }.thenByDescending { it.month })
            .take(10)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = null,
                    tint = PrimaryAccent
                )
                Text(
                    text = "Histórico de Faturas - $cardName",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (filteredPayments.isEmpty()) {
                    Text(
                        text = "Nenhum pagamento de fatura registrado para este cartão.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    Text(
                        text = "Últimos pagamentos registrados:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    filteredPayments.forEach { payment ->
                        val monthCal = Calendar.getInstance()
                        monthCal.set(Calendar.MONTH, payment.month - 1)
                        val monthName = SimpleDateFormat("MMMM", Locale("pt", "BR")).format(monthCal.time)
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pt", "BR")) else it.toString() }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = PrimaryAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "$monthName/${payment.year}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }
                            payment.paidAt?.let { pAt ->
                                val datePart = pAt.take(10)
                                Text(
                                    text = "Paga em $datePart",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                        HorizontalDivider(color = DividerColor)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar", color = PrimaryAccent, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun SubscriptionRowItem(
    subscription: SubscriptionItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit = {}
) {
    val brand = remember(subscription.name) {
        SubscriptionRegistry.getBrandForSubscription(subscription.name)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
            .testTag("subscription_item_${subscription.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Coluna da Esquerda: Ícone oficial da marca (44.dp) + Nome, Categoria, Cartão e Divisão
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                SubscriptionBrandIcon(
                    subscriptionName = subscription.name,
                    size = 44.dp
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = subscription.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    // Linha com chips/tags informativas
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Categoria Chip
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        ) {
                            Text(
                                text = brand.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Cartão Vinculado
                        if (subscription.cardName.isNotBlank()) {
                            Text(
                                text = "• ${subscription.cardName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Tag de Compartilhamento
                        if (subscription.isShared) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                            ) {
                                val sharedText = if (!subscription.sharedWith.isNullOrBlank()) {
                                    "Div. c/ ${subscription.sharedWith}"
                                } else {
                                    "Dividida"
                                }
                                Text(
                                    text = sharedText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Coluna da Direita: Preço mensal destacado e detalhamento de "Sua parte"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    val isSharedWithEffect = subscription.isShared && subscription.receivedAmount > 0
                    val displayValue = if (isSharedWithEffect) {
                        subscription.effectiveMonthlyValue
                    } else {
                        subscription.monthlyValue
                    }

                    Text(
                        text = String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", displayValue),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSharedWithEffect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )

                    if (isSharedWithEffect) {
                        Text(
                            text = String.format(Locale.forLanguageTag("pt-BR"), "Sua parte (Total R$ %.2f)", subscription.monthlyValue),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    } else {
                        Text(
                            text = "mensal",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp).testTag("delete_subscription_${subscription.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Excluir assinatura",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionBottomSheet(
    availableCards: List<CardItem>,
    initialSubscription: SubscriptionItem?,
    onDismiss: () -> Unit,
    onConfirm: (SubscriptionItem) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf(initialSubscription?.name ?: "") }
    var valueStr by remember {
        mutableStateOf(initialSubscription?.let { String.format(Locale.US, "%.2f", it.monthlyValue) } ?: "")
    }
    var selectedCardId by remember {
        mutableStateOf(initialSubscription?.cardId ?: (availableCards.firstOrNull()?.id ?: ""))
    }
    var selectedCardName by remember {
        mutableStateOf(
            initialSubscription?.cardName
                ?: availableCards.firstOrNull { it.id == selectedCardId }?.name
                ?: if (availableCards.isNotEmpty()) availableCards.first().name else ""
        )
    }

    var isShared by remember { mutableStateOf(initialSubscription?.isShared ?: false) }
    var sharedWith by remember { mutableStateOf(initialSubscription?.sharedWith ?: "") }
    var receivedAmountStr by remember {
        mutableStateOf(
            initialSubscription?.let {
                if (it.isShared && it.receivedAmount > 0) String.format(Locale.US, "%.2f", it.receivedAmount) else ""
            } ?: ""
        )
    }

    var isDropdownExpanded by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var valueError by remember { mutableStateOf<String?>(null) }
    var cardError by remember { mutableStateOf<String?>(null) }
    var sharedWithError by remember { mutableStateOf<String?>(null) }
    var receivedAmountError by remember { mutableStateOf<String?>(null) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var showSubscriptionPicker by remember { mutableStateOf(false) }

    val hasUnsavedChanges = remember(name, valueStr, selectedCardId, isShared, sharedWith, receivedAmountStr) {
        if (initialSubscription == null) {
            name.isNotBlank() || valueStr.isNotBlank() || isShared || sharedWith.isNotBlank() || receivedAmountStr.isNotBlank()
        } else {
            name != initialSubscription.name ||
                valueStr != String.format(Locale.US, "%.2f", initialSubscription.monthlyValue) ||
                selectedCardId != initialSubscription.cardId ||
                isShared != initialSubscription.isShared ||
                sharedWith != (initialSubscription.sharedWith ?: "") ||
                receivedAmountStr != (if (initialSubscription.isShared && initialSubscription.receivedAmount > 0) String.format(Locale.US, "%.2f", initialSubscription.receivedAmount) else "")
        }
    }

    val currentHasUnsavedChanges by rememberUpdatedState(hasUnsavedChanges)

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue ->
            if (sheetValue == SheetValue.Hidden && currentHasUnsavedChanges) {
                showUnsavedDialog = true
                false
            } else {
                true
            }
        }
    )

    fun closeSheet(onClosed: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            onClosed()
        }
    }

    val attemptDismiss = {
        if (currentHasUnsavedChanges) {
            showUnsavedDialog = true
        } else {
            closeSheet(onDismiss)
        }
    }

    BackHandler(enabled = currentHasUnsavedChanges) {
        showUnsavedDialog = true
    }

    fun validateAndSave(): Boolean {
        var isValid = true

        if (name.trim().isBlank()) {
            nameError = "Informe o nome da assinatura."
            isValid = false
        } else {
            nameError = null
        }

        val amount = valueStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            valueError = "Informe um valor mensal válido maior que zero."
            isValid = false
        } else {
            valueError = null
        }

        if (availableCards.isNotEmpty() && selectedCardId.isBlank()) {
            cardError = "Selecione um cartão para a assinatura."
            isValid = false
        } else {
            cardError = null
        }

        var recAmount = 0.0
        if (isShared) {
            if (sharedWith.trim().isBlank()) {
                sharedWithError = "Informe com quem divide."
                isValid = false
            } else {
                sharedWithError = null
            }

            val parsedRec = receivedAmountStr.toDoubleOrNull()
            if (parsedRec == null || parsedRec <= 0.0) {
                receivedAmountError = "Informe o valor pago pela outra pessoa."
                isValid = false
            } else if (amount != null && parsedRec >= amount) {
                receivedAmountError = "O valor deve ser menor que o total da assinatura."
                isValid = false
            } else {
                recAmount = parsedRec
                receivedAmountError = null
            }
        } else {
            sharedWithError = null
            receivedAmountError = null
        }

        if (isValid && amount != null) {
            val sub = SubscriptionItem(
                id = initialSubscription?.id ?: UUID.randomUUID().toString(),
                name = name.trim(),
                monthlyValue = amount,
                cardId = selectedCardId,
                cardName = selectedCardName,
                isShared = isShared,
                sharedWith = if (isShared) sharedWith.trim() else null,
                receivedAmount = if (isShared) recAmount else 0.0
            )
            closeSheet { onConfirm(sub) }
            return true
        }
        return false
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (currentHasUnsavedChanges) {
                showUnsavedDialog = true
            } else {
                closeSheet(onDismiss)
            }
        },
        sheetState = sheetState,
        properties = ModalBottomSheetDefaults.properties(
            shouldDismissOnBackPress = false
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (initialSubscription != null) "Editar Assinatura" else "Nova Assinatura",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            // Card seletor que abre o SubscriptionSelectionDialog
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, if (nameError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showSubscriptionPicker = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SubscriptionBrandIcon(
                        subscriptionName = name.ifBlank { "Assinatura" },
                        size = 42.dp
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = name.ifBlank { "Selecionar Assinatura" },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (name.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (name.isBlank()) "Toque para pesquisar no catálogo" else "Toque para alterar serviço",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Alterar serviço",
                        tint = PrimaryAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            if (nameError != null) {
                Text(
                    text = nameError ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            if (showSubscriptionPicker) {
                SubscriptionSelectionDialog(
                    currentSubscriptionId = null,
                    onDismiss = { showSubscriptionPicker = false },
                    onSelectSubscription = { selectedSub ->
                        name = selectedSub.displayName
                        nameError = null
                        showSubscriptionPicker = false
                    }
                )
            }

            OutlinedTextField(
                value = valueStr,
                onValueChange = {
                    valueStr = it.replace(',', '.')
                    if (valueError != null) valueError = null
                },
                label = { Text("Valor mensal total (R$)", style = MaterialTheme.typography.bodySmall) },
                placeholder = { Text("39.90") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = valueError != null,
                supportingText = {
                    valueError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryAccent,
                    focusedLabelColor = PrimaryAccent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = isDropdownExpanded && availableCards.isNotEmpty(),
                onExpandedChange = { if (availableCards.isNotEmpty()) isDropdownExpanded = !isDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = if (availableCards.isEmpty()) "Nenhum cartão cadastrado" else selectedCardName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Cartão associado", style = MaterialTheme.typography.bodySmall) },
                    isError = cardError != null,
                    supportingText = {
                        cardError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    trailingIcon = {
                        if (availableCards.isNotEmpty()) ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryAccent,
                        focusedLabelColor = PrimaryAccent
                    ),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                if (availableCards.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        availableCards.forEach { card ->
                            DropdownMenuItem(
                                text = { Text(card.name, style = MaterialTheme.typography.bodyMedium) },
                                onClick = {
                                    selectedCardId = card.id
                                    selectedCardName = card.name
                                    isDropdownExpanded = false
                                    if (cardError != null) cardError = null
                                }
                            )
                        }
                    }
                }
            }

            // Assinatura Compartilhada Switch e Campos
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Dividir assinatura?",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Abata a quantia paga por outra pessoa do seu gasto",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isShared,
                            onCheckedChange = {
                                isShared = it
                                if (!it) {
                                    sharedWithError = null
                                    receivedAmountError = null
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }

                    if (isShared) {
                        OutlinedTextField(
                            value = sharedWith,
                            onValueChange = {
                                sharedWith = it
                                if (sharedWithError != null) sharedWithError = null
                            },
                            label = { Text("Com quem divide?", style = MaterialTheme.typography.bodySmall) },
                            placeholder = { Text("Ex: Amigo, Irmão, Colega...") },
                            singleLine = true,
                            isError = sharedWithError != null,
                            supportingText = {
                                sharedWithError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryAccent,
                                focusedLabelColor = PrimaryAccent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = receivedAmountStr,
                            onValueChange = {
                                receivedAmountStr = it.replace(',', '.')
                                if (receivedAmountError != null) receivedAmountError = null
                            },
                            label = { Text("Valor que a pessoa paga (R$)", style = MaterialTheme.typography.bodySmall) },
                            placeholder = { Text("Ex: 19.95") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = receivedAmountError != null,
                            supportingText = {
                                receivedAmountError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryAccent,
                                focusedLabelColor = PrimaryAccent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        val totalAmount = valueStr.toDoubleOrNull()
                        if (totalAmount != null && totalAmount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        val half = totalAmount / 2.0
                                        receivedAmountStr = String.format(Locale.US, "%.2f", half)
                                        receivedAmountError = null
                                    }
                                ) {
                                    Text("Dividir meio a meio (50%)", style = MaterialTheme.typography.labelMedium)
                                }

                                val recVal = receivedAmountStr.toDoubleOrNull() ?: 0.0
                                val net = (totalAmount - recVal).coerceAtLeast(0.0)
                                Text(
                                    text = "Seu custo: ${String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", net)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(onClick = attemptDismiss, modifier = Modifier.weight(1f)) {
                    Text("Cancelar", color = TextSecondary)
                }
                Button(
                    onClick = { validateAndSave() },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(2f).height(48.dp)
                ) {
                    Text("Salvar Assinatura", fontWeight = FontWeight.Bold)
                }
            }

            if (initialSubscription != null && onDelete != null) {
                TextButton(
                    onClick = {
                        closeSheet { onDelete() }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("delete_subscription_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Excluir Assinatura", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showUnsavedDialog) {
        UnsavedChangesConfirmationDialog(
            onDismissRequest = { showUnsavedDialog = false },
            onDiscard = {
                showUnsavedDialog = false
                closeSheet(onDismiss)
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
private fun DailyExpenseRowItem(
    expense: DailyExpense,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .testTag("daily_expense_item_${expense.id}"),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            val methodIcon = if (expense.paymentMethod == PaymentMethod.ESPECIE) Icons.Outlined.Payments else Icons.Outlined.AccountBalance
            Icon(
                imageVector = methodIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = expense.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val displayDate = remember(expense.date) {
                    if (expense.date.contains("-")) {
                        try {
                            val dateObj = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(expense.date)
                            SimpleDateFormat("dd/MM/yyyy", Locale.US).format(dateObj!!)
                        } catch (e: Exception) { expense.date }
                    } else {
                        expense.date
                    }
                }
                Text(
                    text = displayDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!expense.observation.isNullOrBlank()) {
                    Text(
                        text = "• ${expense.observation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", expense.value),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp).testTag("edit_daily_expense_${expense.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp).testTag("delete_daily_expense_${expense.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Excluir",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyExpenseBottomSheet(
    initialExpense: DailyExpense?,
    onDismiss: () -> Unit,
    onConfirm: (DailyExpense) -> Unit
) {
    val scope = rememberCoroutineScope()
    val dateDisplayFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.US) }
    val dateStorageFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }

    val todayFormatted = remember {
        dateDisplayFormat.format(Calendar.getInstance().time)
    }

    var name by remember { mutableStateOf(initialExpense?.name ?: "") }
    var valueStr by remember {
        mutableStateOf(initialExpense?.let { String.format(Locale.US, "%.2f", it.value) } ?: "")
    }
    var date by remember {
        val initial = initialExpense?.date
        val formatted = if (initial != null && initial.contains("-")) {
            try {
                val d = dateStorageFormat.parse(initial)
                dateDisplayFormat.format(d!!)
            } catch (e: Exception) { initial }
        } else {
            initial ?: todayFormatted
        }
        mutableStateOf(formatted)
    }
    var paymentMethod by remember { mutableStateOf(initialExpense?.paymentMethod ?: PaymentMethod.CONTA) }
    var observation by remember { mutableStateOf(initialExpense?.observation ?: "") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var valueError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    val hasUnsavedChanges = remember(name, valueStr, date, paymentMethod, observation) {
        if (initialExpense == null) {
            name.isNotBlank() || valueStr.isNotBlank() || observation.isNotBlank() || (date != todayFormatted)
        } else {
            name != initialExpense.name ||
                valueStr != String.format(Locale.US, "%.2f", initialExpense.value) ||
                observation != (initialExpense.observation ?: "") ||
                paymentMethod != initialExpense.paymentMethod
        }
    }

    val currentHasUnsavedChanges by rememberUpdatedState(hasUnsavedChanges)

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue ->
            if (sheetValue == SheetValue.Hidden && currentHasUnsavedChanges) {
                showUnsavedDialog = true
                false
            } else {
                true
            }
        }
    )

    fun closeSheet(onClosed: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            onClosed()
        }
    }

    val attemptDismiss = {
        if (currentHasUnsavedChanges) {
            showUnsavedDialog = true
        } else {
            closeSheet(onDismiss)
        }
    }

    BackHandler(enabled = currentHasUnsavedChanges) {
        showUnsavedDialog = true
    }

    fun validateAndSave(): Boolean {
        var isValid = true

        if (name.trim().isBlank()) {
            nameError = "Informe o nome do gasto."
            isValid = false
        } else {
            nameError = null
        }

        val amount = valueStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            valueError = "Informe um valor válido maior que zero."
            isValid = false
        } else {
            valueError = null
        }

        if (date.trim().isBlank()) {
            dateError = "Informe a data do gasto."
            isValid = false
        } else {
            try {
                val d = dateDisplayFormat.parse(date.trim())
                if (d == null) {
                    dateError = "Data inválida (use DD/MM/AAAA)."
                    isValid = false
                } else {
                    dateError = null
                }
            } catch (e: Exception) {
                dateError = "Data inválida (use DD/MM/AAAA)."
                isValid = false
            }
        }

        if (isValid && amount != null) {
            val finalDate = try {
                val d = dateDisplayFormat.parse(date.trim())
                dateStorageFormat.format(d!!)
            } catch (e: Exception) {
                date.trim()
            }

            val finalExpense = DailyExpense(
                id = initialExpense?.id ?: UUID.randomUUID().toString(),
                name = name.trim(),
                value = amount,
                date = finalDate,
                source = initialExpense?.source ?: DataSource.MANUAL,
                paymentMethod = paymentMethod,
                observation = observation.trim().ifBlank { null }
            )
            closeSheet { onConfirm(finalExpense) }
            return true
        }
        return false
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (currentHasUnsavedChanges) {
                showUnsavedDialog = true
            } else {
                closeSheet(onDismiss)
            }
        },
        sheetState = sheetState,
        properties = ModalBottomSheetDefaults.properties(
            shouldDismissOnBackPress = false
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (initialExpense != null) "Editar Gasto" else "Novo Gasto Diário",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            // Nome
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (nameError != null) nameError = null
                },
                label = { Text("Nome do gasto", style = MaterialTheme.typography.bodySmall) },
                singleLine = true,
                isError = nameError != null,
                supportingText = {
                    nameError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryAccent,
                    focusedLabelColor = PrimaryAccent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_expense_name_input")
            )

            // Valor e Data em Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = valueStr,
                    onValueChange = {
                        valueStr = it.replace(',', '.')
                        if (valueError != null) valueError = null
                    },
                    label = { Text("Valor (R$)", style = MaterialTheme.typography.bodySmall) },
                    placeholder = { Text("45.90") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = valueError != null,
                    supportingText = {
                        valueError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryAccent,
                        focusedLabelColor = PrimaryAccent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("daily_expense_value_input")
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = {
                        date = it
                        if (dateError != null) dateError = null
                    },
                    label = { Text("Data (DD/MM/AAAA)", style = MaterialTheme.typography.bodySmall) },
                    singleLine = true,
                    isError = dateError != null,
                    supportingText = {
                        dateError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryAccent,
                        focusedLabelColor = PrimaryAccent
                    ),
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("daily_expense_date_input")
                )
            }

            // Meio de Pagamento
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Meio de pagamento:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = paymentMethod == PaymentMethod.CONTA,
                        onClick = { paymentMethod = PaymentMethod.CONTA },
                        label = { Text("Conta") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Outlined.AccountBalance, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = paymentMethod == PaymentMethod.ESPECIE,
                        onClick = { paymentMethod = PaymentMethod.ESPECIE },
                        label = { Text("Espécie") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Outlined.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Observação
            OutlinedTextField(
                value = observation,
                onValueChange = { observation = it },
                label = { Text("Observação (opcional)", style = MaterialTheme.typography.bodySmall) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryAccent,
                    focusedLabelColor = PrimaryAccent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_expense_observation_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = attemptDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar", color = TextSecondary)
                }
                Button(
                    onClick = { validateAndSave() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryAccent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(2f)
                        .height(48.dp)
                        .testTag("save_daily_expense_button")
                ) {
                    Text("Salvar Gasto", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showUnsavedDialog) {
        UnsavedChangesConfirmationDialog(
            onDismissRequest = { showUnsavedDialog = false },
            onDiscard = {
                showUnsavedDialog = false
                closeSheet(onDismiss)
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
