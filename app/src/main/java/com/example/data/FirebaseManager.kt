package com.example.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirebaseManager {
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "FirebaseManager"

    /**
     * Salva os dados financeiros do usuário na nuvem.
     * Vinculado ao ID único da conta Google.
     */
    suspend fun syncDataToCloud(userId: String, data: StorageData): Boolean {
        return try {
            Log.d(TAG, "Iniciando sincronização para a nuvem: $userId")
            db.collection("users")
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
            Log.d(TAG, "Buscando dados na nuvem para: $userId")
            val document = db.collection("users")
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
}
