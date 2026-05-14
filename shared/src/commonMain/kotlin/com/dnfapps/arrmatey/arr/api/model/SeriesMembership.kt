package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
class SeriesMembership(
    val seriesName: String,
    val seriesNumber: String,
    val isPrimary: Boolean,
    val sortOrder: Int
)