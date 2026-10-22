package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ArrUpdate(
    val version: String? = null,
    val branch: String? = null,
    val releaseDate: String? = null,
    val fileName: String? = null,
    val url: String? = null,
    val installed: Boolean = false,
    val latest: Boolean = false,
)
