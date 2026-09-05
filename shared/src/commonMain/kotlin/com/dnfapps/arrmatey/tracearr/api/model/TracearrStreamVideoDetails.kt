package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TracearrStreamVideoDetails(
    val bitrate: Double? = null,
    val dynamicRange: String? = null,
    val framerate: String? = null,
    val height: Double? = null,
    val width: Double? = null,
)
