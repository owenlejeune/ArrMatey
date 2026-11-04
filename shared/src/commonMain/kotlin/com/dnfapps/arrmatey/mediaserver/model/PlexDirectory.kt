package com.dnfapps.arrmatey.mediaserver.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlexDirectory(
    @SerialName("key") val key: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("type") val type: String = "",
)
