package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ViolationSeverity {
    @SerialName("low")
    Low,

    @SerialName("warning")
    Warning,

    @SerialName("high")
    High,

    Unknown
}
