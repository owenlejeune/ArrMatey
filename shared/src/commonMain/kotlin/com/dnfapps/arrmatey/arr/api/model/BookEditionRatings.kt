package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class BookEditionRatings(
    val votes: Int,
    val value: Double,
    val popularity: Double
)