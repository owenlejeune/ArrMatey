package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class SeriesMonitoringBody(
    val series: List<Long>,
    val monitoringOptions: SeriesMonitorOption
)

@Serializable
data class SeriesMonitorOption(
    val monitor: SeriesMonitorType
)

@Serializable
data class ArtistMonitoringBody(
    val artist: List<Long>,
    val monitoringOptions: ArtistMonitoringOption
)

@Serializable
data class ArtistMonitoringOption(
    val monitoringBody: ArtistMonitorType
)

@Serializable
data class AuthorMonitoringBody(
    val author: List<Long>,
    val monitoringOptions: AuthorMonitoringOption
)

@Serializable
data class AuthorMonitoringOption(
    val monitor: AuthorMonitorType
)