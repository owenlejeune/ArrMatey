package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class VolumesResponse(
    val error: String? = null,
    val result: List<ComicVolume>? = emptyList(),
)
