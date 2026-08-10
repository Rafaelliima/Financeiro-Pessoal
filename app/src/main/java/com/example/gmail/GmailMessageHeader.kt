package com.example.gmail

/**
 * Estrutura de dados contendo apenas os metadados do e-mail.
 * Não baixa e não armazena o corpo completo da mensagem.
 */
data class GmailMessageHeader(
    val id: String,
    val subject: String,
    val sender: String,
    val date: String,
    val snippet: String? = null
)

/**
 * Resultado encapsulado de uma consulta de teste/sincronização à Gmail API.
 */
data class GmailQueryResult(
    val messages: List<GmailMessageHeader>,
    val totalFound: Int,
    val newProcessedCount: Int,
    val historyId: String? = null,
    val statusMessage: String,
    val isSuccess: Boolean
)
