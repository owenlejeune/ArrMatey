package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class OpenSubtitlesComSettings(
    val include_ai_translated: Boolean,
    val include_machine_translated: Boolean,
    val password: String,
    val use_hash: Boolean,
    val username: String
)
