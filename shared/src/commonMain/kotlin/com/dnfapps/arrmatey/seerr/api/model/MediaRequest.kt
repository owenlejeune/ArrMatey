package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class MediaRequest(
    val id: Long,
    val status: Int,
    @Contextual val createdAt: Instant,
    @Contextual val updatedAt: Instant,
    val type: RequestType,
    val is4k: Boolean,
    val serverId: Long? = null,
    val profileId: Long? = null,
    val rootFolder: String? = null,
    val languageProfileId: Long? = null,
    val tags: List<String>? = null,
    val isAutoRequest: Boolean,
    val media: RequestMedia,
    val seasons: List<RequestSeason> = emptyList(),
    val modifiedBy: RequestUser? = null,
    val requestedBy: RequestUser,
    val seasonCount: Int,
    val canRemove: Boolean = false
) {
    val isMovie: Boolean get() = type == RequestType.Movie
    val isTv: Boolean get() = type == RequestType.Tv
}