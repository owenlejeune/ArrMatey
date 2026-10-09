package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ComicFile(
    val id: Long? = null,
    val filepath: String? = null,
    val size: Long = 0,
)
