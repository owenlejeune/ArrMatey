package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class SubsyncCheckerSettings(
    val blacklisted_languages: List<String>,
    val blacklisted_providers: List<String>
)
