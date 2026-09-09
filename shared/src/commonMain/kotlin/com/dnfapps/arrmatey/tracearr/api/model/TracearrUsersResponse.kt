package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TracearrUsersResponse(
    val data: List<TracearrUserDetail> = emptyList(),
    val meta: TracearrCursorMeta? = null,
)
