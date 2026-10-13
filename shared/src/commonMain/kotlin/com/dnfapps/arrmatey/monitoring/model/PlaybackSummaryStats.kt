package com.dnfapps.arrmatey.monitoring.model

data class PlaybackSummaryStats(
    val activeStreamsCount: Int = 0,
    val transcodeCount: Int = 0,
    val directPlayCount: Int = 0,
    val totalBandwidthKbps: Long = 0,
    val lanBandwidthKbps: Long = 0,
    val wanBandwidthKbps: Long = 0,
    val todayTotalPlays: Long = 0,
    val todayTotalDurationSeconds: Long = 0,
    val activeUsersCount: Int = 0,
)
