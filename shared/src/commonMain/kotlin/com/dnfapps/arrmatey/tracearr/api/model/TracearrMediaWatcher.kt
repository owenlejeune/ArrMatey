package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TracearrMediaWatcher(
    val user: TracearrMediaWatcherUser? = null,
    val plays: Long = 0,
    @SerialName("watch_time_ms") val watchTimeMs: Long = 0,
    @SerialName("completion_pct") val completionPct: Int? = null,
    @SerialName("last_watched_day") val lastWatchedDay: String? = null,
    @SerialName("distinct_episodes_watched") val distinctEpisodesWatched: Int? = null,
)
