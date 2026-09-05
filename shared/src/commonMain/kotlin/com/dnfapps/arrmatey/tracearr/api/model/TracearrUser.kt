package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TracearrUser(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    val username: String? = null,
    @SerialName("thumb_url") val thumbUrl: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("identity_name") val identityName: String? = null,
    @SerialName("trust_score") val trustScore: Int? = null,
    @SerialName("last_activity_at") val lastActivityAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("identity_server_user_ids") val identityServerUserIds: List<String> = emptyList(),
)
