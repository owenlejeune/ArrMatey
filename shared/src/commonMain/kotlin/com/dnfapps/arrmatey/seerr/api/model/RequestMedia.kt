package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class RequestMedia(
    val downloadStatus: List<DownloadStatus> = emptyList(),
    val downloadStatus4k: List<DownloadStatus> = emptyList(),
    val id: Long,
    val mediaType: RequestType,
    val tmdbId: Long,
    val tvdbId: Long? = null,
    val imdbId: String? = null,
    val status: Int,
    val status4k: Int,
    @Contextual val createdAt: Instant,
    @Contextual val updatedAt: Instant,
    @Contextual val lastSeasonChange: Instant,
    @Contextual val mediaAddedAt: Instant,
    val serviceId: Long? = null,
    val serviceId4k: Long? = null,
    val externalServiceId: Long? = null,
    val externalServiceId4k: Long? = null,
    val externalServiceSlug: String? = null,
    val externalServiceSlug4k: String? = null,
    val ratingKey: String? = null,
    val ratingKey4k: String? = null,
    val jellyfinMediaId: String? = null,
    val jellyfinMediaId4k: String? = null
)