package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.StatusFeedbackBanner
import com.example.ui.theme.DividerColor
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.data.DataSource
import java.util.UUID

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import com.example.data.CardPaymentItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class CardItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val source: DataSource = DataSource.MANUAL,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Composable
fun CardsScreen(
    cards: List<CardItem>,
    cardPayments: List<CardPaymentItem> = emptyList(),
    onUpdateCards: (List<CardItem>) -> Unit,
    onRegisterInvoicePayment: (CardItem) -> Unit = {},
    statusMessage: String? = null,
    isErrorStatus: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var cardToEdit by remember { mutableStateOf<CardItem?>(null) }
    var cardToDelete by remember { mutableStateOf<CardItem?>(null) }
    var cardToPayInvoice by remember { mutableStateOf<CardItem?>(null) }

    val currentCal = remember { Calendar.getInstance() }
    val currentMonth = currentCal.get(Calendar.MONTH) + 1
    val currentYear = currentCal.get(Calendar.YEAR)

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("cards_screen")
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Meus Cartões",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )

            if (cards.isEmpty()) {
                EmptyStateCard(
                    icon = Icons.Outlined.CreditCard,
                    title = "Nenhum cartão cadastrado",
                    description = "Clique no botão abaixo para cadastrar um novo cartão."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    itemsIndexed(cards, key = { _, card -> card.id }) { index, card ->
                        val isPaidThisMonth = cardPayments.any {
                            it.cardId == card.id && it.month == currentMonth && it.year == currentYear && it.paid
                        }
                        CardRowItem(
                            card = card,
                            cardPayments = cardPayments,
                            isPaidThisMonth = isPaidThisMonth,
                            onEdit = { cardToEdit = card },
                            onDelete = { cardToDelete = card },
                            onPayInvoice = { cardToPayInvoice = card }
                        )
                        if (index < cards.size - 1) {
                            HorizontalDivider(color = DividerColor)
                        }
                    }
                }
            }
        }

        // FAB discreto para adicionar cartão
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = PrimaryAccent,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_card_button")
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Adicionar Cartão"
            )
        }
    }

    // Dialog Confirmar Pagamento da Fatura
    cardToPayInvoice?.let { card ->
        AlertDialog(
            onDismissRequest = { cardToPayInvoice = null },
            title = { Text("Registrar Pagamento da Fatura", style = MaterialTheme.typography.titleMedium, color = TextPrimary) },
            text = {
                Text(
                    "Confirmar o pagamento da fatura do cartão \"${card.name}\" referente ao mês atual ($currentMonth/$currentYear)?\n\nIsso avançará as parcelas das compras associadas.",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRegisterInvoicePayment(card)
                        cardToPayInvoice = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                    modifier = Modifier.testTag("confirm_pay_invoice_button")
                ) {
                    Text("Confirmar Pagamento")
                }
            },
            dismissButton = {
                TextButton(onClick = { cardToPayInvoice = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Dialog Adicionar
    if (showAddDialog) {
        CardFormDialog(
            title = "Adicionar Cartão",
            initialName = "",
            onDismiss = { showAddDialog = false },
            onConfirm = { newName ->
                if (newName.isNotBlank()) {
                    val updatedList = cards + CardItem(name = newName.trim())
                    onUpdateCards(updatedList)
                }
                showAddDialog = false
            }
        )
    }

    // Dialog Editar
    cardToEdit?.let { card ->
        CardFormDialog(
            title = "Editar Cartão",
            initialName = card.name,
            onDismiss = { cardToEdit = null },
            onConfirm = { updatedName ->
                if (updatedName.isNotBlank()) {
                    val updatedList = cards.map {
                        if (it.id == card.id) it.copy(name = updatedName.trim()) else it
                    }
                    onUpdateCards(updatedList)
                }
                cardToEdit = null
            }
        )
    }

    // Dialog Confirmar Exclusão
    cardToDelete?.let { card ->
        AlertDialog(
            onDismissRequest = { cardToDelete = null },
            title = { Text("Excluir Cartão", style = MaterialTheme.typography.titleMedium) },
            text = { Text("Deseja realmente excluir o cartão \"${card.name}\"?", style = MaterialTheme.typography.bodyLarge) },
            confirmButton = {
                Button(
                    onClick = {
                        val updatedList = cards.filter { it.id != card.id }
                        onUpdateCards(updatedList)
                        cardToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.testTag("confirm_delete_card_button")
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { cardToDelete = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun CardRowItem(
    card: CardItem,
    cardPayments: List<CardPaymentItem>,
    isPaidThisMonth: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPayInvoice: () -> Unit
) {
    var showInvoiceHistory by remember { mutableStateOf(false) }

    val historyPayments = remember(cardPayments, card.id, card.name) {
        cardPayments
            .filter { it.cardId == card.id || (it.cardId.isBlank() && card.name.isNotBlank()) }
            .sortedWith(compareByDescending<CardPaymentItem> { it.year }.thenByDescending { it.month })
            .take(6)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .testTag("card_item_${card.id}"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CreditCard,
                    contentDescription = null,
                    tint = TextSecondary
                )
                Text(
                    text = card.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            Row {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.testTag("edit_card_${card.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Editar cartão",
                        tint = TextSecondary
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_card_${card.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Excluir cartão",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Botão de Pagamento da Fatura
        if (isPaidThisMonth) {
            Button(
                onClick = {},
                enabled = false,
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = DividerColor,
                    disabledContentColor = TextSecondary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pay_invoice_button_${card.id}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Fatura paga este mês")
            }
        } else {
            OutlinedButton(
                onClick = onPayInvoice,
                border = BorderStroke(1.dp, PrimaryAccent),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pay_invoice_button_${card.id}")
            ) {
                Text("Registrar pagamento da fatura")
            }
        }

        // Histórico de Faturas para este cartão (Sprint 13)
        if (historyPayments.isNotEmpty()) {
            TextButton(
                onClick = { showInvoiceHistory = !showInvoiceHistory },
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Icon(
                    imageVector = if (showInvoiceHistory) Icons.Outlined.KeyboardArrowDown else Icons.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                    tint = PrimaryAccent,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    text = "Últimos pagamentos de faturas (${historyPayments.size})",
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryAccent,
                    fontWeight = FontWeight.Medium
                )
            }

            if (showInvoiceHistory) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, bottom = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    historyPayments.forEach { payment ->
                        val monthCal = Calendar.getInstance()
                        monthCal.set(Calendar.MONTH, payment.month - 1)
                        val monthName = SimpleDateFormat("MMMM", Locale("pt", "BR")).format(monthCal.time)
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pt", "BR")) else it.toString() }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = PrimaryAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "$monthName/${payment.year}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CardFormDialog(
    title: String,
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do cartão", style = MaterialTheme.typography.bodySmall) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryAccent,
                        focusedLabelColor = PrimaryAccent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_name_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryAccent
                ),
                modifier = Modifier.testTag("save_card_button")
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}
