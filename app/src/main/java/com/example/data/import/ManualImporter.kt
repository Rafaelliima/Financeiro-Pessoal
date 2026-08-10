package com.example.data.import

import com.example.data.DailyExpense

/**
 * Implementação manual padrão da interface ExpenseImporter.
 */
class ManualImporter : ExpenseImporter {
    override suspend fun importExpenses(): List<DailyExpense> {
        return emptyList()
    }
}
