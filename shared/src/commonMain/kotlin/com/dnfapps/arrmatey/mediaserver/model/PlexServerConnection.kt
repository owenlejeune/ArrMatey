package com.dnfapps.arrmatey.mediaserver.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlexServerConnection(
    @SerialName("protocol") val protocol: String,
    @SerialName("address") val address: String,
    @SerialName("port") val port: Int,
    @SerialName("uri") val uri: String,
    @SerialName("local") val local: Boolean = false,
)
