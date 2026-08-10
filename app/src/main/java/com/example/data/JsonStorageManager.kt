package com.example.data

import android.content.Context
import com.example.ui.screens.CardItem
import java.io.File

class JsonStorageManager(private val context: Context) {

    private val fileName = "dados_financeiros.json"
    private val file: File
        get() = File(context.filesDir, fileName)

    /**
     * Carrega todos os dados (Cartões, Compras e Assinaturas) do arquivo JSON local.
     * Suporta versionamento, migração automática e segurança contra arquivo corrompido.
     */
    fun loadData(): StorageOperationResult<StorageData> {
        return StorageReader.readStorage(file) { missingFile ->
            createInitialData(missingFile)
        }
    }

    /**
     * Valida os dados e realiza a escrita atômica segura via arquivo temporário (.tmp).
     */
    fun saveData(data: StorageData): StorageOperationResult<Boolean> {
        // 1. Validação de consistência de dados
        val validation = DataValidator.validate(data)
        if (!validation.isValid) {
            return StorageOperationResult(
                data = false,
                isSuccess = false,
                message = "Validação falhou: ${validation.errorMessage ?: "Dados inconsistentes."}"
            )
        }

        // 2. Escrita atômica segura em arquivo temporário (.tmp) com validação e substituição final (.json)
        return AtomicFileWriter.writeAtomic(file, data)
    }

    private fun createInitialData(targetFile: File): StorageOperationResult<StorageData> {
        val initialCards = listOf(
            CardItem(name = "Cartão de Crédito Principal"),
            CardItem(name = "Cartão de Benefícios")
        )
        val initialData = StorageData(
            version = CURRENT_VERSION,
            cards = initialCards,
            purchases = emptyList(),
            subscriptions = emptyList()
        )
        val saveResult = AtomicFileWriter.writeAtomic(targetFile, initialData)
        return if (saveResult.isSuccess) {
            StorageOperationResult(
                data = initialData,
                isSuccess = true,
                message = "Arquivo JSON v$CURRENT_VERSION criado automaticamente com dados iniciais."
            )
        } else {
            StorageOperationResult(
                data = initialData,
                isSuccess = false,
                message = "Erro ao criar arquivo JSON inicial: ${saveResult.message}"
            )
        }
    }
}
