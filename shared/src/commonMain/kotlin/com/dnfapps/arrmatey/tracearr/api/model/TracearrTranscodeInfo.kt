package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TracearrTranscodeInfo(
    val containerDecision: TracearrStreamDecision? = null,
    val hwDecoding: String? = null,
    val hwEncoding: String? = null,
    val hwRequested: Boolean? = null,
    val reasons: List<String> = emptyList(),
    val sourceContainer: String? = null,
    val speed: Double? = null,
    val streamContainer: String? = null,
    val throttled: Boolean? = null,
)
