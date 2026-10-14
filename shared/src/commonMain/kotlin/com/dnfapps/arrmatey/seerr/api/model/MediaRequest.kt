package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class MediaRequest(
    val id: Long = 0L,
    val status: Int = 0,
    @Contextual val createdAt: Instant? = null,
    @Contextual val updatedAt: Instant? = null,
    val type: RequestType = RequestType.Movie,
    val is4k: Boolean = false,
    val serverId: Long? = null,
    val profileId: Long? = null,
    val rootFolder: String? = null,
    val languageProfileId: Long? = null,
    val tags: List<String>? = null,
    val isAutoRequest: Boolean = false,
    val media: RequestMedia,
    val seasons: List<RequestSeason> = emptyList(),
    val modifiedBy: RequestUser? = null,
    val requestedBy: RequestUser? = null,
    val seasonCount: Int = 0,
    val canRemove: Boolean = false,
) {
    val isMovie: Boolean get() = type == RequestType.Movie
    val isTv: Boolean get() = type == RequestType.Tv

    fun matchesFilter(state: RequestState): Boolean = when (state) {
        RequestState.All -> true
        RequestState.Pending -> RequestStatus.fromValue(status) == RequestStatus.Pending
        RequestState.Approved -> RequestStatus.fromValue(status) == RequestStatus.Approved
        RequestState.Processing -> SeerrMediaStatus.fromValue(media.status) == SeerrMediaStatus.Processing
        RequestState.Available ->
            SeerrMediaStatus.fromValue(media.status) == SeerrMediaStatus.Available ||
                SeerrMediaStatus.fromValue(media.status) == SeerrMediaStatus.PartiallyAvailable
        RequestState.Unavailable -> SeerrMediaStatus.fromValue(media.status) == SeerrMediaStatus.Unknown
        RequestState.Failed ->
            RequestStatus.fromValue(status) == RequestStatus.Failed ||
                RequestStatus.fromValue(status) == RequestStatus.Declined
        RequestState.Deleted -> SeerrMediaStatus.fromValue(media.status) == SeerrMediaStatus.Deleted
        RequestState.Completed -> SeerrMediaStatus.fromValue(media.status) == SeerrMediaStatus.Available
    }
}
