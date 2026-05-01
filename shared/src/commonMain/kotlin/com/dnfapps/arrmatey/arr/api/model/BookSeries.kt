package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class BookSeries(
    val id: Long,
    val title: String? = null,
    val description: String? = null,
    val links: List<BookSeriesLink> = emptyList()
)
