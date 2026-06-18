package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class LegendasTVSettings(
    val featured_only: Boolean,
    val password: String,
    val username: String
)
