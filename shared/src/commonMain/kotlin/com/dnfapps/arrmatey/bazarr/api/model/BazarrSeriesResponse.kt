package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class BazarrSeriesResponse(
    val data: List<BazarrSeries> = emptyList(),
    val total: Int = 0
)
