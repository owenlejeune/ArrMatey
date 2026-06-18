package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ProxySettings(
    val exclude: List<String>,
    val password: String,
    val port: String,
    val type: String?,
    val url: String,
    val username: String
)
