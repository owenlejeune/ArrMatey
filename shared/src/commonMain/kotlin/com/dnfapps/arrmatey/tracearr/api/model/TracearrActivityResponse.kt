package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TracearrActivityResponse(
    val period: TracearrPeriod,
    val range: TracearrActivityRange,
    val plays: List<TracearrActivityPlay> = emptyList(),
    val concurrent: List<TracearrActivityConcurrent> = emptyList(),
    val byDayOfWeek: List<TracearrActivityByDoW> = emptyList(),
    val byHourOfDay: List<TracearrActivityByHoD> = emptyList(),
    val platforms: List<TracearrActivityPlatform> = emptyList(),
    val quality: TracearrActivityQuality,
)
