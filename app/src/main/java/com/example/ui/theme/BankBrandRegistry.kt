package com.example.ui.theme

import androidx.compose.ui.graphics.Color

data class BankBrand(
    val mainColor: Color,
    val onColor: Color = Color.White,
    val bankName: String
)

object BankBrandRegistry {

    private val Brands = listOf(
        BankBrand(Color(0xFF8A05BE), Color.White, "Nubank"),
        BankBrand(Color(0xFFFF7800), Color.White, "Itaú"),
        BankBrand(Color(0xFFFF7A00), Color.White, "Inter"),
        BankBrand(Color(0xFFEC0000), Color.White, "Santander"),
        BankBrand(Color(0xFFB20C15), Color.White, "Bradesco"),
        BankBrand(Color(0xFF0038A8), Color.White, "Banco do Brasil"),
        BankBrand(Color(0xFF005CA9), Color.White, "Caixa"),
        BankBrand(Color(0xFF00A335), Color.White, "Mercado Pago"),
        BankBrand(Color(0xFF00458C), Color.White, "XP Investimentos"),
        BankBrand(Color(0xFF2563EB), Color.White, "BTG Pactual")
    )

    fun getBrandForName(name: String, manualColorHex: String? = null): BankBrand {
        if (!manualColorHex.isNullOrBlank()) {
            return try {
                BankBrand(Color(android.graphics.Color.parseColor(manualColorHex)), Color.White, name)
            } catch (_: Exception) {
                getBrandForName(name, null)
            }
        }

        val normalized = name.lowercase().trim()
        
        // Mapeamentos específicos por palavras-chave
        if (normalized.contains("nu") || normalized.contains("roxo")) {
            return Brands.find { it.bankName == "Nubank" }!!
        }
        if (normalized.contains("itau") || normalized.contains("itaú")) {
            return Brands.find { it.bankName == "Itaú" }!!
        }
        if (normalized.contains("inter") || normalized.contains("orange")) {
            return Brands.find { it.bankName == "Inter" }!!
        }
        if (normalized.contains("bradesco")) {
            return Brands.find { it.bankName == "Bradesco" }!!
        }
        if (normalized.contains("santander")) {
            return Brands.find { it.bankName == "Santander" }!!
        }
        if (normalized.contains("mercado") || normalized.contains("mp")) {
            return Brands.find { it.bankName == "Mercado Pago" }!!
        }
        if (normalized.contains("caixa")) {
            return Brands.find { it.bankName == "Caixa" }!!
        }
        if (normalized.contains("brasil") || normalized.contains(" bb ")) {
            return Brands.find { it.bankName == "Banco do Brasil" }!!
        }
        if (normalized.contains("xp ")) {
            return Brands.find { it.bankName == "XP Investimentos" }!!
        }

        // Default: Cor do app
        return BankBrand(Color(0xFF2563EB), Color.White, "Geral")
    }
}
