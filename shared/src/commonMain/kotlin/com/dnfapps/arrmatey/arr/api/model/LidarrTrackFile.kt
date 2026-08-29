package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class LidarrTrackFile(
    val id: Long,
    val path: String? = null,
    val size: Long = 0,
    val quality: QualityInfo,
    val releaseGroup: String? = null,
    val sceneName: String? = null,
    val mediaInfo: MediaInfo? = null,
    val albumId: Long,
    val artistId: Long,
)
