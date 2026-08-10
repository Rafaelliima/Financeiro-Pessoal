package com.example.gmail.parser

import com.example.data.ImportProvider
import com.example.gmail.GmailMessageHeader
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Version 1 do Parser especializado em extrair com segurança confirmações de PIX enviado do Mercado Pago.
 * Ignora rigorosamente qualquer e-mail que não seja uma saída real de dinheiro ou
 * que apresente dados incompletos/ambíguos.
 */
class MercadoPagoParserV1 : EmailParser {

    private val ignoreKeywords = listOf(
        "recebeu", "recebido", "cobrança", "cobranca", "fatura", "promoção", "promocao",
        "empréstimo", "emprestimo", "cartão", "cartao", "convite", "segurança", "seguranca",
        "código", "codigo", "senha", "oferta", "cashback", "investimento", "desconto",
        "recarga", "comprovação", "comprovacao", "alerta", "recompensa"
    )

    private val validKeywords = listOf(
        "pix", "enviou", "enviado", "transferiu", "transferência", "transferencia"
    )

    override fun parse(header: GmailMessageHeader): ParserResult {
        val subject = header.subject
        val snippet = header.snippet ?: ""
        val sender = header.sender
        val combinedText = "$subject $snippet".lowercase(Locale.ROOT)

        // 1. Verificação de Origem e Segurança
        val isMercadoPagoSender = sender.contains("mercadopago", ignoreCase = true) ||
                sender.contains("mercado livre", ignoreCase = true) ||
                combinedText.contains("mercado pago")

        if (!isMercadoPagoSender) {
            return ParserResult.Ignored("E-mail não pertence ao Mercado Pago.")
        }

        // Verifica se contém palavras-chave de bloqueio/marketing/recebimento
        if (ignoreKeywords.any { combinedText.contains(it) }) {
            return ParserResult.Ignored("E-mail contém termos ignorados (marketing, recebimento, cartão ou segurança).")
        }

        // Verifica se contém contexto de PIX / Transferência realizada
        val hasValidContext = validKeywords.any { combinedText.contains(it) }
        if (!hasValidContext) {
            return ParserResult.Ignored("E-mail não é uma confirmação de PIX ou transferência enviada.")
        }

        // 2. Extração do Valor
        val extractedValue = extractValue(subject, snippet)
            ?: return ParserResult.Ignored("Não foi possível identificar o valor do PIX com segurança.")

        // 3. Extração da Data da Operação
        val extractedDate = extractDate(header.date, snippet)
            ?: return ParserResult.Ignored("Não foi possível identificar a data do e-mail.")

        // 4. Extração do Nome do Destinatário com Verificação de Sanidade
        val (rawExtractedName, capturedFragment) = extractRecipientNameDetails(subject, snippet)
        var recipientName = rawExtractedName ?: "Destinatário não identificado"
        var confidence = 1.0

        if (rawExtractedName == null) {
            confidence = 0.5
            recipientName = "Destinatário não identificado"
        } else {
            val cleanForCheck = recipientName.replace(".", "").trim()
            val isShort = recipientName.length < 3
            val isNumericOnly = cleanForCheck.isNotEmpty() && cleanForCheck.all { it.isDigit() }
            val isTooLong = recipientName.length > 80

            if (isShort || isNumericOnly || isTooLong) {
                confidence = 0.5
                if (isNumericOnly || recipientName.isBlank()) {
                    recipientName = "Destinatário não identificado"
                }
            }
        }

        val formattedVal = String.format(Locale("pt", "BR"), "R$ %.2f", extractedValue)

        return ParserResult.Success(
            recipientName = recipientName,
            value = extractedValue,
            formattedValue = formattedVal,
            date = extractedDate,
            confidence = confidence,
            importProvider = ImportProvider.MERCADO_PAGO,
            debugCapturedFragment = capturedFragment
        )
    }

    fun extractValue(subject: String, snippet: String): Double? {
        val fullText = "$subject $snippet"
        val valueRegex = Regex("""(?i)R\$\s*([\d\.]+\,\d{2})""")
        val match = valueRegex.find(fullText) ?: return null

        val rawVal = match.groupValues[1]
        val cleanVal = rawVal.replace(".", "").replace(",", ".")
        val doubleVal = cleanVal.toDoubleOrNull() ?: return null

        return if (doubleVal > 0.0) doubleVal else null
    }

    fun extractRecipientNameDetails(subject: String, snippet: String): Pair<String?, String> {
        val cleanSubject = subject.replace(Regex("""[\r\n]+"""), " ").replace(Regex("""\s+"""), " ")
        val cleanSnippet = snippet.replace(Regex("""[\r\n]+"""), " ").replace(Regex("""\s+"""), " ")
        val fullText = "$cleanSubject $cleanSnippet"

        val patterns = listOf(
            Regex("""(?i)foi enviado\s+[^.]+\.\s*(.+?)\s*CPF/CNPJ:"""),
            Regex("""(?i)\.\s*(.+?)\s*CPF/CNPJ:"""),
            Regex("""(?i)(?:enviou|enviado|transferiu)\s+(?:um\s+)?(?:pix\s+)?(?:de\s+R\$\s*[\d\.\,]+\s+)?para\s+([A-Za-zÁ-úà-ú\.\s]{2,80})"""),
            Regex("""(?i)pix\s+para\s+([A-Za-zÁ-úà-ú\.\s]{2,80})"""),
            Regex("""(?i)transferência\s+para\s+([A-Za-zÁ-úà-ú\.\s]{2,80})"""),
            Regex("""(?i)para\s+([A-Za-zÁ-úà-ú\.\s]{2,80})""")
        )

        var lastCapturedFragment = ""

        for (pattern in patterns) {
            val match = pattern.find(fullText)
            if (match != null) {
                val rawName = match.groupValues[1]
                lastCapturedFragment = rawName
                val cleanName = sanitizeName(rawName)
                if (cleanName.isNotBlank()) {
                    return Pair(formatNameForDisplay(cleanName), lastCapturedFragment)
                }
            }
        }
        return Pair(null, lastCapturedFragment)
    }

    fun extractRecipientName(subject: String, snippet: String): String? {
        return extractRecipientNameDetails(subject, snippet).first
    }

    private fun sanitizeName(rawName: String): String {
        var name = rawName.replace(Regex("""[\r\n]+"""), " ").replace(Regex("""\s+"""), " ").trim()

        val stopWords = listOf(
            "pelo", "via", "no valor", "de R$", "com sucesso", "em ", "pela",
            "mercado pago", "data", "chave", "instituição", "instituicao"
        )

        for (word in stopWords) {
            val lower = name.lowercase(Locale.ROOT)
            val index = lower.indexOf(word)
            if (index > 0) {
                name = name.substring(0, index).trim()
            }
        }

        name = name.replace(Regex("""[^\p{L}\.\s]"""), "").trim()
        name = name.replace(Regex("""\s+"""), " ")

        return name
    }

    fun formatNameForDisplay(name: String): String {
        if (name.isBlank()) return name
        val lowercasePrepositions = setOf("de", "da", "do", "das", "dos", "e")
        val words = name.split(" ")
        return words.mapIndexed { index, word ->
            val lower = word.lowercase(Locale.ROOT)
            if (index > 0 && lower in lowercasePrepositions) {
                lower
            } else {
                lower.split(".").joinToString(".") { part ->
                    part.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                }
            }
        }.joinToString(" ")
    }

    fun extractDate(headerDate: String, snippet: String): String? {
        if (headerDate.isNotBlank()) {
            val parsedHeaderDate = parseRfcOrIsoDate(headerDate)
            if (parsedHeaderDate != null) return parsedHeaderDate
        }

        val snippetDateRegex = Regex("""(\d{2})/(\d{2})/(\d{4})""")
        val match = snippetDateRegex.find(snippet)
        if (match != null) {
            val (day, month, year) = match.destructured
            return "$year-$month-$day"
        }

        return null
    }

    private fun parseRfcOrIsoDate(dateStr: String): String? {
        val formats = listOf(
            "EEE, d MMM yyyy HH:mm:ss Z",
            "EEE, d MMM yyyy HH:mm:ss z",
            "d MMM yyyy HH:mm:ss Z",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd"
        )

        for (fmt in formats) {
            try {
                val sdf = SimpleDateFormat(fmt, Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val date = sdf.parse(dateStr)
                if (date != null) {
                    val outSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    return outSdf.format(date)
                }
            } catch (_: Exception) {}
        }
        return null
    }
}
