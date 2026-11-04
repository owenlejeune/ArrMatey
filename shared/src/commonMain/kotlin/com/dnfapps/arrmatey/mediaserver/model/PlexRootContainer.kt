package com.dnfapps.arrmatey.mediaserver.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlexRootContainer<T>(
    @SerialName("MediaContainer") val mediaContainer: T,
)
