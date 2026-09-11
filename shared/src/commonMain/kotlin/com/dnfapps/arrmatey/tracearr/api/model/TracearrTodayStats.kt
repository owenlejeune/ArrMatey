package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class TracearrTodayStats(
    val activeStreams: Int = 0,
    val todayPlays: Int = 0,
    val todaySessions: Int = 0,
    val watchTimeHours: Float = 0f,
    val alertsLast24h: Int = 0,
    val activeUsersToday: Int = 0,
    @Contextual val timestamp: Instant? = null,
) {
    val formattedWatchTime: String
        get() {
            val hours = watchTimeHours
            val rounded = (hours * 10.0).toLong() / 10.0
            return if (rounded % 1.0 == 0.0) {
                "${rounded.toLong()}h"
            } else {
                "${rounded}h"
            }
        }
}
