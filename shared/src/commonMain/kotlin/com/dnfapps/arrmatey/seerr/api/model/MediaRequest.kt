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
    val canRemove: Boolean = false,
) {
    val isMovie: Boolean get() = type == RequestType.Movie
    val isTv: Boolean get() = type == RequestType.Tv

    fun matchesFilter(state: RequestState): Boolean =
        when (state) {
            RequestState.All -> true
            RequestState.Pending -> RequestStatus.fromValue(status) == RequestStatus.Pending
            RequestState.Approved -> RequestStatus.fromValue(status) == RequestStatus.Approved
            RequestState.Processing -> MediaStatus.fromValue(media.status) == MediaStatus.Processing
            RequestState.Available ->
                MediaStatus.fromValue(media.status) == MediaStatus.Available ||
                    MediaStatus.fromValue(media.status) == MediaStatus.PartiallyAvailable
            RequestState.Unavailable -> MediaStatus.fromValue(media.status) == MediaStatus.Unknown
            RequestState.Failed ->
                RequestStatus.fromValue(status) == RequestStatus.Failed ||
                    RequestStatus.fromValue(status) == RequestStatus.Declined
            RequestState.Deleted -> MediaStatus.fromValue(media.status) == MediaStatus.Deleted
            RequestState.Completed -> MediaStatus.fromValue(media.status) == MediaStatus.Available
        }
}
