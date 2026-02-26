package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class RequestUser(
    val permissions: Int,
    val warnings: List<String> = emptyList(),
    val id: Long,
    val email: String,
    val plexUsername: String? = null,
    val jellyfinUsername: String? = null,
    val username: String? = null,
    @Contextual val recoveryLinkExpirationDate: Instant? = null,
    val userType: Int,
    val plexId: String? = null,
    val jellyfinUserId: String? = null,
    val avatar: String,
    val avatarETag: String? = null,
    val avatarVersion: Int? = null,
    val movieQuotaLimit: Int? = null,
    val movieQuotaDays: Int? = null,
    val tvQuotaLimit: Int? = null,
    val tvQuotaDays: Int? = null,
    @Contextual val createdAt: Instant,
    @Contextual val updatedAt: Instant,
    val requestCount: Int,
    val displayName: String
)