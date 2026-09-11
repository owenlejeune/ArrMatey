package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TracearrMediaWatchers(
    @SerialName("media_id") val mediaId: String? = null,
    @SerialName("media_type") val mediaType: TracearrMediaType? = null,
    val window: String? = null,
    val watchers: List<TracearrMediaWatcher> = emptyList(),
)
