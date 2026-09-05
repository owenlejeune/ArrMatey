package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TracearrUser(
    val id: String? = null,
    val userId: String? = null,
    val username: String? = null,
    val thumbUrl: String? = null,
    val avatarUrl: String? = null,
    val identityName: String? = null,
    val trustScore: Int? = null,
    val lastActivityAt: String? = null,
    val createdAt: String? = null,
    val identityServerUserIds: List<String> = emptyList(),
)
