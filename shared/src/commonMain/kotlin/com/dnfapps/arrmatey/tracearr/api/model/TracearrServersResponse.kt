package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TracearrServersResponse(
    val data: List<TracearrServer> = emptyList(),
    @SerialName("tracearr_version") val tracearrVersion: String? = null,
)
