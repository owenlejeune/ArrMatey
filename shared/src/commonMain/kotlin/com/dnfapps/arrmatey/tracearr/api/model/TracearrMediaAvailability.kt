package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class TracearrMediaAvailability(
    @SerialName("server_id") val serverId: String? = null,
    @SerialName("server_type") val serverType: TracearrServerType? = null,
    @SerialName("library_id") val libraryId: String? = null,
    @SerialName("rating_key") val ratingKey: String? = null,
    @SerialName("added_at") @Contextual val addedAt: Instant? = null,
    @SerialName("removed_at") @Contextual val removedAt: Instant? = null,
    @SerialName("video_resolution") val videoResolution: String? = null,
    @SerialName("file_size") val fileSize: Long? = null,
    val versions: List<TracearrMediaVersion> = emptyList(),
    val replaces: String? = null,
)
