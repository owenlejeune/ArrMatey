package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ContentRatings(
    val results: List<ContentRating> = emptyList()
)