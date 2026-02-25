package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Releases(
    val results: List<ReleaseInfo> = emptyList()
)