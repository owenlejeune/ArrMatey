package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TracearrPaginationMeta(
    val total: Int = 0,
    val page: Int = 1,
    val pageSize: Int = 25,
)
