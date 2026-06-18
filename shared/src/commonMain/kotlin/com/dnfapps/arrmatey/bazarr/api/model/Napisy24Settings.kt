package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Napisy24Settings(
    val password: String,
    val username: String
)
