package com.example.gmail

/**
 * Responsável por montar consultas à Gmail API de forma centralizada.
 * Garante que filtros como 'from:mercadopago.com' não fiquem espalhados pelo código.
 */
class GmailQueryBuilder {

    private var sender: String = "mercadopago.com"
    private var customKeywords: String? = null

    fun setSender(sender: String): GmailQueryBuilder {
        this.sender = sender
        return this
    }

    fun setKeywords(keywords: String?): GmailQueryBuilder {
        this.customKeywords = keywords
        return this
    }

    fun build(): String {
        val base = "from:$sender"
        return if (!customKeywords.isNullOrBlank()) {
            "$base $customKeywords"
        } else {
            base
        }
    }

    companion object {
        fun defaultMercadoPagoQuery(): String {
            return GmailQueryBuilder().build()
        }
    }
}
