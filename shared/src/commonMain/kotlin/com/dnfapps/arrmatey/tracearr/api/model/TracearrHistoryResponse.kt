package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TracearrHistoryResponse(
    val data: List<TracearrHistoryItem> = emptyList(),
    val meta: TracearrCursorMeta? = null,
)
