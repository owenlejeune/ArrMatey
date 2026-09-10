package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TracearrActivityRange(
    val start: String? = null,
    val end: String? = null
)
