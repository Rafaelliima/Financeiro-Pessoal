package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.bank.BankRegistry
import com.example.data.bank.StandardBank
import com.example.ui.theme.PrimaryAccent

/**
 * Diálogo para pesquisa e seleção de banco padronizado ou cadastro de instituição manual.
 */
@Composable
fun BankSelectionDialog(
    currentBankId: String? = null,
    onDismiss: () -> Unit,
    onSelectBank: (StandardBank) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var isManualEntry by remember { mutableStateOf(false) }

    // Campos para adição manual
    var manualName by remember { mutableStateOf("") }
    var manualNameError by remember { mutableStateOf<String?>(null) }
    var selectedManualColorHex by remember { mutableStateOf("#2563EB") }

    val presetColors = listOf(
        "#8A05BE", // Roxo Nubank
        "#EC7000", // Laranja Itaú
        "#0038A8", // Azul BB
        "#CC092F", // Vermelho Bradesco
        "#005CA9", // Azul Caixa
        "#EC0000", // Vermelho Santander
        "#FF7A00", // Laranja Inter
        "#1B1B1B", // Preto C6
        "#00A335", // Verde Mercado Pago
        "#11C76F", // Verde PicPay
        "#2563EB", // Azul Primário
        "#6B7280"  // Cinza Neutro
    )

    val searchResults = remember(searchQuery) {
        BankRegistry.searchBanks(searchQuery)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isManualEntry) "Adicionar Banco Manual" else "Selecionar Banco",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Fechar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!isManualEntry) {
                    // Campo de pesquisa com ícone e botão limpar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Pesquisar banco por nome...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Limpar busca"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryAccent,
                            focusedLabelColor = PrimaryAccent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bank_search_input")
                    )

                    // Lista de bancos encontrados
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(searchResults, key = { it.id }) { bank ->
                            val isSelected = bank.id == currentBankId
                            val bankColor = try {
                                Color(android.graphics.Color.parseColor(bank.colorHex))
                            } catch (_: Exception) {
                                PrimaryAccent
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onSelectBank(bank) }
                                    .padding(vertical = 10.dp, horizontal = 8.dp)
                                    .testTag("bank_item_${bank.id}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Badge visual com logotipo oficial ou iniciais
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (bank.logoResId != null) Color.Transparent else bankColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (bank.logoResId != null) {
                                        Image(
                                            painter = painterResource(id = bank.logoResId),
                                            contentDescription = bank.displayName,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(10.dp))
                                        )
                                    } else {
                                        val initials = bank.displayName.split(" ")
                                            .mapNotNull { it.firstOrNull()?.toString() }
                                            .take(2)
                                            .joinToString("")
                                        Text(
                                            text = initials.ifBlank { "B" },
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = bank.displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = bank.officialName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = "Selecionado",
                                        tint = PrimaryAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        }

                        if (searchResults.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Outlined.AccountBalance,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Nenhum banco encontrado.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Opção de adicionar manualmente
                    TextButton(
                        onClick = { isManualEntry = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_manual_bank_button")
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Não encontrou seu banco? Adicionar manualmente")
                    }
                } else {
                    // Formulário manual
                    Text(
                        text = "Informe o nome e escolha uma cor para a instituição:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = manualName,
                        onValueChange = {
                            manualName = it
                            if (manualNameError != null) manualNameError = null
                        },
                        label = { Text("Nome da instituição") },
                        placeholder = { Text("Ex: Cooperativa de Crédito") },
                        isError = manualNameError != null,
                        supportingText = {
                            manualNameError?.let {
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
                            .testTag("manual_bank_name_input")
                    )

                    Text(
                        text = "Cor representativa:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                        width = if (selectedManualColorHex == hex) 3.dp else 0.dp,
                                        color = if (selectedManualColorHex == hex) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedManualColorHex = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedManualColorHex == hex) {
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

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = { isManualEntry = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Voltar à lista")
                        }
                        Button(
                            onClick = {
                                if (manualName.trim().isBlank()) {
                                    manualNameError = "Informe o nome do banco para continuar."
                                    return@Button
                                }
                                val customBank = BankRegistry.createCustomBank(
                                    name = manualName.trim(),
                                    colorHex = selectedManualColorHex
                                )
                                onSelectBank(customBank)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("save_manual_bank_button")
                        ) {
                            Text("Confirmar Banco")
                        }
                    }
                }
            }
        },
        confirmButton = {},
        containerColor = MaterialTheme.colorScheme.surface
    )
}
