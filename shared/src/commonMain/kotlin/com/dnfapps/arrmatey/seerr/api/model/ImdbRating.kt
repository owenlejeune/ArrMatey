package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ImdbRating(
    val title: String,
    val url: String,
    val criticsScore: Float,
    val criticsScoreCount: Int
)
