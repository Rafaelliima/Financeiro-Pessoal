package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
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
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.SheetState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
    onUpdateSubscriptions: (List<SubscriptionItem>) -> Unit = {},
    onRegisterInvoicePayment: (CardItem) -> Unit = {},
    statusMessage: String? = null,
    isErrorStatus: Boolean = false,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableStateOf(0) } // 0: Por Cartão, 1: Dia a Dia, 2: Assinatura
    var purchasesFilter by remember { mutableStateOf("Todas") } // "Todas", "Ativas", "Quitadas"
    var isHistoryExpanded by remember { mutableStateOf(false) } // Recolhido por padrão
    var cardForPaymentsHistory by remember { mutableStateOf<CardItem?>(null) }
    var expandedCardNames by remember { mutableStateOf<Set<String>>(emptySet()) }

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
    val purchaseSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val expenseSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val subscriptionSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val activePurchases = remember(purchases) {
        purchases.filter { !it.isCurrentlyQuitada() }
    }
    val quitadasPurchases = remember(purchases) {
        purchases.filter { it.isCurrentlyQuitada() }
    }

    val groupedActivePurchases = remember(activePurchases) {
        activePurchases.groupBy { if (it.cardName.isNotBlank()) it.cardName else "Sem cartão" }
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
            Text(
                text = "Despesas e Compras",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

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

                if (purchases.isEmpty()) {
                    EmptyStateCard(
                        icon = Icons.Outlined.ShoppingBag,
                        title = "Nenhuma compra registrada",
                        description = "Clique no botão abaixo para cadastrar uma nova compra de cartão."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(),
                        contentPadding = PaddingValues(bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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
                                        val cardMonthlyTotal = cardPurchases.sumOf { p -> p.calculateInstallments().installmentValue }

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
                                                        Box(
                                                            modifier = Modifier
                                                                .size(44.dp)
                                                                .clip(RoundedCornerShape(12.dp))
                                                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Outlined.CreditCard,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.primary
                                                            )
                                                        }
                                                        Column {
                                                            Text(
                                                                text = cardName,
                                                                style = MaterialTheme.typography.titleMedium,
                                                                color = MaterialTheme.colorScheme.onSurface,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            Text(
                                                                text = "${cardPurchases.size} compra${if (cardPurchases.size > 1) "s" else ""} • R$ ${String.format(Locale("pt", "BR"), "%.2f", cardMonthlyTotal)} /mês",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                                        val currentCal = Calendar.getInstance()
                                                        val currentMonth = currentCal.get(Calendar.MONTH) + 1
                                                        val currentYear = currentCal.get(Calendar.YEAR)
                                                        val isCardPaidThisMonth = cardPayments.any {
                                                            it.cardId == linkedCard?.id && it.month == currentMonth && it.year == currentYear && it.paid
                                                        }

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
                                                                onClick = { onRegisterInvoicePayment(linkedCard) },
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
                                                                val monthName = remember(currentMonth) {
                                                                    val cal = Calendar.getInstance()
                                                                    cal.set(Calendar.MONTH, currentMonth - 1)
                                                                    SimpleDateFormat("MMMM", Locale("pt", "BR")).format(cal.time)
                                                                        .replaceFirstChar { it.uppercase() }
                                                                }
                                                                Text(
                                                                    text = if (isCardPaidThisMonth) "Fatura de $monthName Paga" else "Pagar Fatura de $monthName",
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
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        itemsIndexed(subscriptions, key = { _, sub -> sub.id }) { index, sub ->
                            SubscriptionRowItem(
                                subscription = sub,
                                onEdit = { subscriptionToEdit = sub },
                                onDelete = { subscriptionToDelete = sub }
                            )
                            if (index < subscriptions.size - 1) {
                                HorizontalDivider(color = DividerColor)
                            }
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

    // Add Purchase Sheet
    if (showAddPurchaseDialog || purchaseToEdit != null) {
        ModalBottomSheet(
            onDismissRequest = { 
                showAddPurchaseDialog = false
                purchaseToEdit = null
            },
            sheetState = purchaseSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            PurchaseFormContent(
                title = if (purchaseToEdit != null) "Editar Compra" else "Nova Compra",
                availableCards = cards,
                initialPurchase = purchaseToEdit,
                onDismiss = {
                    scope.launch { purchaseSheetState.hide() }.invokeOnCompletion {
                        showAddPurchaseDialog = false
                        purchaseToEdit = null
                    }
                },
                onConfirm = { purchase ->
                    if (purchaseToEdit != null) {
                        onUpdatePurchases(purchases.map { if (it.id == purchase.id) purchase else it })
                    } else {
                        onUpdatePurchases(purchases + purchase)
                    }
                    scope.launch { purchaseSheetState.hide() }.invokeOnCompletion {
                        showAddPurchaseDialog = false
                        purchaseToEdit = null
                    }
                }
            )
        }
    }

    // Add Daily Expense Sheet
    if (showAddDailyExpenseDialog || dailyExpenseToEdit != null) {
        ModalBottomSheet(
            onDismissRequest = { 
                showAddDailyExpenseDialog = false
                dailyExpenseToEdit = null
            },
            sheetState = expenseSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            DailyExpenseFormContent(
                title = if (dailyExpenseToEdit != null) "Editar Gasto" else "Novo Gasto Diário",
                initialExpense = dailyExpenseToEdit,
                onDismiss = {
                    scope.launch { expenseSheetState.hide() }.invokeOnCompletion {
                        showAddDailyExpenseDialog = false
                        dailyExpenseToEdit = null
                    }
                },
                onConfirm = { expense ->
                    if (dailyExpenseToEdit != null) {
                        onUpdateDailyExpenses(dailyExpenses.map { if (it.id == expense.id) expense else it })
                    } else {
                        onUpdateDailyExpenses(dailyExpenses + expense)
                    }
                    scope.launch { expenseSheetState.hide() }.invokeOnCompletion {
                        showAddDailyExpenseDialog = false
                        dailyExpenseToEdit = null
                    }
                }
            )
        }
    }

    // Add Subscription Sheet
    if (showAddSubscriptionDialog || subscriptionToEdit != null) {
        ModalBottomSheet(
            onDismissRequest = { 
                showAddSubscriptionDialog = false
                subscriptionToEdit = null
            },
            sheetState = subscriptionSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            SubscriptionFormContent(
                title = if (subscriptionToEdit != null) "Editar Assinatura" else "Nova Assinatura",
                availableCards = cards,
                initialSubscription = subscriptionToEdit,
                onDismiss = {
                    scope.launch { subscriptionSheetState.hide() }.invokeOnCompletion {
                        showAddSubscriptionDialog = false
                        subscriptionToEdit = null
                    }
                },
                onConfirm = { sub ->
                    if (subscriptionToEdit != null) {
                        onUpdateSubscriptions(subscriptions.map { if (it.id == sub.id) sub else it })
                    } else {
                        onUpdateSubscriptions(subscriptions + sub)
                    }
                    scope.launch { subscriptionSheetState.hide() }.invokeOnCompletion {
                        showAddSubscriptionDialog = false
                        subscriptionToEdit = null
                    }
                }
            )
        }
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
                        val updatedList = dailyExpenses.filter { it.id != expense.id }
                        onUpdateDailyExpenses(updatedList)
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
private fun PurchaseRowItem(
    purchase: PurchaseItem,
    isCardPaidThisMonth: Boolean = false,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val calc = remember(purchase) { purchase.calculateInstallments() }
    val isQuitado = calc.status == "Quitado" || purchase.isQuitada
    val referenceMonth = remember(purchase) { purchase.getNextInstallmentReference() }

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
                    Text(
                        text = "${calc.currentInstallment}/${calc.totalInstallments}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = if (isQuitado) "Quitada" else if (purchase.isInstallment) "Vence: $referenceMonth" else "À vista",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCardPaidThisMonth && purchase.isInstallment) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", if (purchase.isInstallment) calc.installmentValue else purchase.totalAmount),
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
private fun PurchaseFormContent(
    title: String,
    availableCards: List<CardItem>,
    initialPurchase: PurchaseItem?,
    onDismiss: () -> Unit,
    onConfirm: (PurchaseItem) -> Unit
) {
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
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .imePadding()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )

        // Nome
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome da compra", style = MaterialTheme.typography.bodySmall) },
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
            onValueChange = { totalAmountStr = it.replace(',', '.') },
            label = { Text("Valor total (R$)", style = MaterialTheme.typography.bodySmall) },
            placeholder = { Text("Ex: 150.00") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                    onValueChange = { totalInstallmentsStr = it },
                    label = { Text("Parcelas", style = MaterialTheme.typography.bodySmall) },
                    placeholder = { Text("Ex: 10") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    onValueChange = { startMonthYearStr = it },
                    label = { Text("Início (MM/AAAA)", style = MaterialTheme.typography.bodySmall) },
                    placeholder = { Text("03/2026") },
                    singleLine = true,
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

        errorMessage?.let { err ->
            Text(
                text = err,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar", color = TextSecondary)
            }
            Button(
                onClick = {
                    val amount = totalAmountStr.toDoubleOrNull()
                    if (name.isBlank()) {
                        errorMessage = "Informe o nome da compra."
                        return@Button
                    }
                    if (amount == null || amount <= 0) {
                        errorMessage = "Informe um valor total válido maior que zero."
                        return@Button
                    }

                    var totalInst = 1
                    var month = currentM
                    var year = currentY

                    if (isInstallment) {
                        val parsedInst = totalInstallmentsStr.toIntOrNull()
                        if (parsedInst == null || parsedInst < 1) {
                            errorMessage = "Informe uma quantidade de parcelas válida (mínimo 1)."
                            return@Button
                        }
                        totalInst = parsedInst

                        val dateParts = startMonthYearStr.trim().split('/')
                        if (dateParts.size != 2) {
                            errorMessage = "Informe a primeira parcela no formato MM/AAAA (ex: 03/2026)."
                            return@Button
                        }
                        val parsedM = dateParts[0].toIntOrNull()
                        val parsedY = dateParts[1].toIntOrNull()
                        if (parsedM == null || parsedM !in 1..12 || parsedY == null || parsedY < 2000) {
                            errorMessage = "Mês (1-12) ou Ano inválido no formato MM/AAAA."
                            return@Button
                        }
                        month = parsedM
                        year = parsedY
                    }

                    val paidCount = initialPurchase?.paidInstallmentsCount ?: 0
                    val isNowQuitada = (initialPurchase?.isQuitada == true) || (isInstallment && paidCount >= totalInst)
                    val nowCompletedAt = if (isNowQuitada && initialPurchase?.completedAt == null) {
                        val cal = Calendar.getInstance()
                        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
                    } else if (!isNowQuitada) {
                        null
                    } else {
                        initialPurchase?.completedAt
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
                    onConfirm(finalPurchase)
                },
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

@Composable
private fun QuitadaPurchaseRowItem(
    purchase: PurchaseItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .testTag("quitada_purchase_item_${purchase.id}"),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = purchase.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
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
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", purchase.totalAmount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
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
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CardPaymentsHistoryDialog(
    cardName: String,
    cardId: String,
    cardPayments: List<CardPaymentItem>,
    onDismiss: () -> Unit
) {
    val filteredPayments = remember(cardPayments, cardId, cardName) {
        cardPayments
            .filter { it.cardId == cardId || (it.cardId.isBlank() && cardName.isNotBlank()) }
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
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .testTag("subscription_item_${subscription.id}"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Repeat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = subscription.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = String.format(Locale("pt", "BR"), "R$ %.2f /mês", subscription.monthlyValue),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (subscription.cardName.isNotBlank()) {
                        Text(
                            text = "• ${subscription.cardName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp).testTag("edit_subscription_${subscription.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp).testTag("delete_subscription_${subscription.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Excluir",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubscriptionFormContent(
    title: String,
    availableCards: List<CardItem>,
    initialSubscription: SubscriptionItem?,
    onDismiss: () -> Unit,
    onConfirm: (SubscriptionItem) -> Unit
) {
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

    var isDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .imePadding()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome da assinatura", style = MaterialTheme.typography.bodySmall) },
            placeholder = { Text("Netflix, Spotify...") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryAccent,
                focusedLabelColor = PrimaryAccent
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = valueStr,
            onValueChange = { valueStr = it.replace(',', '.') },
            label = { Text("Valor mensal (R$)", style = MaterialTheme.typography.bodySmall) },
            placeholder = { Text("39.90") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                            }
                        )
                    }
                }
            }
        }

        errorMessage?.let { err ->
            Text(
                text = err,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                Text("Cancelar", color = TextSecondary)
            }
            Button(
                onClick = {
                    val amount = valueStr.toDoubleOrNull()
                    if (name.isBlank()) {
                        errorMessage = "Informe o nome da assinatura."
                        return@Button
                    }
                    if (amount == null || amount <= 0) {
                        errorMessage = "Informe um valor mensal válido."
                        return@Button
                    }
                    val sub = SubscriptionItem(
                        id = initialSubscription?.id ?: UUID.randomUUID().toString(),
                        name = name.trim(),
                        monthlyValue = amount,
                        cardId = selectedCardId,
                        cardName = selectedCardName
                    )
                    onConfirm(sub)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(2f).height(48.dp)
            ) {
                Text("Salvar Assinatura", fontWeight = FontWeight.Bold)
            }
        }
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
                Text(
                    text = expense.date,
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

@Composable
private fun DailyExpenseFormContent(
    title: String,
    initialExpense: DailyExpense?,
    onDismiss: () -> Unit,
    onConfirm: (DailyExpense) -> Unit
) {
    val todayFormatted = remember {
        val cal = Calendar.getInstance()
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }

    var name by remember { mutableStateOf(initialExpense?.name ?: "") }
    var valueStr by remember {
        mutableStateOf(initialExpense?.let { String.format(Locale.US, "%.2f", it.value) } ?: "")
    }
    var date by remember { mutableStateOf(initialExpense?.date ?: todayFormatted) }
    var paymentMethod by remember { mutableStateOf(initialExpense?.paymentMethod ?: PaymentMethod.CONTA) }
    var observation by remember { mutableStateOf(initialExpense?.observation ?: "") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .imePadding()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )

        // Nome
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome do gasto", style = MaterialTheme.typography.bodySmall) },
            singleLine = true,
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
                onValueChange = { valueStr = it.replace(',', '.') },
                label = { Text("Valor (R$)", style = MaterialTheme.typography.bodySmall) },
                placeholder = { Text("45.90") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                onValueChange = { date = it },
                label = { Text("Data (AAAA-MM-DD)", style = MaterialTheme.typography.bodySmall) },
                singleLine = true,
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

        errorMessage?.let { err ->
            Text(
                text = err,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar", color = TextSecondary)
            }
            Button(
                onClick = {
                    val amount = valueStr.toDoubleOrNull()
                    if (name.isBlank()) {
                        errorMessage = "Informe o nome do gasto."
                        return@Button
                    }
                    if (amount == null || amount <= 0) {
                        errorMessage = "Informe um valor válido maior que zero."
                        return@Button
                    }
                    if (date.isBlank()) {
                        errorMessage = "Informe uma data válida."
                        return@Button
                    }

                    val finalExpense = DailyExpense(
                        id = initialExpense?.id ?: UUID.randomUUID().toString(),
                        name = name.trim(),
                        value = amount,
                        date = date.trim(),
                        source = initialExpense?.source ?: DataSource.MANUAL,
                        paymentMethod = paymentMethod,
                        observation = observation.trim().ifBlank { null }
                    )
                    onConfirm(finalExpense)
                },
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
