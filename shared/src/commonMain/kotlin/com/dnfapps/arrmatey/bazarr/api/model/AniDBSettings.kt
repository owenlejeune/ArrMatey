package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class AniDBSettings(
    val api_client: String,
    val api_client_ver: Int
)
