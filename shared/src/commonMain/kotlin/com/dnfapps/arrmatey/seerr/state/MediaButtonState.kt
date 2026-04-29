package com.dnfapps.arrmatey.seerr.state

import com.dnfapps.arrmatey.seerr.api.model.MediaInfo
import com.dnfapps.arrmatey.seerr.api.model.MediaStatus
import com.dnfapps.arrmatey.seerr.api.model.RequestStatus
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.api.model.Video
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
    val pendingRequestId: Long? = null,

    val showReportIssueButton: Boolean = false,

    val showManageMenu: Boolean = false,
    val showOpenInServiceButton: Boolean = false,
    val serviceUrl: String? = null,
    val serviceName: String? = null,
    val showClearDataButton: Boolean = false,

    val mediaProvider: MediaProvider = MediaProvider.None
) {
    constructor(): this(false) // empty ios constructor
}

enum class MediaProvider {
    None,
    Plex,
    Jellyfin
}

fun MediaInfo?.toButtonState(
    relatedVideos: List<Video>,
    totalSeasonCount: Int = 0,  // NEW: Pass from TV details API
    currentUserId: Long? = null,
    isAdmin: Boolean = false
): MediaButtonState {
    if (this == null) {
        return MediaButtonState(
            showRequestButton = true,
            showWatchTrailerOption = getTrailerUrl(relatedVideos) != null,
            trailerUrl = getTrailerUrl(relatedVideos)
        )
    }

    val isAvailable = status == 5  // AVAILABLE
    val isPartiallyAvailable = status == 4  // PARTIALLY_AVAILABLE
    val isPending = status == 2  // PENDING
    val isProcessing = status == 3  // PROCESSING
    val hasContent = isAvailable || isPartiallyAvailable

    // For TV shows, check if we have all seasons or just some
    val isTvShow = mediaType == RequestType.Tv
    val availableSeasonNumbers = seasons
        .filter { it.status == 5 || it.status == 4 }
        .map { it.seasonNumber }
    val hasAllSeasons = isTvShow && totalSeasonCount > 0 &&
            availableSeasonNumbers.size >= totalSeasonCount
    val hasPartialContent = isTvShow && hasContent && !hasAllSeasons && totalSeasonCount > 0

    // Determine media provider and URLs
    val (mediaProvider, watchUrl, watchLabel) = when {
        !mediaUrl.isNullOrEmpty() || !iOSPlexUrl.isNullOrEmpty() -> {
            Triple(
                MediaProvider.Plex,
                iOSPlexUrl ?: mediaUrl,
                MR.strings.watch_on_plex
            )
        }
        !jellyfinMediaId.isNullOrEmpty() -> {
            Triple(
                MediaProvider.Jellyfin,
                buildJellyfinUrl(jellyfinMediaId, mediaType),
                MR.strings.watch_on_jellyfin
            )
        }
        else -> Triple(MediaProvider.None, null, MR.strings.watch)
    }

    // Find pending request
    val pendingRequest = requests.firstOrNull { it.status == 1 }
    val userHasPendingRequest = pendingRequest?.requestedBy?.id == currentUserId

    val serviceName = when {
        serviceUrl?.contains("sonarr", ignoreCase = true) == true -> "Sonarr"
        serviceUrl?.contains("radarr", ignoreCase = true) == true -> "Radarr"
        serviceUrl?.contains("lidarr", ignoreCase = true) == true -> "Lidarr"
        else -> null
    }

    val trailerUrl = getTrailerUrl(relatedVideos)

    return MediaButtonState(
        // Watch button
        showWatchButton = hasContent && watchUrl != null,
        watchButtonUrl = watchUrl,
        watchButtonLabel = watchLabel,
        showWatchTrailerOption = trailerUrl != null,
        trailerUrl = trailerUrl,

        // Request buttons
        showRequestButton = status == 1,  // Only if nothing requested yet
        showRequestMoreButton = hasPartialContent,  // NEW: Show for partial TV content
        showRequest4kButton = hasContent && (status4k == null || status4k == 1),
        availableSeasons = availableSeasonNumbers,
        totalSeasons = totalSeasonCount,

        // Request management
        showViewRequestButton = pendingRequest != null,
        showApproveRequestButton = isAdmin && pendingRequest != null,
        showDeclineRequestButton = (isAdmin || userHasPendingRequest) && pendingRequest != null,
        pendingRequestId = pendingRequest?.id,

        // Report Issue
        showReportIssueButton = hasContent,

        // Manage menu
        showManageMenu = true,
        showOpenInServiceButton = !serviceUrl.isNullOrEmpty(),
        serviceUrl = serviceUrl,
        serviceName = serviceName,
        showClearDataButton = isAdmin,

        mediaProvider = mediaProvider
    )
}

private fun getTrailerUrl(relatedVideos: List<Video>): String? {
    val trailer = relatedVideos.firstOrNull {
        it.type.equals("Trailer", ignoreCase = true) ||
                it.type.equals("Teaser", ignoreCase = true)
    }
    return trailer?.url
}

private fun buildJellyfinUrl(jellyfinMediaId: String, mediaType: RequestType): String {
    return "jellyfin://media/${mediaType.name.lowercase()}/$jellyfinMediaId"
}