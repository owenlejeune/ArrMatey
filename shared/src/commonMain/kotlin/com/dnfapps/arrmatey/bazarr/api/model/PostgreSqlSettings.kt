package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class PostgreSqlSettings(
    val database: String,
    val enabled: Boolean,
    val host: String,
    val password: String,
    val port: Int,
    val url: String,
    val username: String
)
