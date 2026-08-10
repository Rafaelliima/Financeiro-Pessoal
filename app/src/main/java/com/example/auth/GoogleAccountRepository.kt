package com.example.auth

import com.example.data.GoogleAccountData
import com.example.data.JsonStorageManager
import com.example.data.StorageData

/**
 * Repositório responsável por gerenciar a persistência do estado da conta Google no aplicativo.
 */
class GoogleAccountRepository(
    private val storageManager: JsonStorageManager
) {

    fun getGoogleAccount(): GoogleAccountData? {
        return storageManager.loadData().data?.googleAccount
    }

    fun saveGoogleAccount(accountData: GoogleAccountData, onComplete: (Boolean, String) -> Unit) {
        val currentData = storageManager.loadData().data ?: StorageData()
        val updatedData = currentData.copy(googleAccount = accountData)
        val result = storageManager.saveData(updatedData)
        onComplete(result.isSuccess, result.message)
    }

    fun clearGoogleAccount(onComplete: (Boolean, String) -> Unit) {
        val currentData = storageManager.loadData().data ?: StorageData()
        val updatedData = currentData.copy(googleAccount = GoogleAccountData(isConnected = false))
        val result = storageManager.saveData(updatedData)
        onComplete(result.isSuccess, result.message)
    }
}
