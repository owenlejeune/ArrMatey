package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Season(
    val seasonNumber: Int,
    val monitored: Boolean,
    val statistics: SeasonStatistics? = null,
    val images: List<ArrImage> = emptyList(),
)
