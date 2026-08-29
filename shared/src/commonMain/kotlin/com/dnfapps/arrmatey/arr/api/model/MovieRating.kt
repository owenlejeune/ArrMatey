package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class MovieRating(
    val votes: Int,
    val value: Double,
    val type: String,
)
