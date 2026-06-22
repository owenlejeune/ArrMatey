package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class BazarrMissingSubtitle(
    val name: String,
    val code2: String,
    val code3: String,
    val forced: Boolean,
    val hi: Boolean
)
