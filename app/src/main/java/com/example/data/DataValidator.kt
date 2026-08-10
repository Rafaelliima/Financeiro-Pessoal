package com.example.data

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

object DataValidator {

    fun validate(data: StorageData): ValidationResult {
        if (data.version < 1) {
            return ValidationResult(false, "Versão dos dados inválida (${data.version}).")
        }

        // Validate cards
        for (card in data.cards) {
            if (card.id.isBlank()) {
                return ValidationResult(false, "Cartão encontrado sem ID válido.")
            }
            if (card.name.isBlank()) {
                return ValidationResult(false, "Cartão ID '${card.id}' possui nome em branco.")
            }
        }

        // Validate purchases
        for (purchase in data.purchases) {
            if (purchase.id.isBlank()) {
                return ValidationResult(false, "Compra encontrada sem ID válido.")
            }
            if (purchase.name.isBlank()) {
                return ValidationResult(false, "Compra ID '${purchase.id}' possui nome em branco.")
            }
            if (purchase.totalAmount <= 0) {
                return ValidationResult(false, "Compra '${purchase.name}' possui valor inválido (R$ ${purchase.totalAmount}).")
            }
            if (purchase.isInstallment && purchase.totalInstallments < 1) {
                return ValidationResult(false, "Compra '${purchase.name}' possui parcelas inválidas (${purchase.totalInstallments}).")
            }
            if (purchase.startMonth !in 1..12 || purchase.startYear < 2000) {
                return ValidationResult(false, "Compra '${purchase.name}' possui data inicial inválida (${purchase.startMonth}/${purchase.startYear}).")
            }
        }

        // Validate subscriptions
        for (sub in data.subscriptions) {
            if (sub.id.isBlank()) {
                return ValidationResult(false, "Assinatura encontrada sem ID válido.")
            }
            if (sub.name.isBlank()) {
                return ValidationResult(false, "Assinatura ID '${sub.id}' possui nome em branco.")
            }
            if (sub.monthlyValue < 0) {
                return ValidationResult(false, "Assinatura '${sub.name}' possui valor mensal negativo.")
            }
        }

        // Validate daily expenses
        for (expense in data.dailyExpenses) {
            if (expense.id.isBlank()) {
                return ValidationResult(false, "Gasto diário encontrado sem ID válido.")
            }
            if (expense.name.isBlank()) {
                return ValidationResult(false, "Gasto diário ID '${expense.id}' possui nome em branco.")
            }
            if (expense.value <= 0) {
                return ValidationResult(false, "Gasto diário '${expense.name}' possui valor inválido (R$ ${expense.value}).")
            }
            if (expense.date.isBlank()) {
                return ValidationResult(false, "Gasto diário '${expense.name}' possui data em branco.")
            }
        }

        return ValidationResult(true)
    }
}
