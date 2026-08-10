package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PowerOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.GoogleAccountData
import com.example.data.SyncEmailDiagnosticLog
import com.example.ui.components.StatusFeedbackBanner
import com.example.ui.theme.DividerColor
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    googleAccount: GoogleAccountData? = null,
    onConnectClick: () -> Unit = {},
    onDisconnectClick: () -> Unit = {},
    onTestGmailClick: () -> Unit = {},
    isTestingGmail: Boolean = false,
    statusMessage: String? = null,
    isErrorStatus: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showDisconnectDialog by remember { mutableStateOf(false) }
    var showProfileDetails by remember { mutableStateOf(true) }

    val isConnected = googleAccount?.isConnected == true

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen")
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Configurações",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )

        // Status Banner de feedback
        if (!statusMessage.isNullOrBlank()) {
            StatusFeedbackBanner(
                message = statusMessage,
                isError = isErrorStatus
            )
        }

        // Seção 1: Perfil e Conta (Menu expansível)
        var showAccountMenu by remember { mutableStateOf(false) }
        var showSyncMenu by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            SettingItemRow(
                icon = Icons.Outlined.Person,
                title = "Perfil e Conta",
                subtitle = if (isConnected) (googleAccount?.userEmail ?: "Conta Google Conectada") else "Conecte sua conta Google",
                onClick = { showAccountMenu = !showAccountMenu },
                trailingIcon = if (showAccountMenu) Icons.Outlined.KeyboardArrowDown else Icons.Outlined.KeyboardArrowRight
            )

            if (showAccountMenu) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp)
                        .testTag("google_account_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Status da Conta:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )

                            SuggestionChip(
                                onClick = {},
                                label = {
                                    Text(
                                        text = if (isConnected) "Conectado" else "Desconectado",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.testTag("connection_status_chip")
                                    )
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (isConnected) Icons.Outlined.CheckCircle else Icons.Outlined.PowerOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (isConnected) MaterialTheme.colorScheme.primaryContainer else DividerColor,
                                    labelColor = if (isConnected) PrimaryAccent else TextSecondary,
                                    iconContentColor = if (isConnected) PrimaryAccent else TextSecondary
                                ),
                                border = null
                            )
                        }

                        if (isConnected) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("connected_account_details")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AccountCircle,
                                        contentDescription = null,
                                        tint = PrimaryAccent,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Column {
                                        Text(
                                            text = googleAccount?.userName ?: "Usuário Google",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            modifier = Modifier.testTag("account_user_name")
                                        )
                                        Text(
                                            text = googleAccount?.userEmail ?: "Sem e-mail",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSecondary,
                                            modifier = Modifier.testTag("account_user_email")
                                        )
                                    }
                                }

                                googleAccount?.connectedAt?.let { connDate ->
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Conectado em: $connDate",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        modifier = Modifier.testTag("account_connected_at")
                                    )
                                }

                                val syncState = googleAccount?.emailSyncState
                                val scopeGranted = syncState?.scopeGranted == true
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Escopo Gmail:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    Text(
                                        text = if (scopeGranted) "Concedido (gmail.readonly)" else "Não concedido",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (scopeGranted) PrimaryAccent else MaterialTheme.colorScheme.error,
                                        modifier = Modifier.testTag("gmail_scope_status")
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                OutlinedButton(
                                    onClick = { showDisconnectDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("disconnect_google_button"),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.PowerOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp).padding(end = 6.dp)
                                    )
                                    Text("Desconectar conta", fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Text(
                                text = "Conecte sua conta Google utilizando a autenticação oficial do Android (OAuth 2.0). Nenhuma senha é solicitada ou armazenada.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )

                            Button(
                                onClick = onConnectClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("connect_google_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryAccent,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AccountCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp).padding(end = 6.dp)
                                )
                                Text("Conectar conta Google", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = DividerColor)

        // Seção 2: Sincronização de Gastos (Menu expansível)
        val syncState = googleAccount?.emailSyncState

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            SettingItemRow(
                icon = Icons.Outlined.CloudSync,
                title = "Sincronização",
                subtitle = if (isConnected) "Sincronização automática ativa (a cada 1h e ao abrir)" else "Sincronização indisponível",
                onClick = { showSyncMenu = !showSyncMenu },
                trailingIcon = if (showSyncMenu) Icons.Outlined.KeyboardArrowDown else Icons.Outlined.KeyboardArrowRight
            )

            if (showSyncMenu) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp)
                        .testTag("gmail_diagnostic_section"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isConnected) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Última sincronização:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text(
                                    text = syncState?.lastSyncedAt ?: "Nunca",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary,
                                    modifier = Modifier.testTag("gmail_last_sync")
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total de e-mails encontrados:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text(
                                    text = "${syncState?.totalEmailsFound ?: 0}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    modifier = Modifier.testTag("gmail_total_found")
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total de gastos importados:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text(
                                    text = "${syncState?.totalImported ?: 0}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryAccent,
                                    modifier = Modifier.testTag("gmail_total_imported")
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total de e-mails ignorados:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text(
                                    text = "${syncState?.totalIgnored ?: 0}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    modifier = Modifier.testTag("gmail_total_ignored")
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Último gasto importado:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text(
                                    text = syncState?.lastImportedExpense ?: "Nenhum",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary,
                                    modifier = Modifier.testTag("gmail_last_imported_expense")
                                )
                            }

                            syncState?.lastError?.let { err ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Último erro:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                    Text(
                                        text = err,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.testTag("gmail_last_error")
                                    )
                                }
                            }

                            syncState?.statusMessage?.let { msg ->
                                Text(
                                    text = "Status: $msg",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (syncState.lastError == null) PrimaryAccent else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.testTag("gmail_sync_status_msg")
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = onTestGmailClick,
                                enabled = !isTestingGmail,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("test_gmail_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryAccent
                                )
                            ) {
                                Text(
                                    text = if (isTestingGmail) "Sincronizando com Gmail..." else "Sincronizar agora",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            HorizontalDivider(color = DividerColor)

                            GmailSyncDiagnosticSection(logs = syncState?.diagnosticLogs ?: emptyList())
                        } else {
                            Text(
                                text = "Sincronização indisponível. Conecte sua conta Google no menu Perfil e Conta acima.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = DividerColor)

        // Sobre
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "Sobre",
                tint = TextSecondary,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column {
                Text(
                    text = "Financeiro Pessoal",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
                Text(
                    text = "Versão 1.0 (Google OAuth 2.0 Oficial)",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }

    // Diálogo de Confirmação de Desconexão
    if (showDisconnectDialog) {
        AlertDialog(
            onDismissRequest = { showDisconnectDialog = false },
            title = {
                Text("Desconectar conta Google?")
            },
            text = {
                Text("Deseja realmente desconectar a conta ${googleAccount?.userEmail ?: ""}? A sessão Google será revogada.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDisconnectDialog = false
                        onDisconnectClick()
                    },
                    modifier = Modifier.testTag("confirm_disconnect_button")
                ) {
                    Text("Desconectar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisconnectDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun SettingItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailingIcon: ImageVector = Icons.Outlined.KeyboardArrowRight
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = TextSecondary,
            modifier = Modifier.padding(end = 16.dp)
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Icon(
            imageVector = trailingIcon,
            contentDescription = null,
            tint = TextSecondary
        )
    }
}

@Composable
private fun GmailSyncDiagnosticSection(
    logs: List<SyncEmailDiagnosticLog>
) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("gmail_diagnostic_logs_section"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.BugReport,
                        contentDescription = null,
                        tint = PrimaryAccent,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Diagnóstico da última sincronização",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                TextButton(onClick = { expanded = !expanded }) {
                    Text(
                        text = if (expanded) "Ocultar" else "Exibir (${logs.size})",
                        color = PrimaryAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "Modo de diagnóstico para auditoria e depuração de e-mails processados.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            if (expanded) {
                if (logs.isEmpty()) {
                    Text(
                        text = "Nenhum e-mail processado na última sincronização.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    logs.forEachIndexed { index, log ->
                        DiagnosticEmailLogCard(index = index, log = log)
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagnosticEmailLogCard(
    index: Int,
    log: SyncEmailDiagnosticLog
) {
    var showTechDetails by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "E-mail #${index + 1}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                val isImported = log.finalResult.contains("Importado", ignoreCase = true)
                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            text = log.finalResult,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (isImported) PrimaryAccent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                        labelColor = if (isImported) PrimaryAccent else MaterialTheme.colorScheme.error
                    ),
                    border = null
                )
            }

            DiagnosticItemRow(
                label = "Valor extraído",
                value = log.extractedValue,
                highlight = log.extractedValue != "não encontrado"
            )
            DiagnosticItemRow(
                label = "Nome extraído",
                value = log.extractedName,
                highlight = log.extractedName != "não encontrado"
            )

            TextButton(
                onClick = { showTechDetails = !showTechDetails },
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Icon(
                    imageVector = if (showTechDetails) Icons.Outlined.KeyboardArrowDown else Icons.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                    tint = PrimaryAccent,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    text = if (showTechDetails) "Ocultar detalhes técnicos" else "Ver detalhes técnicos",
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimaryAccent,
                    fontWeight = FontWeight.Medium
                )
            }

            if (showTechDetails) {
                DiagnosticItemRow(label = "MessageId", value = log.messageId)
                DiagnosticItemRow(label = "Subject", value = log.subject)
                DiagnosticItemRow(label = "From", value = log.sender)
                DiagnosticItemRow(label = "Date", value = log.date)
                DiagnosticItemRow(label = "Snippet retornado pela Gmail API", value = log.snippet.ifBlank { "(Vazio)" })
                DiagnosticItemRow(
                    label = "Data extraída",
                    value = log.extractedDate,
                    highlight = log.extractedDate != "não encontrada"
                )

                log.ignoreReason?.let { reason ->
                    DiagnosticItemRow(
                        label = "Regra (Motivo)",
                        value = reason,
                        isError = true
                    )
                }

                val isIgnored = log.finalResult.equals("Ignorado", ignoreCase = true)
                val isNameReason = log.ignoreReason?.let { reason ->
                    reason.contains("nome", ignoreCase = true) || reason.contains("recebedor", ignoreCase = true)
                } == true

                if (isIgnored && isNameReason) {
                    val fragmentDisplay = log.debugCapturedFragment?.ifEmpty { "(Nenhum match do regex)" } ?: "(Nenhum match do regex)"
                    DiagnosticItemRow(
                        label = "Trecho capturado pelo regex (Debug)",
                        value = fragmentDisplay,
                        isError = true
                    )
                }
            }
        }
    }
}

@Composable
private fun DiagnosticItemRow(
    label: String,
    value: String,
    highlight: Boolean = false,
    isError: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (highlight || isError) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isError -> MaterialTheme.colorScheme.error
                highlight -> PrimaryAccent
                else -> TextPrimary
            }
        )
    }
}
