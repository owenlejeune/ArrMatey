package com.dnfapps.arrmatey.discover.model

object DiscoverLanguages {
    val allLanguages = listOf(
        "en" to "English",
        "ja" to "Japanese",
        "es" to "Spanish",
        "fr" to "French",
        "de" to "German",
        "it" to "Italian",
        "ko" to "Korean",
        "zh" to "Chinese",
        "cn" to "Cantonese",
        "pt" to "Portuguese",
        "ru" to "Russian",
        "hi" to "Hindi",
        "sv" to "Swedish",
        "no" to "Norwegian",
        "da" to "Danish",
        "nl" to "Dutch",
        "pl" to "Polish",
        "tr" to "Turkish",
        "ar" to "Arabic",
        "fi" to "Finnish",
        "th" to "Thai",
        "id" to "Indonesian",
        "cs" to "Czech",
        "hu" to "Hungarian",
        "el" to "Greek",
        "he" to "Hebrew",
        "uk" to "Ukrainian",
        "vi" to "Vietnamese",
    )

    private val languageMap = allLanguages.toMap()

    fun getLanguageName(code: String): String {
        val lower = code.lowercase()
        return languageMap[lower] ?: code.uppercase()
    }
}
