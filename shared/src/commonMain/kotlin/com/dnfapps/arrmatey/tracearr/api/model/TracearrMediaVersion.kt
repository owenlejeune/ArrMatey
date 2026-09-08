package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TracearrMediaVersion(
    val resolution: String? = null,
    @SerialName("video_codec") val videoCodec: String? = null,
    @SerialName("audio_codec") val audioCodec: String? = null,
    @SerialName("dynamic_range") val dynamicRange: String? = null,
    val container: String? = null,
    @SerialName("file_size") val fileSize: Long? = null,
)
