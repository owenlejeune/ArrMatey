package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class MovieCollection(
    val title: String? = null,
    val tmdbId: Int? = null,
)
