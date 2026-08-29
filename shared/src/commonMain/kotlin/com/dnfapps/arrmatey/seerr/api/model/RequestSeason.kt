package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class RequestSeason(
    val id: Long,
    val seasonNumber: Int,
    val status: Int,
    @Contextual val createdAt: Instant,
    @Contextual val updatedAt: Instant,
)
