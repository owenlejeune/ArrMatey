package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class LocalAuthBody(
    val email: String,
    val password: String,
)

@Serializable
data class PlexAuthBody(
    val authToken: String,
)

@Serializable
data class JellyfinAuthBody(
    val username: String,
    val password: String,
)

@Serializable
data class JellyfinQuickConnectAuthBody(
    val secret: String,
)

@Serializable
data class QuickConnectInitiateResponse(
    val code: String,
    val secret: String,
)

@Serializable
data class QuickConnectCheckResponse(
    val authenticated: Boolean,
)

data class SeerrAuthResult(
    val user: SeerrUser,
    val sessionToken: String? = null,
)
