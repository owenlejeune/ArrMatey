package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class SubF2MSettings(
    val user_agent: String,
    val verify_ssl: Boolean
)
