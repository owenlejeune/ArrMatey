package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class DeathByCaptchaSettings(
    val password: String,
    val username: String
)
