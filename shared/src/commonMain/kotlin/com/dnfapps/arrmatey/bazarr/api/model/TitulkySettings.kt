package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TitulkySettings(
    val approved_only: Boolean,
    val password: String,
    val skip_wrong_fps: Boolean,
    val username: String
)
