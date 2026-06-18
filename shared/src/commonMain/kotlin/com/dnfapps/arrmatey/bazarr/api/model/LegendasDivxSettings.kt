package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class LegendasDivxSettings(
    val password: String,
    val skip_wrong_fps: Boolean,
    val username: String
)
