package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class SeasonStatistics(
    val episodeFileCount: Int,
    val episodeCount: Int,
    val totalEpisodeCount: Int,
    val sizeOnDisk: Long,
    val percentOfEpisodes: Double,
    val nextAiring: String? = null,
    val previousAiring: String? = null,
    val releaseGroups: List<String> = emptyList(),
)
