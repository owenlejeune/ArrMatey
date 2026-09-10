package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TracearrActivityByDoW(
    val day: Int = 0,
    val name: String? = null,
    val count: Int = 0
)
