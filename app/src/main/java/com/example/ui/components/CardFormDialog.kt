package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.bank.BankRegistry
import com.example.data.bank.StandardBank
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Diálogo unificado para Adicionar ou Editar Cartão.
 * Conta com seleção padronizada de banco, validação de campos obrigatórios
 * e proteção contra perda acidental de dados preenchidos.
 */
@Composable
fun CardFormDialog(
    title: String,
    initialName: String = "",
    initialColorHex: String? = null,
    initialBankId: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, colorHex: String?, bankId: String?) -> Unit
) {
    // Banco selecionado inicialmente (ou inferido pelo nome)
    val initialBank = remember {
        BankRegistry.getBankForCard(initialName, initialBankId, initialColorHex)
    }

    val effectiveInitialName = remember { initialName.ifBlank { initialBank.displayName } }
    var selectedBank by remember { mutableStateOf(initialBank) }
    var name by remember { mutableStateOf(effectiveInitialName) }
    var selectedColorHex by remember { mutableStateOf(initialColorHex ?: initialBank.colorHex) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var showBankPicker by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    // Dirty checking para proteção contra descarte
    val hasUnsavedChanges = remember(name, selectedColorHex, selectedBank) {
        name != effectiveInitialName ||
            selectedColorHex != (initialColorHex ?: initialBank.colorHex) ||
            selectedBank.id != initialBank.id
    }

    val attemptDismiss = {
        if (hasUnsavedChanges) {
            showUnsavedDialog = true
        } else {
            onDismiss()
        }
    }

    BackHandler(enabled = hasUnsavedChanges) {
        showUnsavedDialog = true
    }

    val presetColors = listOf(
        "#8A05BE", // Nubank
        "#EC7000", // Itaú
        "#0038A8", // BB
        "#CC092F", // Bradesco
        "#005CA9", // Caixa
        "#EC0000", // Santander
        "#FF7A00", // Inter
        "#1B1B1B", // C6
        "#00A335", // Mercado Pago
        "#11C76F", // PicPay
        "#2563EB", // Azul Geral
        "#6B7280"  // Cinza
    )

    AlertDialog(
        onDismissRequest = attemptDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .imePadding()
            ) {
                // Seletor de Banco Padronizado
                Text(
                    text = "Instituição Bancária:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                OutlinedCard(
                    onClick = { showBankPicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("select_bank_card_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val bankColor = try {
                            Color(android.graphics.Color.parseColor(selectedBank.colorHex))
                        } catch (_: Exception) {
                            PrimaryAccent
                        }

                        val currentBank = selectedBank
                        val bankLogoRes = currentBank.logoResId

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (bankLogoRes != null) Color.Transparent else bankColor),
                            contentAlignment = Alignment.Center
                        ) {
                            if (bankLogoRes != null) {
                                Image(
                                    painter = painterResource(id = bankLogoRes),
                                    contentDescription = currentBank.displayName,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            } else {
                                val initials = currentBank.displayName.split(" ")
                                    .mapNotNull { it.firstOrNull()?.toString() }
                                    .take(2)
                                    .joinToString("")
                                Text(
                                    text = initials.ifBlank { "B" },
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedBank.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (selectedBank.isCustom) "Instituição personalizada" else selectedBank.officialName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }

                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Alterar banco",
                            tint = PrimaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Nome ou Apelido do Cartão
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError != null) nameError = null
                    },
                    label = { Text("Nome ou apelido do cartão", style = MaterialTheme.typography.bodySmall) },
                    placeholder = { Text("Ex: Nubank Principal") },
                    isError = nameError != null,
                    supportingText = {
                        nameError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryAccent,
                        focusedLabelColor = PrimaryAccent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_name_input")
                )

                // Cor do cartão
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Cor do cartão:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(presetColors) { hex ->
                            val color = try {
                                Color(android.graphics.Color.parseColor(hex))
                            } catch (_: Exception) {
                                Color.Gray
                            }
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (selectedColorHex.equals(hex, ignoreCase = true)) 3.dp else 0.dp,
                                        color = if (selectedColorHex.equals(hex, ignoreCase = true)) PrimaryAccent else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColorHex = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedColorHex.equals(hex, ignoreCase = true)) {
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
                onClick = {
                    val trimmed = name.trim()
                    if (trimmed.isBlank()) {
                        nameError = "Informe o nome do cartão para continuar."
                        return@Button
                    }
                    onConfirm(trimmed, selectedColorHex, selectedBank.id)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                modifier = Modifier.testTag("save_card_button")
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = attemptDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )

    if (showBankPicker) {
        BankSelectionDialog(
            currentBankId = selectedBank.id,
            onDismiss = { showBankPicker = false },
            onSelectBank = { bank ->
                selectedBank = bank
                // Se o nome atual do cartão era igual ao banco anterior ou vazio, atualiza automaticamente
                if (name.isBlank() || name == initialBank.displayName) {
                    name = bank.displayName
                }
                selectedColorHex = bank.colorHex
                showBankPicker = false
            }
        )
    }

    if (showUnsavedDialog) {
        UnsavedChangesConfirmationDialog(
            onDismissRequest = { showUnsavedDialog = false },
            onDiscard = {
                showUnsavedDialog = false
                onDismiss()
            },
            onSaveAndExit = {
                val trimmed = name.trim()
                if (trimmed.isBlank()) {
                    nameError = "Informe o nome do cartão para continuar."
                    showUnsavedDialog = false
                } else {
                    showUnsavedDialog = false
                    onConfirm(trimmed, selectedColorHex, selectedBank.id)
                }
            }
        )
    }
}
