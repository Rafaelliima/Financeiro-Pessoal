package com.example.data

/**
 * Origem dos dados financeiros no sistema.
 * Suporta a evolução da arquitetura para integrações futuras sem breaking changes.
 */
enum class DataSource {
    MANUAL,
    EMAIL,
    NOTIFICATION,
    IMPORT,
    API,
    OCR;

    companion object {
        fun fromString(value: String?): DataSource {
            return try {
                if (!value.isNullOrBlank()) valueOf(value.uppercase()) else MANUAL
            } catch (_: Exception) {
                MANUAL
            }
        }
    }
}
