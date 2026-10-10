package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class VolumeDetailsResponse(
    val error: String? = null,
    val result: ComicVolume? = null,
)
