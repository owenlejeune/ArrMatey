package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class BookshelfRatings(
    val votes: Int,
    val value: Float,
    val popularity: Float
): ArrRatings