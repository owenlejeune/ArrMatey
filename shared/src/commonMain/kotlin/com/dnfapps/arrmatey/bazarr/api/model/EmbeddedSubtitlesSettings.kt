package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class EmbeddedSubtitlesSettings(
    val fallback_lang: String,
    val hi_fallback: Boolean,
    val included_codecs: List<String>,
    val timeout: Int,
    val unknown_as_fallback: Boolean
)
