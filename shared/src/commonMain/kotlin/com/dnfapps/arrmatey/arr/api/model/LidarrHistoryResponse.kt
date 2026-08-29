package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class LidarrHistoryResponse(
    val page: Int,
    val pageSize: Int,
    val totalRecords: Int,
    val records: List<LidarrHistoryItem>,
)
