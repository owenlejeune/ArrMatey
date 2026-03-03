package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class MediaInfo(
    val id: Long,
    val tmdbId: Long,
    val tvdbId: Long? = null,
    val status: Int,
    val status4k: Int? = null,

    // Plex fields
    val mediaUrl: String? = null,  // Plex web URL
    val iOSPlexUrl: String? = null,  // Plex iOS deep link
    val ratingKey: String? = null,
    val ratingKey4k: String? = null,

    // Jellyfin fields
    val jellyfinMediaId: String? = null,
    val jellyfinMediaId4k: String? = null,

    // Service fields (Radarr/Sonarr/Lidarr)
    val serviceUrl: String? = null,
    val externalServiceSlug: String? = null,
    val serviceId: Long? = null,
    val serviceId4k: Long? = null,
    val externalServiceId: Long? = null,
    val externalServiceId4k: Long? = null,

    val mediaType: RequestType,
    val requests: List<MediaRequest> = emptyList(),
    val issues: List<Issue> = emptyList(),
    val seasons: List<SeasonInfo> = emptyList(),
    val mediaAddedAt: String? = null,
    @Contextual val createdAt: Instant? = null,
    @Contextual val updatedAt: Instant? = null
)