package com.example.data.subscription

import java.text.Normalizer
import java.util.regex.Pattern

object SubscriptionRegistry {
    private val DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")

    fun normalize(text: String): String {
        val nfd = Normalizer.normalize(text, Normalizer.Form.NFD)
        return DIACRITICS_PATTERN.matcher(nfd).replaceAll("").lowercase().trim()
    }

    // Catálogo nacional abrangente com domínios para ícones em alta resolução e cores oficiais Simple Icons
    private val POPULAR_SUBSCRIPTIONS = listOf(
        // Streaming & Música
        StandardSubscription("spotify", "Spotify", "#1DB954", "Música", listOf("spotify", "spoti"), "spotify.com"),
        StandardSubscription("netflix", "Netflix", "#E50914", "Streaming", listOf("netflix", "net flix"), "netflix.com"),
        StandardSubscription("primevideo", "Amazon Prime", "#00A8E1", "Streaming", listOf("prime", "amazon", "prime video"), "primevideo.com"),
        StandardSubscription("disneyplus", "Disney+", "#113CCF", "Streaming", listOf("disney", "disney+"), "disneyplus.com"),
        StandardSubscription("max", "Max (HBO)", "#002BE7", "Streaming", listOf("max", "hbo", "hbo max"), "max.com"),
        StandardSubscription("youtube", "YouTube Premium", "#FF0000", "Vídeo", listOf("youtube", "yt"), "youtube.com"),
        StandardSubscription("globoplay", "Globoplay", "#FB0037", "Streaming", listOf("globoplay", "globo"), "globoplay.globo.com"),
        StandardSubscription("paramount", "Paramount+", "#0064FF", "Streaming", listOf("paramount", "paramount+"), "paramountplus.com"),
        StandardSubscription("crunchyroll", "Crunchyroll", "#F47521", "Streaming", listOf("crunchyroll", "anime"), "crunchyroll.com"),
        StandardSubscription("appletv", "Apple TV+", "#000000", "Streaming", listOf("apple tv", "appletv"), "tv.apple.com"),
        StandardSubscription("applemusic", "Apple Music", "#FA243C", "Música", listOf("apple music", "itunes"), "music.apple.com"),
        StandardSubscription("deezer", "Deezer", "#A238FF", "Música", listOf("deezer"), "deezer.com"),
        StandardSubscription("tidal", "Tidal", "#000000", "Música", listOf("tidal"), "tidal.com"),

        // Produtividade, Nuvem e IA
        StandardSubscription("chatgpt", "ChatGPT Plus", "#10A37F", "Inteligência Artificial", listOf("chatgpt", "openai", "gpt"), "openai.com"),
        StandardSubscription("googleone", "Google One", "#4285F4", "Armazenamento", listOf("google one", "google drive", "drive"), "one.google.com"),
        StandardSubscription("icloud", "iCloud / Apple One", "#555555", "Armazenamento", listOf("icloud", "apple one", "apple"), "apple.com"),
        StandardSubscription("microsoft365", "Microsoft 365", "#D83B01", "Produtividade", listOf("office", "microsoft", "365", "word"), "microsoft.com"),
        StandardSubscription("canva", "Canva Pro", "#00C4CC", "Design", listOf("canva"), "canva.com"),
        StandardSubscription("github", "GitHub Pro", "#181717", "Desenvolvimento", listOf("github"), "github.com"),
        StandardSubscription("adobe", "Adobe Creative Cloud", "#FF0000", "Design", listOf("adobe", "photoshop", "illustrator"), "adobe.com"),
        StandardSubscription("notion", "Notion Plus", "#000000", "Produtividade", listOf("notion"), "notion.so"),

        // Jogos
        StandardSubscription("playstation", "PlayStation Plus", "#003791", "Jogos", listOf("ps plus", "playstation", "psn"), "playstation.com"),
        StandardSubscription("xbox", "Xbox Game Pass", "#107C10", "Jogos", listOf("xbox", "game pass", "gamepass"), "xbox.com"),
        StandardSubscription("nintendo", "Nintendo Switch Online", "#E60012", "Jogos", listOf("nintendo", "switch"), "nintendo.com"),
        StandardSubscription("geforcenow", "GeForce NOW", "#76B900", "Jogos", listOf("geforce", "nvidia"), "nvidia.com"),

        // Benefícios, Delivery e Serviços
        StandardSubscription("ifood", "Clube iFood", "#EA1D2C", "Alimentação", listOf("ifood", "clube ifood"), "ifood.com.br"),
        StandardSubscription("meliplus", "Meli+ (Mercado Livre)", "#FFE600", "Benefícios", listOf("meli", "meli+", "mercado livre"), "mercadolivre.com.br"),
        StandardSubscription("rappi", "Rappi Prime", "#FF441F", "Delivery", listOf("rappi", "rappi prime"), "rappi.com.br"),
        StandardSubscription("uberone", "Uber One", "#000000", "Transporte", listOf("uber", "uber one"), "uber.com"),

        // Educação & Telecom
        StandardSubscription("duolingo", "Duolingo Super", "#58CC02", "Educação", listOf("duolingo", "duo"), "duolingo.com"),
        StandardSubscription("alura", "Alura", "#08122A", "Educação", listOf("alura"), "alura.com.br"),
        StandardSubscription("cambly", "Cambly", "#FFA000", "Educação", listOf("cambly"), "cambly.com"),
        StandardSubscription("brisanet", "Brisanet", "#FF7A00", "Internet", listOf("brisanet", "brisa"), "brisanet.com.br"),
        StandardSubscription("claro", "Claro", "#DA291C", "Telecom", listOf("claro"), "claro.com.br"),
        StandardSubscription("vivo", "Vivo", "#660099", "Telecom", listOf("vivo"), "vivo.com.br"),
        StandardSubscription("tim", "TIM", "#002F6C", "Telecom", listOf("tim"), "tim.com.br")
    )

    fun getAllSubscriptions(): List<StandardSubscription> = POPULAR_SUBSCRIPTIONS

    fun getSubscriptionById(id: String?): StandardSubscription? {
        if (id.isNullOrBlank()) return null
        return POPULAR_SUBSCRIPTIONS.find { it.id == id }
    }

    fun searchSubscriptions(query: String): List<StandardSubscription> {
        val qNorm = normalize(query)
        if (qNorm.isBlank()) return POPULAR_SUBSCRIPTIONS
        return POPULAR_SUBSCRIPTIONS.filter { sub ->
            val normDisplay = normalize(sub.displayName)
            val normCat = normalize(sub.category)
            normDisplay.contains(qNorm) ||
                normCat.contains(qNorm) ||
                sub.aliases.any { normalize(it).contains(qNorm) }
        }
    }

    fun getBrandForSubscription(name: String?): StandardSubscription {
        val safeName = name?.trim() ?: ""
        val norm = normalize(safeName)
        val matched = POPULAR_SUBSCRIPTIONS.find { sub ->
            val normDisplay = normalize(sub.displayName)
            norm.contains(normDisplay) || sub.aliases.any { norm.contains(normalize(it)) }
        }
        if (matched != null) return matched

        val colors = listOf("#2563EB", "#7C3AED", "#DB2777", "#059669", "#D97706", "#4B5563")
        val colorIndex = Math.abs(safeName.hashCode()) % colors.size
        return StandardSubscription(
            id = "custom_${norm.take(10)}",
            displayName = safeName.ifBlank { "Assinatura" },
            colorHex = colors[colorIndex],
            category = "Outros",
            isCustom = true
        )
    }
}
