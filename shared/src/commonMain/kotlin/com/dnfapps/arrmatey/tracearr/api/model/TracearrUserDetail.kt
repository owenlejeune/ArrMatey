package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TracearrUserDetail(
    val id: String,
    val username: String? = null,
    val email: String? = null,
    @SerialName("thumb_url") val thumbUrl: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("identity_name") val identityName: String? = null,
    @SerialName("trust_score") val trustScore: Int? = null,
    @SerialName("last_activity_at") val lastActivityAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("plex_account_id") val plexAccountId: String? = null,
    val user: TracearrUser? = null,
    val accounts: List<TracearrUserAccount> = emptyList(),
) {
    val effectiveUsername: String get() = username ?: user?.username ?: ""
    val effectiveAvatarUrl: String? get() = avatarUrl ?: thumbUrl ?: user?.avatarUrl ?: user?.thumbUrl
    val effectiveTrustScore: Int? get() = trustScore ?: user?.trustScore
    val effectiveCreatedAt: String? get() = createdAt ?: user?.createdAt
    val effectiveLastActivityAt: String? get() = lastActivityAt ?: user?.lastActivityAt
}

