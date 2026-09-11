package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TracearrMediaStatsCombined(
    val plays: Long = 0,
    @SerialName("watch_time_ms") val watchTimeMs: Long = 0,
    @SerialName("unique_users") val uniqueUsers: Int = 0,
)
