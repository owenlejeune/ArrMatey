package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class AlbumStatistics(
    val trackFileCount: Int,
    val trackCount: Int,
    val totalTrackCount: Int,
    val sizeOnDisk: Long,
    val percentOfTracks: Double,
)
