package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TracearrCursorMeta(
    val nextCursor: String? = null,
    val pageSize: Int = 25,
)
