package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class RequestUser(
    val permissions: Int = 0,
    val warnings: List<String> = emptyList(),
    val id: Long = 0L,
    val email: String? = null,
    val plexUsername: String? = null,
    val jellyfinUsername: String? = null,
    val username: String? = null,
    @Contextual val recoveryLinkExpirationDate: Instant? = null,
    val userType: Int = 0,
    val plexId: Long? = null,
    val jellyfinUserId: String? = null,
    val avatar: String? = null,
    val avatarETag: String? = null,
    val avatarVersion: String? = null,
    val movieQuotaLimit: Int? = null,
    val movieQuotaDays: Int? = null,
    val tvQuotaLimit: Int? = null,
    val tvQuotaDays: Int? = null,
    @Contextual val createdAt: Instant? = null,
    @Contextual val updatedAt: Instant? = null,
    val requestCount: Int = 0,
    val displayName: String = "",
)
