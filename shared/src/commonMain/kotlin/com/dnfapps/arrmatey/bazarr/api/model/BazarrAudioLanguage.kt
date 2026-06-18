package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class BazarrAudioLanguage(
    val name: String,
    val code2: String,
    val code3: String
)
