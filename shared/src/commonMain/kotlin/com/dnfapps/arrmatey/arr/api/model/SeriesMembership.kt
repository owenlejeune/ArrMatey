package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class SeriesMembership(
    val id: Long? = null,
    val audiobookId: Long? = null,
    val seriesName: String,
    val seriesNumber: String,
    val seriesAsin: String? = null,
    val isPrimary: Boolean,
    val sortOrder: Int
)