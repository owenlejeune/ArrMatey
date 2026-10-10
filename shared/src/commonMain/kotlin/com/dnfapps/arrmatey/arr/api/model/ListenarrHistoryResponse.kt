package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ListenarrHistoryResponse(
    val history: List<ListenarrHistoryItem>,
    val total: Int = 0,
    val limit: Int = 50,
    val offset: Int = 0,
)
