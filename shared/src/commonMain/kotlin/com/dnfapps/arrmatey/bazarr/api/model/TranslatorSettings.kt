package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TranslatorSettings(
    val default_score: Int,
    val gemini_key: String,
    val gemini_model: String,
    val lingarr_token: String,
    val lingarr_url: String,
    val translator_info: Boolean,
    val translator_type: String
)
