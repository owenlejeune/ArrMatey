package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TracearrSourceAudioDetails(
    val bitrate: Double? = null,
    val channelLayout: String? = null,
    val language: String? = null,
    val sampleRate: Double? = null,
)
