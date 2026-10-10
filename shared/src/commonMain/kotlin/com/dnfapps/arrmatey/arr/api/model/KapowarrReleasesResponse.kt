package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class KapowarrReleasesResponse(
    val error: String? = null,
    val result: List<KapowarrRelease>? = emptyList(),
)
