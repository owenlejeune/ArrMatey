package com.dnfapps.arrmatey.mediaserver.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlexDirectoryListing(
    @SerialName("size") val size: Int = 0,
    @SerialName("Directory") val directories: List<PlexDirectory>? = null,
)
