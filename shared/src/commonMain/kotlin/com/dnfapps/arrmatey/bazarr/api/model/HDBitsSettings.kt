package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class HDBitsSettings(
    val passkey: String,
    val username: String
)
