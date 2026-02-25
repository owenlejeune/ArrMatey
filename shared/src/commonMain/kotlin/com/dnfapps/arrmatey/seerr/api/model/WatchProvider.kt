package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class WatchProvider(
    val iso_3166_1: String,
    val link: String,
    val buy: List<Provider> = emptyList(),
    val flatrate: List<Provider> = emptyList()
)