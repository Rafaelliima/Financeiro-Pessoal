package com.example.data

/**
 * Identifica a instituição ou provedor responsável pela importação automática do gasto.
 */
enum class ImportProvider {
    MANUAL,
    MERCADO_PAGO;

    companion object {
        fun fromString(value: String?): ImportProvider? {
            if (value.isNullOrBlank()) return null
            return try {
                valueOf(value)
            } catch (e: Exception) {
                null
            }
        }
    }
}
