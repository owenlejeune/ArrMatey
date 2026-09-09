package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TracearrUserStats(
    @SerialName("user_id") val userId: String? = null,
    val windows: TracearrUserStatsWindows? = null,
    @SerialName("top_genres") val topGenres: List<TracearrGenreStat> = emptyList(),
)
