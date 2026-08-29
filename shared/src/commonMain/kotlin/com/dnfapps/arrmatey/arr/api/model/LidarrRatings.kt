package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class LidarrRatings(
    val votes: Int,
    val value: Float,
) : ArrRatings
