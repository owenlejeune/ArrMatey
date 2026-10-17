package com.dnfapps.arrmatey.instances.model

import kotlinx.serialization.Serializable

@Serializable
data class PlexPinResponse(
    val id: Long,
    val code: String,
    val product: String? = null,
    val clientIdentifier: String? = null,
    val expiresIn: Long? = null,
    val createdAt: String? = null,
    val expiresAt: String? = null,
    val authToken: String? = null,
)
