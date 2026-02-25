package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ContentRating(
    val iso_3166_1: String,
    val rating: String
)