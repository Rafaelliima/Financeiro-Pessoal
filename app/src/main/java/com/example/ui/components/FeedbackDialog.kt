package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class FeedbackType(val label: String, val code: String) {
    SUGGESTION("Sugestão", "SUGGESTION"),
    BUG("Problema / Bug", "BUG")
}

@Composable
fun FeedbackDialog(
    initialType: FeedbackType = FeedbackType.SUGGESTION,
    defaultEmail: String? = null,
    isSubmitting: Boolean = false,
    onDismiss: () -> Unit,
    onSubmit: (type: String, message: String, contactEmail: String?) -> Unit
) {
    var selectedType by remember { mutableStateOf(initialType) }
    var message by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf(defaultEmail ?: "") }
    var messageError by remember { mutableStateOf<String?>(null) }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    val hasUnsavedChanges = remember(message, contactEmail) {
        message.isNotBlank() || (defaultEmail == null && contactEmail.isNotBlank())
    }

    val attemptDismiss = {
        if (hasUnsavedChanges && !isSubmitting) {
            showUnsavedDialog = true
        } else if (!isSubmitting) {
            onDismiss()
        }
    }

    BackHandler(enabled = hasUnsavedChanges && !isSubmitting) {
        showUnsavedDialog = true
    }

    fun validateAndSubmit(): Boolean {
        if (message.trim().isBlank()) {
            messageError = "Por favor, descreva sua sugestão ou o problema encontrado."
            return false
        }
        messageError = null
        onSubmit(
            selectedType.code,
            message.trim(),
            contactEmail.trim().ifBlank { null }
        )
        return true
    }

    AlertDialog(
        onDismissRequest = attemptDismiss,
        title = {
            Text(
                text = if (selectedType == FeedbackType.SUGGESTION) "Enviar Sugestão" else "Reportar um Problema",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Tipo de Feedback
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == FeedbackType.SUGGESTION,
                        onClick = { selectedType = FeedbackType.SUGGESTION },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Lightbulb,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text("Sugestão") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedType == FeedbackType.BUG,
                        onClick = { selectedType = FeedbackType.BUG },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.BugReport,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text("Problema") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Campo de mensagem
                OutlinedTextField(
                    value = message,
                    onValueChange = {
                        message = it
                        if (messageError != null) messageError = null
                    },
                    label = { Text("Descrição detalhada", style = MaterialTheme.typography.bodySmall) },
                    placeholder = {
                        Text(
                            if (selectedType == FeedbackType.SUGGESTION)
                                "Compartilhe suas ideias de melhorias ou recursos que gostaria de ver no aplicativo..."
                            else
                                "Descreva o que aconteceu, onde ocorreu o erro e, se possível, como reproduzi-lo..."
                        )
                    },
                    minLines = 4,
                    maxLines = 6,
                    isError = messageError != null,
                    supportingText = {
                        messageError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryAccent,
                        focusedLabelColor = PrimaryAccent
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting
                )

                // E-mail para contato (opcional)
                OutlinedTextField(
                    value = contactEmail,
                    onValueChange = { contactEmail = it },
                    label = { Text("E-mail para retorno (opcional)", style = MaterialTheme.typography.bodySmall) },
                    placeholder = { Text("seu-email@exemplo.com") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryAccent,
                        focusedLabelColor = PrimaryAccent
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { validateAndSubmit() },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Enviar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = attemptDismiss,
                enabled = !isSubmitting
            ) {
                Text("Cancelar", color = TextSecondary)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )

    if (showUnsavedDialog) {
        UnsavedChangesConfirmationDialog(
            onDismissRequest = { showUnsavedDialog = false },
            onDiscard = {
                showUnsavedDialog = false
                onDismiss()
            },
            onSaveAndExit = {
                if (validateAndSubmit()) {
                    showUnsavedDialog = false
                } else {
                    showUnsavedDialog = false
                }
            }
        )
    }
}
