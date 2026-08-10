package com.example.data.import

import com.example.data.DailyExpense

/**
 * Interface responsável pela importação de despesas de diversas fontes (Email, OCR, CSV, API, etc.)
 * Preparada para suportar sincronização e leitura automática em sprints futuras.
 */
interface ExpenseImporter {
    suspend fun importExpenses(): List<DailyExpense>
}
