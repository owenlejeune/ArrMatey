package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class LidarrStatistics(
    override val sizeOnDisk: Long,
    val albumCount: Int = 0,
    val trackFileCount: Int = 0,
    val trackCount: Int = 0,
    val totalTrackCount: Int = 0,
    val percentOfTracks: Float = 0f,
    override val releaseGroups: List<String> = emptyList(),
) : ArrStatistics
