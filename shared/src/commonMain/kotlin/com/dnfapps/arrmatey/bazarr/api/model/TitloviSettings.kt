package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TitloviSettings(
    val password: String,
    val username: String
)
