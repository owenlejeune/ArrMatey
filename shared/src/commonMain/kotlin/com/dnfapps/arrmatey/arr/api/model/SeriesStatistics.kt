package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class SeriesStatistics(
    val seasonCount: Int,
    val episodeFileCount: Int,
    val episodeCount: Int,
    val totalEpisodeCount: Int,
    val percentOfEpisodes: Double,
    override val sizeOnDisk: Long,
    override val releaseGroups: List<String> = emptyList(),
) : ArrStatistics
