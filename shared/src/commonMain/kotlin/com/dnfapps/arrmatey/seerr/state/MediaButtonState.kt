package com.dnfapps.arrmatey.seerr.state

import com.dnfapps.arrmatey.seerr.api.model.MediaInfo
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.api.model.SeerrUser
import com.dnfapps.arrmatey.seerr.api.model.UserPermission
import com.dnfapps.arrmatey.seerr.api.model.Video
import com.dnfapps.arrmatey.seerr.policy.SeerrPermissionPolicy
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class MediaButtonState(
    val showWatchButton: Boolean = false,
    val watchButtonUrl: String? = null,
    @Contextual val watchButtonLabel: StringResource = MR.strings.watch,
    val showWatchTrailerOption: Boolean = false,
    val trailerUrl: String? = null,
    val showRequestButton: Boolean = false,
    val showRequestMoreButton: Boolean = false,
    val showRequest4kButton: Boolean = false,
    val availableSeasons: List<Int> = emptyList(),
    val totalSeasons: Int = 0,
    val showViewRequestButton: Boolean = false,
    val showApproveRequestButton: Boolean = false,
    val showDeclineRequestButton: Boolean = false,
    val showCancelRequestButton: Boolean = false,
    val pendingRequestId: Long? = null,
    val showReportIssueButton: Boolean = false,
    val showManageMenu: Boolean = false,
    val showOpenInServiceButton: Boolean = false,
    val serviceUrl: String? = null,
    val serviceName: String? = null,
    val showRemoveFromServiceButton: Boolean = false,
    val showClearDataButton: Boolean = false,
    val showMarkAsAvailableButton: Boolean = false,
    val mediaProvider: MediaProvider = MediaProvider.None,
) {
    constructor() : this(false) // empty ios constructor
}

enum class MediaProvider {
    None,
    Plex,
    Jellyfin,
}

fun MediaInfo?.toButtonState(
    relatedVideos: List<Video>,
    totalSeasonCount: Int = 0, // NEW: Pass from TV details API
    currentUserId: Long? = null,
    isAdmin: Boolean = false,
    currentUser: SeerrUser? = null,
    mediaType: RequestType? = null,
): MediaButtonState {
    val resolvedType = mediaType ?: this?.mediaType
    val canRequest = currentUser == null || SeerrPermissionPolicy.canRequestMedia(currentUser, type = resolvedType, is4k = false)
    val canRequest4k = currentUser == null || SeerrPermissionPolicy.canRequestMedia(currentUser, type = resolvedType, is4k = true)

    if (this == null) {
        return MediaButtonState(
            showRequestButton = canRequest,
            showRequest4kButton = canRequest4k,
            showWatchTrailerOption = getTrailerUrl(relatedVideos) != null,
            trailerUrl = getTrailerUrl(relatedVideos),
        )
    }

    val isAvailable = status == 5 // AVAILABLE
    val isPartiallyAvailable = status == 4 // PARTIALLY_AVAILABLE
    val isPending = status == 2 // PENDING
    val isProcessing = status == 3 // PROCESSING
    val hasContent = isAvailable || isPartiallyAvailable
    val hasSeerrMedia = status != 1

    // For TV shows, check if we have all seasons or just some
    val isTvShow = resolvedType == RequestType.Tv
    val availableSeasonNumbers =
        seasons
            .filter { it.status == 5 || it.status == 4 }
            .map { it.seasonNumber }
    val hasAllSeasons =
        isTvShow &&
            totalSeasonCount > 0 &&
            availableSeasonNumbers.size >= totalSeasonCount
    val hasPartialContent = isTvShow && hasContent && !hasAllSeasons && totalSeasonCount > 0

    // Determine media provider and URLs
    val (mediaProvider, watchUrl, watchLabel) =
        when {
            !mediaUrl.isNullOrEmpty() || !iOSPlexUrl.isNullOrEmpty() -> {
                Triple(
                    MediaProvider.Plex,
                    iOSPlexUrl ?: mediaUrl,
                    MR.strings.watch_on_plex,
                )
            }
            !jellyfinMediaId.isNullOrEmpty() -> {
                Triple(
                    MediaProvider.Jellyfin,
                    buildJellyfinUrl(jellyfinMediaId, resolvedType),
                    MR.strings.watch_on_jellyfin,
                )
            }
            else -> Triple(MediaProvider.None, null, MR.strings.watch)
        }

    // Find pending request
    val pendingRequest = requests.firstOrNull { it.status == 1 }
    val userHasPendingRequest = pendingRequest?.requestedBy?.id == currentUserId

    val serviceName =
        when {
            serviceUrl?.contains("sonarr", ignoreCase = true) == true -> "Sonarr"
            serviceUrl?.contains("radarr", ignoreCase = true) == true -> "Radarr"
            serviceUrl?.contains("lidarr", ignoreCase = true) == true -> "Lidarr"
            resolvedType == RequestType.Movie -> "Radarr"
            resolvedType == RequestType.Tv -> "Sonarr"
            else -> null
        }

    val trailerUrl = getTrailerUrl(relatedVideos)

    val canManage = isAdmin || (currentUser != null && SeerrPermissionPolicy.canApproveOrDecline(currentUser))
    val canCancelOrDecline = pendingRequest != null && (isAdmin || (currentUser != null && SeerrPermissionPolicy.canCancelOrDeleteRequest(currentUser, pendingRequest)) || userHasPendingRequest)
    val canManageMedia = isAdmin || (currentUser != null && SeerrPermissionPolicy.canManageMedia(currentUser))
    val canCreateIssues = currentUser == null || isAdmin || SeerrPermissionPolicy.canCreateIssues(currentUser)

    return MediaButtonState(
        // Watch button
        showWatchButton = hasContent && watchUrl != null,
        watchButtonUrl = watchUrl,
        watchButtonLabel = watchLabel,
        showWatchTrailerOption = trailerUrl != null,
        trailerUrl = trailerUrl,
        // Request buttons
        showRequestButton = status == 1 && canRequest, // Only if standard not requested yet
        showRequestMoreButton = hasPartialContent && canRequest, // Show for partial TV content
        showRequest4kButton = (status4k == null || status4k == 1) && canRequest4k, // Only if 4k not requested yet
        availableSeasons = availableSeasonNumbers,
        totalSeasons = totalSeasonCount,
        // Request management
        showViewRequestButton = canManage && pendingRequest != null,
        showApproveRequestButton = canManage && pendingRequest != null,
        showDeclineRequestButton = canManage && pendingRequest != null,
        showCancelRequestButton = !canManage && canCancelOrDecline,
        pendingRequestId = pendingRequest?.id,
        // Report Issue
        showReportIssueButton = hasContent && canCreateIssues,
        // Manage menu
        showManageMenu = hasSeerrMedia && canManageMedia,
        showOpenInServiceButton = canManageMedia && !serviceUrl.isNullOrEmpty(),
        serviceUrl = serviceUrl,
        serviceName = serviceName,
        showRemoveFromServiceButton = hasSeerrMedia && canManageMedia,
        showClearDataButton = hasSeerrMedia && (isAdmin || currentUser?.hasPermission(UserPermission.ADMIN) == true),
        showMarkAsAvailableButton = hasSeerrMedia && !isAvailable && canManageMedia,
        mediaProvider = mediaProvider,
    )
}

private fun getTrailerUrl(relatedVideos: List<Video>): String? {
    val trailer =
        relatedVideos.firstOrNull {
            it.type.equals("Trailer", ignoreCase = true) ||
                it.type.equals("Teaser", ignoreCase = true)
        }
    return trailer?.url
}

private fun buildJellyfinUrl(
    jellyfinMediaId: String,
    mediaType: RequestType?,
): String = "jellyfin://media/${mediaType?.name?.lowercase() ?: "item"}/$jellyfinMediaId"
