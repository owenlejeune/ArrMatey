package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class SeerrUser(
    val id: Long = 0L,
    val permissions: Int = 0,
    val userType: Int = 0,
    val displayName: String = "",
    val email: String? = null,
    val plexUsername: String? = null,
    val jellyfinUsername: String? = null,
    val username: String? = null,
    @Contextual val recoveryLinkExpirationDate: Instant? = null,
    val plexId: Long? = null,
    val jellyfinUserId: String? = null,
    val avatar: String? = null,
    val avatarETag: String? = null,
    val avatarVersion: String? = null,
    val movieQuotaLimit: Int? = null,
    val movieQuotaDays: Int? = null,
    val tvQuotaLimit: Int? = null,
    val tvQuotaDays: Int? = null,
    val requestCount: Int = 0,
    val warnings: List<String> = emptyList(),
    @Contextual val createdAt: Instant? = null,
    @Contextual val updatedAt: Instant? = null,
    // settings: UserSettings? = null
) {
    val effectiveDisplayName: String
        get() = displayName.ifBlank { username ?: email ?: plexUsername ?: jellyfinUsername ?: "User" }

    fun hasPermission(permission: UserPermission): Boolean {
        if ((this.permissions and UserPermission.ADMIN.bit) != 0) return true
        return (this.permissions and permission.bit) != 0
    }

    fun hasAnyPermission(vararg permissions: UserPermission): Boolean = permissions.any { hasPermission(it) }
}
