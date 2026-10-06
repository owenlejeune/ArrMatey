package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class BookshelfRatings(
    val votes: Int = 0,
    val value: Float = 0f,
    val popularity: Float = 0f,
) : ArrRatings
