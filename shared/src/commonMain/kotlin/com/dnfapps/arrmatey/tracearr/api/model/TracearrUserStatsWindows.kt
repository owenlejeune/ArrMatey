package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TracearrUserStatsWindows(
    @SerialName("all_time") val allTime: TracearrUserStatsWindow? = null,
    @SerialName("last_30") val last30: TracearrUserStatsWindow? = null,
    @SerialName("last_7") val last7: TracearrUserStatsWindow? = null,
)
