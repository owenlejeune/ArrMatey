package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class KaragargaSettings(
    val f_password: String,
    val f_username: String,
    val password: String,
    val username: String
)
