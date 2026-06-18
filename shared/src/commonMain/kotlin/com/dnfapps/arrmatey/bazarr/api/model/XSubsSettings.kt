package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class XSubsSettings(
    val password: String,
    val username: String
)
