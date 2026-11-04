package com.dnfapps.arrmatey.mediaserver.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlexServerResource(
    @SerialName("name") val name: String,
    @SerialName("product") val product: String = "",
    @SerialName("productVersion") val productVersion: String? = null,
    @SerialName("platform") val platform: String? = null,
    @SerialName("device") val device: String? = null,
    @SerialName("clientIdentifier") val clientIdentifier: String,
    @SerialName("provides") val provides: String = "",
    @SerialName("connections") val connections: List<PlexServerConnection> = emptyList(),
)
