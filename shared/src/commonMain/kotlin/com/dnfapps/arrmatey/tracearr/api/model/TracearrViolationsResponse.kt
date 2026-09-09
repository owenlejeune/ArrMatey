package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TracearrViolationsResponse(
    val data: List<TracearrViolation> = emptyList(),
    val meta: TracearrPaginationMeta? = null,
)
