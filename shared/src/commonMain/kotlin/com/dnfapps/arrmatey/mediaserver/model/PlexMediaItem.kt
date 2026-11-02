package com.dnfapps.arrmatey.mediaserver.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlexMediaItem(
    @SerialName("id") val id: Long? = null,
    @SerialName("videoResolution") val videoResolution: String? = null,
    @SerialName("videoCodec") val videoCodec: String? = null,
    @SerialName("audioCodec") val audioCodec: String? = null,
    @SerialName("audioChannels") val audioChannels: Int? = null,
    @SerialName("bitrate") val bitrate: Long? = null,
    @SerialName("container") val container: String? = null,
    @SerialName("videoProfile") val videoProfile: String? = null,
    @SerialName("Part") val part: List<PlexPart>? = null,
)
