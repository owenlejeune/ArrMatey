package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TracearrStreamAudioDetails(
    val bitrate: Double? = null,
    val channels: Double? = null,
    val language: String? = null,
)
