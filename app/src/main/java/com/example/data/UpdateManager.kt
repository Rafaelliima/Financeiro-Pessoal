package com.example.data

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.File

data class AppVersionInfo(
    @get:PropertyName("latestVersionCode") @set:PropertyName("latestVersionCode") var latestVersionCode: Long = 0,
    @get:PropertyName("latestVersionName") @set:PropertyName("latestVersionName") var latestVersionName: String = "",
    @get:PropertyName("apkUrl") @set:PropertyName("apkUrl") var apkUrl: String = "",
    @get:PropertyName("releaseNotes") @set:PropertyName("releaseNotes") var releaseNotes: String = ""
)

class UpdateManager(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "UpdateManager"

    /**
     * Verifica se existe uma nova versão no Firestore.
     */
    suspend fun checkForUpdate(): AppVersionInfo? {
        return try {
            Log.d(TAG, "Iniciando consulta ao Firestore em config/app_version...")
            val doc = db.collection("config").document("app_version").get().await()
            if (doc.exists()) {
                val data = doc.data
                Log.d(TAG, "Dados brutos do Firestore: $data")

                // Busca os valores manualmente tratando possíveis espaços nos nomes dos campos ou erros de tipo
                val cloudCode = (data?.get("latestVersionCode") as? Long) 
                    ?: (data?.get(" latestVersionCode") as? Long) // Tenta com o espaço que apareceu no log
                    ?: 0L
                
                val cloudName = (data?.get("latestVersionName") as? String) ?: ""
                val cloudUrl = (data?.get("apkUrl") as? String) ?: ""
                val cloudNotes = (data?.get("releaseNotes") as? String) ?: ""

                val info = AppVersionInfo(cloudCode, cloudName, cloudUrl, cloudNotes)
                val currentVersionCode = getInternalVersionCode()
                
                Log.d(TAG, "Comparando: Nuvem($cloudCode) vs Celular($currentVersionCode)")

                if (info.latestVersionCode > currentVersionCode) {
                    Log.i(TAG, "Nova versão detectada!")
                    info
                } else {
                    null
                }
            } else {
                Log.w(TAG, "O documento 'config/app_version' NÃO existe no Firestore.")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "ERRO CRÍTICO NA VERIFICAÇÃO: ${e.message}")
            if (e.message?.contains("permission-denied") == true) {
                Log.e(TAG, "DICA: Verifique as Regras de Segurança (Rules) do Firestore!")
            }
            null
        }
    }

    private fun getInternalVersionCode(): Int {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode
            }
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Inicia o download do APK via DownloadManager.
     */
    fun downloadAndInstallApk(apkUrl: String) {
        val cleanUrl = apkUrl.trim()
        if (cleanUrl.isEmpty()) {
            Log.e(TAG, "URL de download está vazia!")
            return
        }

        val fileName = "update_${System.currentTimeMillis()}.apk"
        val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        if (downloadDir?.exists() == false) downloadDir.mkdirs()
        
        val destination = File(downloadDir, fileName)
        
        // Limpa downloads anteriores para economizar espaço
        downloadDir?.listFiles()?.forEach { 
            if (it.name.startsWith("update_") && it.name.endsWith(".apk")) it.delete() 
        }

        val request = DownloadManager.Request(Uri.parse(cleanUrl))
            .setTitle("Atualizando Financeiro Pessoal")
            .setDescription("Baixando nova versão do GitHub...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .addRequestHeader("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36")

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = manager.enqueue(request)

        Log.d(TAG, "Download enfileirado com ID: $downloadId. URL: $cleanUrl")

        // Registra um receiver para saber quando o download terminar e abrir o instalador
        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                if (id == downloadId) {
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor = manager.query(query)
                    if (cursor.moveToFirst()) {
                        val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        if (statusIndex != -1) {
                            val status = cursor.getInt(statusIndex)
                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                Log.i(TAG, "Download concluído com sucesso. Iniciando instalação...")
                                installApk(destination)
                            } else {
                                val reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                                val reason = if (reasonIndex != -1) cursor.getInt(reasonIndex) else -1
                                Log.e(TAG, "Download falhou. Status: $status, Razão: $reason")
                            }
                        }
                    }
                    cursor.close()
                    context.unregisterReceiver(this)
                }
            }
        }
        
        // IMPORTANTE: Para receber o sinal do DownloadManager no Android 14+, 
        // o receiver deve ser registrado como EXPORTED.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                onComplete, 
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), 
                Context.RECEIVER_EXPORTED
            )
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
    }

    private fun installApk(apkFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao abrir instalador", e)
        }
    }
}
