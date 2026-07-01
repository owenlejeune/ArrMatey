package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class AlbumMedium(
    val mediumNumber: Int,
    val mediumName: String? = null,
    val mediumFormat: String? = null
)
