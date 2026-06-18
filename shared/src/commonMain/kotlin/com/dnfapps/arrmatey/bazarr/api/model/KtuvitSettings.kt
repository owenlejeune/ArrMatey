package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class KtuvitSettings(
    val email: String,
    val hashed_password: String
)
