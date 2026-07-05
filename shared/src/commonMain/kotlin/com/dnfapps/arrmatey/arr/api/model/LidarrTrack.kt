package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class LidarrTrack(
    val id: Long,
    val title: String,
    val duration: Int,
    val trackNumber: String? = null,
    val absoluteTrackNumber: Int = 0,
    val explicit: Boolean = false,
    val hasFile: Boolean = false,
    val trackFileId: Long? = null,
    val trackFile: LidarrTrackFile? = null,
    val artistId: Long,
    val albumId: Long
)