package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class RadarrHistoryResponse(
    val page: Int = 1,
    val pageSize: Int = 100,
    val totalRecords: Int = 0,
    val records: List<RadarrHistoryItem> = emptyList(),
)
