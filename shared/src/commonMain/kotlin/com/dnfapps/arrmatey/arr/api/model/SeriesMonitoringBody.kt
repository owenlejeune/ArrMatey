package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class SeriesMonitoringBody(
    val series: List<IdWrapper>,
    val monitoringOptions: SeriesMonitorOption
)

@Serializable
data class SeriesMonitorOption(
    val monitor: SeriesMonitorType
)

@Serializable
data class ArtistMonitoringBody(
    val artist: List<IdWrapper>,
    val monitoringOptions: ArtistMonitoringOption
)

@Serializable
data class ArtistMonitoringOption(
    val monitoringBody: ArtistMonitorType
)

@Serializable
data class IdWrapper(val id: Long)