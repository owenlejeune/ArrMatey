package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class PodnapisiSettings(
    val verify_ssl: Boolean
)
