package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TracearrSourceVideoDetails(
    val bitrate: Double? = null,
    val framerate: String? = null,
    val dynamicRange: String? = null,
    val aspectRatio: Double? = null,
    val profile: String? = null,
    val level: String? = null,
    val colorSpace: String? = null,
    val colorDepth: Int? = null,
)
