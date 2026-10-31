package com.dnfapps.arrmatey.mediaserver.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlexMediaListing(
    @SerialName("size") val size: Int = 0,
    @SerialName("totalSize") val totalSize: Int? = null,
    @SerialName("offset") val offset: Int? = null,
    @SerialName("Metadata") val metadata: List<PlexMetadataItem>? = null,
)
