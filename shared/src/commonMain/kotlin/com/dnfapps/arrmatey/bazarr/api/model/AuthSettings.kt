package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthSettings(
    val apikey: String,
    val password: String,
    val type: String?,
    val username: String
)
