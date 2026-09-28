package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class RequestMedia(
    val downloadStatus: List<DownloadStatus> = emptyList(),
    val downloadStatus4k: List<DownloadStatus> = emptyList(),
    val id: Long = 0L,
    val mediaType: RequestType = RequestType.Movie,
    val tmdbId: Long = 0L,
    val tvdbId: Long? = null,
    val imdbId: String? = null,
    val status: Int = 0,
    val status4k: Int = 0,
    @Contextual val createdAt: Instant? = null,
    @Contextual val updatedAt: Instant? = null,
    @Contextual val lastSeasonChange: Instant? = null,
    @Contextual val mediaAddedAt: Instant? = null,
    val serviceId: Long? = null,
    val serviceId4k: Long? = null,
    val externalServiceId: Long? = null,
    val externalServiceId4k: Long? = null,
    val externalServiceSlug: String? = null,
    val externalServiceSlug4k: String? = null,
    val ratingKey: String? = null,
    val ratingKey4k: String? = null,
    val jellyfinMediaId: String? = null,
    val jellyfinMediaId4k: String? = null,
)
