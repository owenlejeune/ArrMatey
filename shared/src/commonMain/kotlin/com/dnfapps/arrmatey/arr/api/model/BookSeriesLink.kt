package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class BookSeriesLink(
    val id: Long,
    val position: String? = null,
    val seriesPosition: Int? = null,
    val seriesId: Long? = null,
    val bookId: Long? = null
)
