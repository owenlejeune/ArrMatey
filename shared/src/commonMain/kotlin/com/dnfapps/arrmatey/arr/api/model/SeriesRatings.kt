package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class SeriesRatings(
    val votes: Int,
    val value: Double,
) : ArrRatings
