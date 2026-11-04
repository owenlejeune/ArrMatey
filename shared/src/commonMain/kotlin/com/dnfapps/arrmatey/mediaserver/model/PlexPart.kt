package com.dnfapps.arrmatey.mediaserver.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlexPart(
    @SerialName("id") val id: Long? = null,
    @SerialName("key") val key: String? = null,
    @SerialName("duration") val duration: Long? = null,
    @SerialName("file") val file: String? = null,
    @SerialName("size") val size: Long? = null,
    @SerialName("container") val container: String? = null,
)
