package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ReleaseInfo(
    val iso_3166_1: String,
    val rating: String? = null,
    val release_dates: List<ReleaseDate> = emptyList()
)