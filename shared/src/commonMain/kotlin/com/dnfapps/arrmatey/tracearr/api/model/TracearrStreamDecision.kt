package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TracearrStreamDecision {
    @SerialName("directplay") DirectPlay,
    @SerialName("copy") Copy,
    @SerialName("transcode") Transcode,
}
