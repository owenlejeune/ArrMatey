package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class KapowarrHistoryResponse(
    val error: String? = null,
    val result: List<KapowarrHistoryItem>? = emptyList(),
)
