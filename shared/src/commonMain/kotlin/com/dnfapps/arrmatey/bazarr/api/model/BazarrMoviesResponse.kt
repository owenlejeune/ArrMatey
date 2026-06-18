package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class BazarrMoviesResponse(
    val data: List<BazarrMovie>,
    val total: Int
)
