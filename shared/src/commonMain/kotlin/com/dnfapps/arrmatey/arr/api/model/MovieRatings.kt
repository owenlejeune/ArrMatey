package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class MovieRatings(
    val imdb: MovieRating? = null,
    val tmdb: MovieRating? = null,
    val metacritic: MovieRating? = null,
    val rottenTomatoes: MovieRating? = null,
    val trakt: MovieRating? = null,
) : ArrRatings
