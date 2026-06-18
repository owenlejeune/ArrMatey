package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class BazarrSubtitle(
    val name: String,
    val code2: String,
    val code3: String,
    val path: String? = null,
    val forced: Boolean,
    val hi: Boolean,
    val file_size: Long? = null
)
