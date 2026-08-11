package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CardPaymentItem
import com.example.data.DataSource
import com.example.data.CardItem
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.BankBrandRegistry
import com.example.ui.theme.DividerColor
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

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
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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
            initialColorHex = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { newName, colorHex ->
                if (newName.isNotBlank()) {
                    val updatedList = cards + CardItem(name = newName.trim(), colorHex = colorHex)
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
            initialColorHex = card.colorHex,
            onDismiss = { cardToEdit = null },
            onConfirm = { updatedName, colorHex ->
                if (updatedName.isNotBlank()) {
                    val updatedList = cards.map {
                        if (it.id == card.id) it.copy(name = updatedName.trim(), colorHex = colorHex) else it
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
    val bankBrand = remember(card.name, card.colorHex) { 
        BankBrandRegistry.getBrandForName(card.name, card.colorHex) 
    }

    val historyPayments = remember(cardPayments, card.id, card.name) {
        cardPayments
            .filter { it.cardId == card.id || (it.cardId.isBlank() && card.name.isNotBlank()) }
            .sortedWith(compareByDescending<CardPaymentItem> { it.year }.thenByDescending { it.month })
            .take(6)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_item_${card.id}"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Visual de Cartão Físico
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f) // Reduz a largura para 85% da tela
                .align(Alignment.CenterHorizontally)
                .aspectRatio(1.586f) // Proporção padrão
                .clip(RoundedCornerShape(16.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = bankBrand.mainColor
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                bankBrand.mainColor,
                                bankBrand.mainColor.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .padding(20.dp) // Reduzido o padding interno
            ) {
                // Nome do Banco/Cartão (Topo)
                Text(
                    text = card.name.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = bankBrand.onColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.TopStart)
                )

                // Simulação do Chip (Centro-Esquerda)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(32.dp, 24.dp) // Reduzido o tamanho do chip
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFD4AF37).copy(alpha = 0.8f))
                )

                // Ações do Cartão (Topo-Direita)
                Row(
                    modifier = Modifier.align(Alignment.TopEnd),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(28.dp).testTag("edit_card_${card.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Editar",
                            tint = bankBrand.onColor.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp).testTag("delete_card_${card.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Excluir",
                            tint = bankBrand.onColor.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Indicador de Status (Base-Direita)
                if (isPaidThisMonth) {
                    val currentMonthName = remember {
                        val cal = Calendar.getInstance()
                        SimpleDateFormat("MMMM", Locale("pt", "BR")).format(cal.time).replaceFirstChar { it.uppercase() }
                    }
                    Row(
                        modifier = Modifier.align(Alignment.BottomEnd),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = bankBrand.onColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Fatura de $currentMonthName Paga",
                            style = MaterialTheme.typography.labelSmall,
                            color = bankBrand.onColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Botão de Ação abaixo do Cartão
        if (!isPaidThisMonth) {
            OutlinedButton(
                onClick = onPayInvoice,
                border = BorderStroke(1.dp, PrimaryAccent),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pay_invoice_button_${card.id}")
            ) {
                Text("Registrar pagamento da fatura", fontWeight = FontWeight.Bold)
            }
        }

        // Histórico de Faturas para este cartão
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
    initialColorHex: String?,
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedColorHex by remember { mutableStateOf(initialColorHex) }

    val presetColors = listOf(
        "#8A05BE", // Nubank
        "#FF7800", // Itaú
        "#FF7A00", // Inter
        "#EC0000", // Santander
        "#B20C15", // Bradesco
        "#0038A8", // BB
        "#005CA9", // Caixa
        "#00A335", // Mercado Pago
        "#111111", // Preto
        "#6B7280"  // Cinza
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Cor personalizada (opcional):",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(presetColors) { hex ->
                            val color = Color(android.graphics.Color.parseColor(hex))
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (selectedColorHex == hex) 3.dp else 0.dp,
                                        color = if (selectedColorHex == hex) PrimaryAccent else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        selectedColorHex = if (selectedColorHex == hex) null else hex
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedColorHex == hex) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, selectedColorHex) },
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
