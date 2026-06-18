package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Addic7edSettings(
    val cookies: String,
    val password: String,
    val user_agent: String,
    val username: String,
    val vip: Boolean
)
