package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class BazarrReleasesResponse(
    val data: List<BazarrRelease> = emptyList(),
    val total: Int = 0
)
