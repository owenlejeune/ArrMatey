package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TracearrActivityPlatform(
    val platform: String? = null,
    val count: Int = 0,
)
