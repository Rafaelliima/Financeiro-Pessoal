package com.example.gmail

import android.util.Log
import com.example.auth.GoogleAccountRepository
import com.example.data.DailyExpense
import com.example.data.DataSource
import com.example.data.EmailSyncState
import com.example.data.ImportProvider
import com.example.data.StorageData
import com.example.data.SyncEmailDiagnosticLog
import com.example.gmail.parser.MercadoPagoParserV1
import com.example.gmail.parser.ParserResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Resultado da sincronização e importação de gastos via e-mail.
 */
data class ExpenseSyncResult(
    val totalFound: Int,
    val newImportedCount: Int,
    val newIgnoredCount: Int,
    val importedExpenses: List<DailyExpense>,
    val updatedStorageData: StorageData?,
    val statusMessage: String,
    val isSuccess: Boolean
)

/**
 * Repositório responsável por orquestrar as operações da Gmail API e
 * gerenciar o estado incremental de sincronização sem alterar nenhuma lógica financeira.
 */
class GmailRepository(
    private val gmailService: GmailService,
    private val accountRepository: GoogleAccountRepository
) {

    companion object {
        private const val TAG = "GmailRepository"
    }

    /**
     * Sincroniza e importa automaticamente gastos de PIX enviado do Mercado Pago.
     * Utiliza o MercadoPagoParserV1 para extrair e validar com segurança nome, valor e data.
     * Realiza deduplicação por MessageId e por sourceMessageId já gravados em DailyExpense.
     */
    suspend fun syncAndImportMercadoPagoExpenses(
        currentData: StorageData,
        saveStorageData: (StorageData) -> Unit
    ): ExpenseSyncResult {
        Log.d(TAG, "Iniciando processo de sincronização e importação de e-mails...")
        val googleAccount = currentData.googleAccount ?: accountRepository.getGoogleAccount()
        val userEmail = googleAccount?.userEmail

        if (googleAccount == null || !googleAccount.isConnected || userEmail.isNullOrBlank()) {
            val errorMsg = "Usuário não conectado à conta Google."
            Log.w(TAG, "Sincronização abortada: $errorMsg")
            return ExpenseSyncResult(
                totalFound = 0,
                newImportedCount = 0,
                newIgnoredCount = 0,
                importedExpenses = emptyList(),
                updatedStorageData = null,
                statusMessage = errorMsg,
                isSuccess = false
            )
        }

        val query = GmailQueryBuilder.defaultMercadoPagoQuery()
        Log.d(TAG, "Executando busca no Gmail com a query: $query")
        val result = gmailService.searchMessages(userEmail, query)

        val currentSync = googleAccount.emailSyncState ?: currentData.emailSyncState ?: EmailSyncState()
        val formattedNow = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR")).format(Date())

        return result.fold(
            onSuccess = { messages ->
                Log.i(TAG, "Consulta Gmail executada com sucesso. ${messages.size} mensagem(ns) encontrada(s).")
                val existingProcessedIds = currentSync.processedMessageIds.toSet()
                val existingExpenseMessageIds = currentData.dailyExpenses.mapNotNull { it.sourceMessageId }.toSet()

                val unprocessedMessages = messages.filter {
                    it.id !in existingProcessedIds && it.id !in existingExpenseMessageIds
                }

                Log.d(TAG, "Deduplicação: ${messages.size - unprocessedMessages.size} e-mail(s) já processado(s), ${unprocessedMessages.size} e-mail(s) novo(s) para analisar.")

                val parser = MercadoPagoParserV1()
                val newlyImportedExpenses = mutableListOf<DailyExpense>()
                val newlyProcessedIds = mutableListOf<String>()
                var newlyIgnoredCount = 0
                var lastImportedString: String? = null

                val isoSdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                val diagnosticLogs = mutableListOf<SyncEmailDiagnosticLog>()
                val unprocessedSet = unprocessedMessages.map { it.id }.toSet()

                for (header in messages) {
                    val valNum = parser.extractValue(header.subject, header.snippet ?: "")
                    val valStr = if (valNum != null) String.format(Locale("pt", "BR"), "R$ %.2f", valNum) else "não encontrado"
                    val nameDetails = parser.extractRecipientNameDetails(header.subject, header.snippet ?: "")
                    val nameStr = nameDetails.first ?: "não encontrado"
                    val capturedFragment = nameDetails.second
                    val dateStr = parser.extractDate(header.date, header.snippet ?: "") ?: "não encontrada"

                    if (header.id in unprocessedSet) {
                        Log.d(TAG, "Executando parser no e-mail ID: ${header.id} | Assunto: ${header.subject}")
                        when (val parseResult = parser.parse(header)) {
                            is ParserResult.Success -> {
                                Log.i(TAG, "Parser aprovou e-mail ID ${header.id}: ${parseResult.formattedValue} para ${parseResult.recipientName}")
                                val newExpense = DailyExpense(
                                    id = UUID.randomUUID().toString(),
                                    name = parseResult.recipientName,
                                    value = parseResult.value,
                                    date = parseResult.date,
                                    source = DataSource.EMAIL,
                                    observation = "Importado automaticamente do Mercado Pago",
                                    sourceMessageId = header.id,
                                    emailSubject = header.subject,
                                    emailSender = header.sender,
                                    importProvider = parseResult.importProvider,
                                    confidence = parseResult.confidence,
                                    createdAt = isoSdf.format(Date()),
                                    updatedAt = isoSdf.format(Date())
                                )
                                newlyImportedExpenses.add(newExpense)
                                newlyProcessedIds.add(header.id)
                                lastImportedString = "${parseResult.formattedValue} - ${parseResult.recipientName}"

                                val finalRes = if (parseResult.confidence < 1.0) "Importado (Baixa confiança)" else "Importado"
                                val warningText = if (parseResult.confidence < 1.0) "Aviso: Nome do recebedor de baixa confiança (${parseResult.recipientName}). Recomenda-se revisão manual." else null

                                diagnosticLogs.add(
                                    SyncEmailDiagnosticLog(
                                        messageId = header.id,
                                        subject = header.subject,
                                        sender = header.sender,
                                        date = header.date,
                                        snippet = header.snippet ?: "",
                                        extractedValue = parseResult.formattedValue,
                                        extractedName = parseResult.recipientName,
                                        extractedDate = parseResult.date,
                                        ignoreReason = warningText,
                                        finalResult = finalRes,
                                        debugCapturedFragment = parseResult.debugCapturedFragment ?: capturedFragment
                                    )
                                )
                            }
                            is ParserResult.Ignored -> {
                                Log.d(TAG, "Parser ignorou e-mail ID ${header.id}: ${parseResult.reason}")
                                newlyIgnoredCount++
                                newlyProcessedIds.add(header.id)

                                diagnosticLogs.add(
                                    SyncEmailDiagnosticLog(
                                        messageId = header.id,
                                        subject = header.subject,
                                        sender = header.sender,
                                        date = header.date,
                                        snippet = header.snippet ?: "",
                                        extractedValue = valStr,
                                        extractedName = nameStr,
                                        extractedDate = dateStr,
                                        ignoreReason = parseResult.reason,
                                        finalResult = "Ignorado",
                                        debugCapturedFragment = parseResult.debugCapturedFragment ?: capturedFragment
                                    )
                                )
                            }
                        }
                    } else {
                        val parseResult = parser.parse(header)
                        val isSuccess = parseResult is ParserResult.Success
                        val reasonText = if (isSuccess) "E-mail já importado em sincronização anterior" else ((parseResult as? ParserResult.Ignored)?.reason ?: "Já processado anteriormente")
                        val finalRes = if (isSuccess) "Importado" else "Ignorado"

                        diagnosticLogs.add(
                            SyncEmailDiagnosticLog(
                                messageId = header.id,
                                subject = header.subject,
                                sender = header.sender,
                                date = header.date,
                                snippet = header.snippet ?: "",
                                extractedValue = if (isSuccess) (parseResult as ParserResult.Success).formattedValue else valStr,
                                extractedName = if (isSuccess) (parseResult as ParserResult.Success).recipientName else nameStr,
                                extractedDate = if (isSuccess) (parseResult as ParserResult.Success).date else dateStr,
                                ignoreReason = reasonText,
                                finalResult = finalRes,
                                debugCapturedFragment = (parseResult as? ParserResult.Success)?.debugCapturedFragment ?: (parseResult as? ParserResult.Ignored)?.debugCapturedFragment ?: capturedFragment
                            )
                        )
                    }
                }

                val updatedDailyExpenses = currentData.dailyExpenses + newlyImportedExpenses
                val allProcessedIds = (existingProcessedIds + newlyProcessedIds + messages.map { it.id }).distinct()

                val newTotalImported = currentSync.totalImported + newlyImportedExpenses.size
                val newTotalIgnored = currentSync.totalIgnored + newlyIgnoredCount

                val latestEmail = messages.firstOrNull()

                val statusMsg = if (unprocessedMessages.isEmpty()) {
                    if (messages.isEmpty()) {
                        "Sincronização concluída com sucesso. Nenhum e-mail do Mercado Pago encontrado."
                    } else {
                        "Sincronização concluída com sucesso. Todos os ${messages.size} e-mail(s) já foram processados."
                    }
                } else {
                    "Sincronização concluída com sucesso! ${newlyImportedExpenses.size} gasto(s) importado(s), $newlyIgnoredCount e-mail(s) ignorado(s)."
                }

                val updatedSyncState = currentSync.copy(
                    lastSyncedAt = formattedNow,
                    processedMessageIds = allProcessedIds,
                    totalEmailsFound = messages.size,
                    totalEmailsProcessed = allProcessedIds.size,
                    totalImported = newTotalImported,
                    totalIgnored = newTotalIgnored,
                    lastImportedExpense = lastImportedString ?: currentSync.lastImportedExpense,
                    lastError = null,
                    scopeGranted = true,
                    statusMessage = statusMsg,
                    lastEmailSubject = latestEmail?.subject ?: currentSync.lastEmailSubject,
                    lastEmailSender = latestEmail?.sender ?: currentSync.lastEmailSender,
                    lastEmailDate = latestEmail?.date ?: currentSync.lastEmailDate,
                    diagnosticLogs = diagnosticLogs
                )

                val updatedAccount = googleAccount.copy(emailSyncState = updatedSyncState)
                val newStorageData = currentData.copy(
                    dailyExpenses = updatedDailyExpenses,
                    googleAccount = updatedAccount,
                    emailSyncState = updatedSyncState
                )

                Log.d(TAG, "Persistindo ${newlyImportedExpenses.size} novos lançamentos e atualizando EmailSyncState...")
                saveStorageData(newStorageData)
                accountRepository.saveGoogleAccount(updatedAccount) { _, _ -> }

                ExpenseSyncResult(
                    totalFound = messages.size,
                    newImportedCount = newlyImportedExpenses.size,
                    newIgnoredCount = newlyIgnoredCount,
                    importedExpenses = newlyImportedExpenses,
                    updatedStorageData = newStorageData,
                    statusMessage = statusMsg,
                    isSuccess = true
                )
            },
            onFailure = { error ->
                val errorMsg = error.message ?: "Erro ao consultar a Gmail API."
                Log.e(TAG, "Falha na sincronização Gmail API: $errorMsg", error)
                val updatedSyncState = currentSync.copy(
                    lastSyncedAt = formattedNow,
                    lastError = errorMsg,
                    scopeGranted = !errorMsg.contains("escopo", ignoreCase = true) && !errorMsg.contains("negada", ignoreCase = true),
                    statusMessage = errorMsg
                )

                val updatedAccount = googleAccount.copy(emailSyncState = updatedSyncState)
                val newStorageData = currentData.copy(
                    googleAccount = updatedAccount,
                    emailSyncState = updatedSyncState
                )

                saveStorageData(newStorageData)
                accountRepository.saveGoogleAccount(updatedAccount) { _, _ -> }

                ExpenseSyncResult(
                    totalFound = currentSync.totalEmailsFound,
                    newImportedCount = 0,
                    newIgnoredCount = 0,
                    importedExpenses = emptyList(),
                    updatedStorageData = newStorageData,
                    statusMessage = errorMsg,
                    isSuccess = false
                )
            }
        )
    }

    /**
     * Executa uma consulta de teste à Gmail API com controle de mensagens processadas.
     * Utiliza MessageId para evitar duplicidade e salvar apenas metadados no EmailSyncState.
     */
    suspend fun testGmailConnection(): GmailQueryResult {
        val googleAccount = accountRepository.getGoogleAccount()
        val userEmail = googleAccount?.userEmail

        if (googleAccount == null || !googleAccount.isConnected || userEmail.isNullOrBlank()) {
            val errorMsg = "Usuário não conectado à conta Google."
            return GmailQueryResult(
                messages = emptyList(),
                totalFound = 0,
                newProcessedCount = 0,
                statusMessage = errorMsg,
                isSuccess = false
            )
        }

        val query = GmailQueryBuilder.defaultMercadoPagoQuery()
        val result = gmailService.searchMessages(userEmail, query)

        val currentSync = googleAccount.emailSyncState ?: EmailSyncState()
        val formattedNow = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR")).format(Date())

        return result.fold(
            onSuccess = { messages ->
                val existingIds = currentSync.processedMessageIds.toSet()
                val newMessages = messages.filter { it.id !in existingIds }

                val updatedProcessedIds = (existingIds + newMessages.map { it.id }).toList()
                val latestEmail = messages.firstOrNull()

                val parser = MercadoPagoParserV1()
                val diagnosticLogs = messages.map { header ->
                    val valNum = parser.extractValue(header.subject, header.snippet ?: "")
                    val valStr = if (valNum != null) String.format(Locale("pt", "BR"), "R$ %.2f", valNum) else "não encontrado"
                    val nameDetails = parser.extractRecipientNameDetails(header.subject, header.snippet ?: "")
                    val nameStr = nameDetails.first ?: "não encontrado"
                    val capturedFragment = nameDetails.second
                    val dateStr = parser.extractDate(header.date, header.snippet ?: "") ?: "não encontrada"

                    when (val parseResult = parser.parse(header)) {
                        is ParserResult.Success -> {
                            val finalRes = if (parseResult.confidence < 1.0) "Importado (Baixa confiança)" else "Importado"
                            val warningText = if (parseResult.confidence < 1.0) "Aviso: Nome do recebedor de baixa confiança (${parseResult.recipientName}). Recomenda-se revisão manual." else null

                            SyncEmailDiagnosticLog(
                                messageId = header.id,
                                subject = header.subject,
                                sender = header.sender,
                                date = header.date,
                                snippet = header.snippet ?: "",
                                extractedValue = parseResult.formattedValue,
                                extractedName = parseResult.recipientName,
                                extractedDate = parseResult.date,
                                ignoreReason = warningText,
                                finalResult = finalRes,
                                debugCapturedFragment = parseResult.debugCapturedFragment ?: capturedFragment
                            )
                        }
                        is ParserResult.Ignored -> {
                            SyncEmailDiagnosticLog(
                                messageId = header.id,
                                subject = header.subject,
                                sender = header.sender,
                                date = header.date,
                                snippet = header.snippet ?: "",
                                extractedValue = valStr,
                                extractedName = nameStr,
                                extractedDate = dateStr,
                                ignoreReason = parseResult.reason,
                                finalResult = "Ignorado",
                                debugCapturedFragment = parseResult.debugCapturedFragment ?: capturedFragment
                            )
                        }
                    }
                }

                val updatedSyncState = currentSync.copy(
                    lastSyncedAt = formattedNow,
                    processedMessageIds = updatedProcessedIds,
                    totalEmailsFound = messages.size,
                    totalEmailsProcessed = updatedProcessedIds.size,
                    scopeGranted = true,
                    statusMessage = if (messages.isEmpty()) {
                        "Conexão OK. Nenhum e-mail encontrado para '$query'."
                    } else {
                        "Conexão OK. ${messages.size} mensagem(ns) encontrada(s) (${newMessages.size} nova(s))."
                    },
                    lastEmailSubject = latestEmail?.subject ?: currentSync.lastEmailSubject,
                    lastEmailSender = latestEmail?.sender ?: currentSync.lastEmailSender,
                    lastEmailDate = latestEmail?.date ?: currentSync.lastEmailDate,
                    diagnosticLogs = diagnosticLogs
                )

                val updatedAccount = googleAccount.copy(emailSyncState = updatedSyncState)
                accountRepository.saveGoogleAccount(updatedAccount) { _, _ -> }

                GmailQueryResult(
                    messages = messages,
                    totalFound = messages.size,
                    newProcessedCount = newMessages.size,
                    statusMessage = updatedSyncState.statusMessage ?: "Conexão bem sucedida.",
                    isSuccess = true
                )
            },
            onFailure = { error ->
                val errorMsg = error.message ?: "Erro ao consultar a Gmail API."
                val updatedSyncState = currentSync.copy(
                    lastSyncedAt = formattedNow,
                    scopeGranted = !errorMsg.contains("escopo", ignoreCase = true) && !errorMsg.contains("negada", ignoreCase = true),
                    statusMessage = errorMsg
                )

                val updatedAccount = googleAccount.copy(emailSyncState = updatedSyncState)
                accountRepository.saveGoogleAccount(updatedAccount) { _, _ -> }

                GmailQueryResult(
                    messages = emptyList(),
                    totalFound = currentSync.totalEmailsFound,
                    newProcessedCount = 0,
                    statusMessage = errorMsg,
                    isSuccess = false
                )
            }
        )
    }
}
