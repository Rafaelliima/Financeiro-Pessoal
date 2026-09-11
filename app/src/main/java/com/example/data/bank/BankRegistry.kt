package com.example.data.bank

import com.example.R
import java.text.Normalizer
import java.util.regex.Pattern

/**
 * Base centralizada de instituições financeiras padronizadas do sistema.
 * Fornece busca inteligente por nome e apelidos (case-insensitive, sem acentos),
 * associação automática com bancos antigos e suporte a bancos personalizados.
 */
object BankRegistry {

    private val DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")

    fun normalize(text: String): String {
        val nfd = Normalizer.normalize(text, Normalizer.Form.NFD)
        return DIACRITICS_PATTERN.matcher(nfd).replaceAll("").lowercase().trim()
    }

    private val STANDARD_BANKS = listOf(
        StandardBank("nubank", "Nubank", "Nu Pagamentos S.A. - Instituição de Pagamento", "260", "nubank", "#8A05BE", logoResId = R.drawable.ic_bank_nubank),
        StandardBank("itau", "Itaú", "Itaú Unibanco S.A.", "341", "itau", "#EC7000", logoResId = R.drawable.ic_bank_itau),
        StandardBank("bancodobrasil", "Banco do Brasil", "Banco do Brasil S.A.", "001", "bancodobrasil", "#0038A8", logoResId = R.drawable.ic_bank_bancodobrasil),
        StandardBank("bradesco", "Bradesco", "Banco Bradesco S.A.", "237", "bradesco", "#CC092F", logoResId = R.drawable.ic_bank_bradesco),
        StandardBank("caixa", "Caixa Econômica Federal", "Caixa Econômica Federal", "104", "caixa", "#005CA9", logoResId = R.drawable.ic_bank_caixa),
        StandardBank("santander", "Santander", "Banco Santander (Brasil) S.A.", "033", "santander", "#EC0000", logoResId = R.drawable.ic_bank_santander),
        StandardBank("inter", "Banco Inter", "Banco Inter S.A.", "077", "inter", "#FF7A00", logoResId = R.drawable.ic_bank_inter),
        StandardBank("c6bank", "C6 Bank", "Banco C6 S.A.", "336", "c6bank", "#1B1B1B", logoResId = R.drawable.ic_bank_c6bank),
        StandardBank("btgpactual", "BTG Pactual", "Banco BTG Pactual S.A.", "208", "btgpactual", "#001E62", logoResId = R.drawable.ic_bank_btgpactual),
        StandardBank("xp", "XP Investimentos", "Banco XP S.A.", "348", "xp", "#000000", logoResId = R.drawable.ic_bank_xp),
        StandardBank("mercadopago", "Mercado Pago", "Mercado Pago IP Ltda.", "323", "mercadopago", "#00A335", logoResId = R.drawable.ic_bank_mercadopago),
        StandardBank("picpay", "PicPay", "PicPay Instituição de Pagamento S.A.", "380", "picpay", "#11C76F", logoResId = R.drawable.ic_bank_picpay),
        StandardBank("pagbank", "PagBank", "PagBank - PagSeguro Internet S.A.", "290", "pagbank", "#00B140", logoResId = R.drawable.ic_bank_pagbank),
        StandardBank("neon", "Neon", "Neon Pagamentos S.A.", "536", "neon", "#00E5FF", logoResId = R.drawable.ic_bank_neon),
        StandardBank("sofisa", "Sofisa Direto", "Banco Sofisa S.A.", "637", "sofisa", "#E60000", logoResId = R.drawable.ic_bank_sofisa),
        StandardBank("next", "Next", "Banco Next", "237", "next", "#00E676", logoResId = R.drawable.ic_bank_next),
        StandardBank("sicoob", "Sicoob", "Banco Cooperativo Sicoob S.A.", "756", "sicoob", "#003641", logoResId = R.drawable.ic_bank_sicoob),
        StandardBank("sicredi", "Sicredi", "Banco Cooperativo Sicredi S.A.", "748", "sicredi", "#00823B", logoResId = R.drawable.ic_bank_sicredi),
        StandardBank("bancopan", "Banco Pan", "Banco Pan S.A.", "623", "bancopan", "#0072C6", logoResId = R.drawable.ic_bank_bancopan),
        StandardBank("bv", "BV Financeira", "Banco Votorantim S.A.", "655", "bv", "#002E73", logoResId = R.drawable.ic_bank_bv),
        StandardBank("bmg", "Banco BMG", "Banco BMG S.A.", "318", "bmg", "#FF5722", logoResId = R.drawable.ic_bank_bmg),
        StandardBank("agibank", "Agibank", "Banco Agibank S.A.", "121", "agibank", "#266BFF", logoResId = R.drawable.ic_bank_agibank),
        StandardBank("daycoval", "Banco Daycoval", "Banco Daycoval S.A.", "707", "daycoval", "#002B49", logoResId = R.drawable.ic_bank_daycoval),
        StandardBank("safra", "Banco Safra", "Banco Safra S.A.", "422", "safra", "#1D252D", logoResId = R.drawable.ic_bank_safra),
        StandardBank("nomad", "Nomad", "Nomad Fintech Inc.", null, "nomad", "#FFDD00", logoResId = R.drawable.ic_bank_nomad),
        StandardBank("wise", "Wise", "Wise Brasil Corretora de Câmbio Ltda.", null, "wise", "#9FE870", logoResId = R.drawable.ic_bank_wise),
        StandardBank("willbank", "Will Bank", "Will Bank S.A.", "280", "willbank", "#FFEC00", logoResId = R.drawable.ic_bank_willbank)
    )

    fun getAllBanks(): List<StandardBank> = STANDARD_BANKS

    fun getBankById(id: String?): StandardBank? {
        if (id.isNullOrBlank()) return null
        return STANDARD_BANKS.find { it.id == id || it.slug == id }
    }

    /**
     * Pesquisa bancos exclusivamente por nome, slug ou apelidos, ignorando maiúsculas e acentos.
     */
    fun searchBanks(query: String): List<StandardBank> {
        val qNorm = normalize(query)
        if (qNorm.isBlank()) return STANDARD_BANKS

        return STANDARD_BANKS.filter { bank ->
            val normDisplay = normalize(bank.displayName)
            val normOfficial = normalize(bank.officialName)
            val normSlug = normalize(bank.slug)

            normDisplay.contains(qNorm) ||
                normOfficial.contains(qNorm) ||
                normSlug.contains(qNorm) ||
                // Aliases comuns
                (qNorm == "bb" && bank.id == "bancodobrasil") ||
                (qNorm == "mp" && bank.id == "mercadopago") ||
                (qNorm == "nu" && bank.id == "nubank") ||
                (qNorm == "pan" && bank.id == "bancopan") ||
                (qNorm == "c6" && bank.id == "c6bank") ||
                (qNorm == "btg" && bank.id == "btgpactual") ||
                (qNorm == "will" && bank.id == "willbank")
        }
    }

    /**
     * Tenta identificar o banco pelo nome cadastrado no cartão para retrocompatibilidade.
     * Se não encontrar na base oficial, retorna um StandardBank customizado com as cores existentes.
     */
    fun getBankForCard(cardName: String?, bankId: String? = null, fallbackColorHex: String? = null): StandardBank {
        // Se já tiver bankId válido, retorna direto
        getBankById(bankId)?.let { return it }

        val safeName = cardName?.trim() ?: ""
        val norm = normalize(safeName)
        val matched = STANDARD_BANKS.find { bank ->
            val normDisplay = normalize(bank.displayName)
            val normSlug = normalize(bank.slug)
            norm == normDisplay || norm == normSlug ||
                (norm.contains("nu") && bank.id == "nubank") ||
                (norm.contains("itau") && bank.id == "itau") ||
                (norm.contains("bradesco") && bank.id == "bradesco") ||
                (norm.contains("santander") && bank.id == "santander") ||
                (norm.contains("caixa") && bank.id == "caixa") ||
                ((norm.contains("brasil") || norm.contains(" bb")) && bank.id == "bancodobrasil") ||
                (norm.contains("inter") && bank.id == "inter") ||
                (norm.contains("c6") && bank.id == "c6bank") ||
                (norm.contains("btg") && bank.id == "btgpactual") ||
                (norm.contains("xp") && bank.id == "xp") ||
                (norm.contains("mercado") && bank.id == "mercadopago") ||
                (norm.contains("picpay") && bank.id == "picpay") ||
                (norm.contains("pagbank") && bank.id == "pagbank") ||
                (norm.contains("neon") && bank.id == "neon") ||
                (norm.contains("sofisa") && bank.id == "sofisa") ||
                (norm.contains("next") && bank.id == "next") ||
                (norm.contains("sicoob") && bank.id == "sicoob") ||
                (norm.contains("sicredi") && bank.id == "sicredi") ||
                (norm.contains("pan") && bank.id == "bancopan") ||
                (norm.contains("bv") && bank.id == "bv") ||
                (norm.contains("bmg") && bank.id == "bmg") ||
                (norm.contains("agibank") && bank.id == "agibank") ||
                (norm.contains("daycoval") && bank.id == "daycoval") ||
                (norm.contains("safra") && bank.id == "safra") ||
                (norm.contains("nomad") && bank.id == "nomad") ||
                (norm.contains("wise") && bank.id == "wise") ||
                (norm.contains("will") && bank.id == "willbank")
        }

        if (matched != null) return matched

        // Custom Bank
        return StandardBank(
            id = "custom_${norm.replace(" ", "_")}",
            displayName = safeName.ifBlank { "Personalizado" },
            officialName = safeName.ifBlank { "Instituição Personalizada" },
            compe = null,
            slug = "custom",
            colorHex = fallbackColorHex ?: "#2563EB",
            isCustom = true
        )
    }

    /**
     * Cria uma instituição customizada para "Não encontrou seu banco? Adicionar manualmente".
     */
    fun createCustomBank(name: String, colorHex: String): StandardBank {
        return StandardBank(
            id = "custom_${System.currentTimeMillis()}",
            displayName = name.trim(),
            officialName = name.trim(),
            compe = null,
            slug = "custom",
            colorHex = colorHex,
            isCustom = true
        )
    }
}
