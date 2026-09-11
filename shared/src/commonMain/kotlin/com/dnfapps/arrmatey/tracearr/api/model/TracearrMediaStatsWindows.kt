package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TracearrMediaStatsWindows(
    @SerialName("all_time") val allTime: TracearrMediaStatsWindow? = null,
    @SerialName("last_30") val last30: TracearrMediaStatsWindow? = null,
    @SerialName("last_7") val last7: TracearrMediaStatsWindow? = null,
)
