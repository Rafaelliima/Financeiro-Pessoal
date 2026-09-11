package com.example.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.PrimaryAccent

/**
 * Diálogo reutilizável de confirmação quando o usuário tenta fechar um formulário
 * que possui alterações preenchidas não salvas.
 */
@Composable
fun UnsavedChangesConfirmationDialog(
    onDismissRequest: () -> Unit,
    onDiscard: () -> Unit,
    onSaveAndExit: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = "Alterações não salvas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = "Você possui alterações que ainda não foram salvas. Deseja sair sem salvar?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            if (onSaveAndExit != null) {
                Button(
                    onClick = onSaveAndExit,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                    modifier = Modifier.testTag("unsaved_save_and_exit_button")
                ) {
                    Text("Salvar e sair")
                }
            } else {
                Button(
                    onClick = onDiscard,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("unsaved_discard_button")
                ) {
                    Text("Descartar")
                }
            }
        },
        dismissButton = {
            if (onSaveAndExit != null) {
                TextButton(
                    onClick = onDiscard,
                    modifier = Modifier.testTag("unsaved_discard_button")
                ) {
                    Text("Descartar", color = MaterialTheme.colorScheme.error)
                }
            }
            TextButton(
                onClick = onDismissRequest,
                modifier = Modifier.testTag("unsaved_cancel_button")
            ) {
                Text("Cancelar")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}
