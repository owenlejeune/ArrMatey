package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class MediaInfo(
    val id: Long,
    val tmdbId: Long,
    val tvdbId: Long? = null,
    val status: Int,
    val requests: List<MediaRequest> = emptyList(),
    val createdAt: String,
    val updatedAt: String
)