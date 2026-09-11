package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TracearrMediaStatsWindow(
    val combined: TracearrMediaStatsCombined? = null,
    @SerialName("per_server") val perServer: List<TracearrMediaServerStats> = emptyList(),
)
