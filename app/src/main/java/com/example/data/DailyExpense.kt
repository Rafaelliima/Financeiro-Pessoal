package com.example.data

import java.util.UUID

data class DailyExpense(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val value: Double,
    val date: String, // Ex: "2026-07-29" ou "29/07/2026"
    val source: DataSource = DataSource.MANUAL,
    val observation: String? = null,
    val sourceMessageId: String? = null,
    val emailSubject: String? = null,
    val emailSender: String? = null,
    val importProvider: ImportProvider? = null,
    val confidence: Double? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
