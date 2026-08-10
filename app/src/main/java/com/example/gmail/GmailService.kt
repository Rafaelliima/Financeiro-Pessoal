package com.example.gmail

import android.accounts.Account
import android.content.Context
import android.util.Log
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

/**
 * Camada isolada do Cliente Gmail API.
 * Realiza requisições HTTP REST diretas e autenticadas via OkHttp e GoogleAuthUtil (Play Services).
 * Utiliza os endpoints oficiais da Gmail API: https://gmail.googleapis.com/gmail/v1/users/me/messages
 * Busca apenas os metadados das mensagens (MessageId, Subject, Sender, Date).
 * Nunca faz download ou parser do corpo completo do e-mail e nunca cadastra despesas.
 */
class GmailService(private val context: Context) {

    companion object {
        private const val TAG = "GmailService"
        private const val BASE_GMAIL_URL = "https://gmail.googleapis.com/gmail/v1/users/me/messages"
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val scope = "oauth2:https://www.googleapis.com/auth/gmail.readonly"

    suspend fun searchMessages(
        userEmail: String,
        query: String,
        maxResults: Int = 20
    ): Result<List<GmailMessageHeader>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Iniciando autenticação OAuth2 para $userEmail")
            // 1. Obtém o Token de Acesso OAuth2 oficial do Google Play Services
            val account = Account(userEmail, "com.google")
            val token = try {
                GoogleAuthUtil.getToken(context, account, scope)
            } catch (e: UserRecoverableAuthException) {
                Log.e(TAG, "Sessão Google expirada para $userEmail", e)
                return@withContext Result.failure(Exception("Sessão Google expirada. Por favor, reconecte sua conta nas Configurações."))
            } catch (e: GoogleAuthException) {
                Log.e(TAG, "Falha na autenticação OAuth2 Google: ${e.message}", e)
                return@withContext Result.failure(Exception("Falha na autenticação OAuth2 com o Google: ${e.message}"))
            }

            Log.d(TAG, "Token OAuth2 obtido com sucesso. Consultando Gmail API...")

            // 2. Consulta os MessageIds na Gmail REST API oficial
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val listUrl = "$BASE_GMAIL_URL?q=$encodedQuery&maxResults=$maxResults"

            val listRequest = Request.Builder()
                .url(listUrl)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Accept", "application/json")
                .get()
                .build()

            val listResponse = client.newCall(listRequest).execute()
            val code = listResponse.code

            if (!listResponse.isSuccessful) {
                listResponse.close()
                Log.e(TAG, "Erro HTTP $code retornado ao consultar $listUrl")
                return@withContext when (code) {
                    401 -> Result.failure(Exception("Token expirado ou inválido (HTTP 401). Reconecte sua conta."))
                    403 -> Result.failure(Exception("Permissão insuficiente ou escopo 'gmail.readonly' não concedido (HTTP 403)."))
                    404 -> Result.failure(Exception("Endpoint da Gmail API não encontrado (HTTP 404)."))
                    400 -> Result.failure(Exception("Requisição inválida enviada para a Gmail API (HTTP 400)."))
                    429 -> Result.failure(Exception("Limite de requisições da Gmail API excedido (HTTP 429). Tente novamente mais tarde."))
                    in 500..599 -> Result.failure(Exception("Erro interno nos servidores da Gmail API (HTTP $code)."))
                    else -> Result.failure(Exception("Erro na consulta à Gmail API (Código HTTP $code)."))
                }
            }

            val listResponseBody = listResponse.body?.string() ?: ""
            listResponse.close()

            val listJson = JSONObject(listResponseBody)
            val messagesArray = listJson.optJSONArray("messages")
            if (messagesArray == null || messagesArray.length() == 0) {
                Log.i(TAG, "Nenhuma mensagem encontrada para a query: $query")
                return@withContext Result.success(emptyList())
            }

            Log.i(TAG, "${messagesArray.length()} mensagem(ns) encontrada(s) na Gmail API.")
            val headersList = mutableListOf<GmailMessageHeader>()

            // 3. Para cada MessageId, obtém apenas os cabeçalhos de metadados (Subject, From, Date)
            for (i in 0 until messagesArray.length()) {
                val msgObj = messagesArray.optJSONObject(i) ?: continue
                val messageId = msgObj.optString("id")
                if (messageId.isBlank()) continue

                val msgUrl = "$BASE_GMAIL_URL/$messageId?format=metadata&metadataHeaders=Subject&metadataHeaders=From&metadataHeaders=Date"
                val msgRequest = Request.Builder()
                    .url(msgUrl)
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Accept", "application/json")
                    .get()
                    .build()

                val msgResponse = try {
                    client.newCall(msgRequest).execute()
                } catch (e: Exception) {
                    Log.w(TAG, "Erro de rede ao buscar detalhes da mensagem $messageId", e)
                    continue
                }

                if (msgResponse.isSuccessful) {
                    val msgResponseBody = msgResponse.body?.string() ?: ""
                    msgResponse.close()
                    val msgJson = JSONObject(msgResponseBody)
                    val snippet = msgJson.optString("snippet", "")

                    val payload = msgJson.optJSONObject("payload")
                    val headersArray = payload?.optJSONArray("headers")

                    var subject = "Sem assunto"
                    var sender = userEmail
                    var date = ""

                    if (headersArray != null) {
                        for (j in 0 until headersArray.length()) {
                            val header = headersArray.optJSONObject(j) ?: continue
                            val name = header.optString("name", "").lowercase()
                            val value = header.optString("value", "")
                            when (name) {
                                "subject" -> subject = value.ifBlank { subject }
                                "from" -> sender = value.ifBlank { sender }
                                "date" -> date = value.ifBlank { date }
                            }
                        }
                    }

                    headersList.add(
                        GmailMessageHeader(
                            id = messageId,
                            subject = subject,
                            sender = sender,
                            date = date,
                            snippet = snippet
                        )
                    )
                } else {
                    Log.w(TAG, "Falha ao buscar mensagem $messageId: HTTP ${msgResponse.code}")
                    msgResponse.close()
                }
            }

            Log.d(TAG, "Metadados obtidos com sucesso para ${headersList.size} mensagens.")
            Result.success(headersList)
        } catch (e: UnknownHostException) {
            Log.e(TAG, "Sem conexão com a internet", e)
            Result.failure(Exception("Sem conexão com a internet. Verifique sua rede e tente novamente."))
        } catch (e: IOException) {
            Log.e(TAG, "Erro de comunicação de rede", e)
            Result.failure(Exception("Erro de comunicação de rede: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "Erro inesperado ao processar consulta Gmail", e)
            Result.failure(Exception("Erro ao processar consulta Gmail: ${e.localizedMessage ?: e.message}"))
        }
    }
}
