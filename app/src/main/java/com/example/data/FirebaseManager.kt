package com.example.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class FirebaseManager {
    private val db: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("FirebaseManager", "FirebaseFirestore not initialized: ${e.message}")
            null
        }
    }
    private val TAG = "FirebaseManager"

    /**
     * Salva os dados financeiros do usuário na nuvem.
     * Vinculado ao ID único da conta Google.
     */
    suspend fun syncDataToCloud(userId: String, data: StorageData): Boolean {
        return try {
            val database = db ?: return false
            Log.d(TAG, "Iniciando sincronização para a nuvem: $userId")
            database.collection("users")
                .document(userId)
                .set(data, SetOptions.merge())
                .await()
            Log.i(TAG, "Dados sincronizados com sucesso no Firestore.")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao sincronizar com Firestore", e)
            false
        }
    }

    /**
     * Busca os dados financeiros do usuário na nuvem.
     */
    suspend fun loadDataFromCloud(userId: String): StorageData? {
        return try {
            val database = db ?: return null
            Log.d(TAG, "Buscando dados na nuvem para: $userId")
            val document = database.collection("users")
                .document(userId)
                .get()
                .await()
            
            if (document.exists()) {
                val data = document.toObject(StorageData::class.java)
                Log.i(TAG, "Dados carregados da nuvem com sucesso.")
                data
            } else {
                Log.i(TAG, "Nenhum dado encontrado na nuvem para este usuário.")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar dados no Firestore", e)
            null
        }
    }

    /**
     * Envia um feedback (sugestão ou problema) para o Discord Webhook como canal prioritário
     * e registra no Firestore em bloco isolado. Retorna true se a mensagem for entregue com sucesso.
     */
    suspend fun sendFeedback(
        type: String, // "SUGGESTION" ou "BUG"
        message: String,
        userEmail: String? = null,
        userName: String? = null,
        appVersion: String = "2.5"
    ): Boolean {
        return withContext(Dispatchers.IO) {
            // 1. Envia notificação em tempo real ao Discord Webhook (Canal Garantido)
            val discordSuccess = sendDiscordWebhook(
                type = type,
                message = message,
                userEmail = userEmail,
                userName = userName,
                appVersion = appVersion
            )

            // 2. Grava no banco Firestore em bloco secundário isolado (não quebra o fluxo se as regras negarem)
            try {
                db?.let { database ->
                    val feedbackData = hashMapOf(
                        "id" to java.util.UUID.randomUUID().toString(),
                        "type" to type,
                        "message" to message,
                        "userEmail" to userEmail,
                        "userName" to userName,
                        "appVersion" to appVersion,
                        "createdAt" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()),
                        "timestamp" to System.currentTimeMillis()
                    )
                    database.collection("feedbacks").add(feedbackData).await()
                    Log.i(TAG, "Feedback salvo também no Firestore com sucesso.")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Aviso: Gravação no Firestore ignorada/falhou (provável bloqueio de Security Rules), mas Discord foi processado.", e)
            }

            // Considera sucesso se o Discord recebeu com sucesso (ou se Firestore gravou)
            discordSuccess
        }
    }

    private suspend fun sendDiscordWebhook(
        type: String,
        message: String,
        userEmail: String?,
        userName: String?,
        appVersion: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val webhookUrl = "https://discord.com/api/webhooks/1547041639216713828/_bn3Te8GFAxLxAB1A3bFQAROO5T-TKNhTwJRawo2Q4hpJ8dBkc20SSUl8o9rT4IfFXsN"
                val isBug = type.equals("BUG", ignoreCase = true)
                val embedColor = if (isBug) 15158332 else 3066993 // Vermelho para Bug, Verde para Sugestão
                val embedTitle = if (isBug) "🐛 Novo Bug Reportado — Grana+" else "💡 Nova Sugestão de Ideia — Grana+"

                val jsonPayload = JSONObject().apply {
                    put("username", "Grana+ Bot")
                    val embed = JSONObject().apply {
                        put("title", embedTitle)
                        put("description", message)
                        put("color", embedColor)
                        val fieldsArray = JSONArray().apply {
                            put(JSONObject().apply {
                                put("name", "👤 Usuário")
                                put("value", userName?.ifBlank { "Anônimo" } ?: "Anônimo")
                                put("inline", true)
                            })
                            put(JSONObject().apply {
                                put("name", "📧 E-mail")
                                put("value", userEmail?.ifBlank { "Não informado" } ?: "Não informado")
                                put("inline", true)
                            })
                            put(JSONObject().apply {
                                put("name", "📱 Versão")
                                put("value", "v$appVersion (Grana+)")
                                put("inline", true)
                            })
                        }
                        put("fields", fieldsArray)
                        put("footer", JSONObject().apply {
                            put("text", "Grana+ • Central de Feedback")
                        })
                        val isoDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }.format(Date())
                        put("timestamp", isoDate)
                    }
                    put("embeds", JSONArray().apply { put(embed) })
                }

                val url = URL(webhookUrl)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("User-Agent", "GranaPlus-Android-App")
                    doOutput = true
                    connectTimeout = 8000
                    readTimeout = 8000
                }

                OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                    writer.write(jsonPayload.toString())
                    writer.flush()
                }

                val responseCode = connection.responseCode
                Log.d(TAG, "Notificação Discord enviada com status: $responseCode")
                connection.disconnect()
                responseCode in 200..299
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao enviar Webhook para o Discord", e)
                false
            }
        }
    }
}
