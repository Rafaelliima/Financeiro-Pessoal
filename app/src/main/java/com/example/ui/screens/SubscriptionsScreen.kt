package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.StatusFeedbackBanner
import com.example.ui.theme.DividerColor
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.data.DataSource
import java.util.Locale
import java.util.UUID

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults

data class SubscriptionItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val monthlyValue: Double,
    val cardId: String = "",
    val cardName: String = "",
    val source: DataSource = DataSource.MANUAL,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Composable
fun SubscriptionsScreen(
    subscriptions: List<SubscriptionItem>,
    cards: List<CardItem> = emptyList(),
    onUpdateSubscriptions: (List<SubscriptionItem>) -> Unit,
    statusMessage: String? = null,
    isErrorStatus: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var subscriptionToEdit by remember { mutableStateOf<SubscriptionItem?>(null) }
    var subscriptionToDelete by remember { mutableStateOf<SubscriptionItem?>(null) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("subscriptions_screen")
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Minhas Assinaturas",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )

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

        // FAB discreto
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = PrimaryAccent,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_subscription_button")
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Nova Assinatura"
            )
        }
    }

    // Add Subscription Dialog
    if (showAddDialog) {
        SubscriptionFormDialog(
            title = "Nova Assinatura",
            availableCards = cards,
            initialSubscription = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { newSub ->
                onUpdateSubscriptions(subscriptions + newSub)
                showAddDialog = false
            }
        )
    }

    // Edit Subscription Dialog
    subscriptionToEdit?.let { sub ->
        SubscriptionFormDialog(
            title = "Editar Assinatura",
            availableCards = cards,
            initialSubscription = sub,
            onDismiss = { subscriptionToEdit = null },
            onConfirm = { updatedSub ->
                val updatedList = subscriptions.map {
                    if (it.id == sub.id) updatedSub else it
                }
                onUpdateSubscriptions(updatedList)
                subscriptionToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
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
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Outlined.Repeat,
                contentDescription = null,
                tint = TextSecondary
            )
            Column {
                Text(
                    text = subscription.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = String.format(Locale("pt", "BR"), "R$ %.2f /mês", subscription.monthlyValue),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    if (subscription.cardName.isNotBlank()) {
                        Text(
                            text = "• ${subscription.cardName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        Row {
            IconButton(
                onClick = onEdit,
                modifier = Modifier.testTag("edit_subscription_${subscription.id}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Editar assinatura",
                    tint = TextSecondary
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_subscription_${subscription.id}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Excluir assinatura",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubscriptionFormDialog(
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (availableCards.isEmpty()) {
                    Text(
                        text = "É necessário cadastrar um cartão antes de criar uma assinatura.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da assinatura", style = MaterialTheme.typography.bodySmall) },
                    placeholder = { Text("Ex: Netflix, Spotify...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryAccent,
                        focusedLabelColor = PrimaryAccent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subscription_name_input")
                )

                OutlinedTextField(
                    value = valueStr,
                    onValueChange = { valueStr = it.replace(',', '.') },
                    label = { Text("Valor mensal (R$)", style = MaterialTheme.typography.bodySmall) },
                    placeholder = { Text("Ex: 39.90") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryAccent,
                        focusedLabelColor = PrimaryAccent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subscription_value_input")
                )

                // Cartão associado
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
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("subscription_card_dropdown")
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (availableCards.isEmpty()) {
                        errorMessage = "É necessário cadastrar um cartão antes de criar uma assinatura."
                        return@Button
                    }
                    val amount = valueStr.toDoubleOrNull()
                    if (name.isBlank()) {
                        errorMessage = "Informe o nome da assinatura."
                        return@Button
                    }
                    if (amount == null || amount <= 0) {
                        errorMessage = "Informe um valor mensal válido maior que zero."
                        return@Button
                    }
                    if (selectedCardId.isBlank()) {
                        errorMessage = "Selecione um cartão para a assinatura."
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryAccent
                ),
                modifier = Modifier.testTag("save_subscription_button")
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
