package com.example.gmail.parser

import com.example.data.ImportProvider

/**
 * Resultado da tentativa de parse de um e-mail financeiro.
 */
sealed class ParserResult {
    data class Success(
        val recipientName: String,
        val value: Double,
        val formattedValue: String,
        val date: String, // Formato YYYY-MM-DD
        val confidence: Double = 1.0,
        val importProvider: ImportProvider = ImportProvider.MERCADO_PAGO,
        val debugCapturedFragment: String? = null
    ) : ParserResult()

    data class Ignored(
        val reason: String,
        val debugCapturedFragment: String? = null
    ) : ParserResult()
}
