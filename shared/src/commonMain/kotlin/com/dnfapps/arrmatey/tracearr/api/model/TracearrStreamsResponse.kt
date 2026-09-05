package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TracearrStreamsResponse(
    val data: List<TracearrStreamSession> = emptyList(),
)
