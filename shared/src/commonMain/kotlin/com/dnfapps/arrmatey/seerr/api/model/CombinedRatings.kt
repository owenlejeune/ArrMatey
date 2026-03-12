package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class CombinedRatings(
    val rt: RottenTomatoesRating?,
    val imdb: ImdbRating?
)