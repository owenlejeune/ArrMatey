package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TracearrTranscodeInfo(
    @SerialName("container_decision") val containerDecision: TracearrStreamDecision? = null,
    @SerialName("hw_decoding") val hwDecoding: String? = null,
    @SerialName("hw_encoding") val hwEncoding: String? = null,
    @SerialName("hw_requested") val hwRequested: Boolean? = null,
    val reasons: List<String> = emptyList(),
    @SerialName("source_container") val sourceContainer: String? = null,
    val speed: Double? = null,
    @SerialName("stream_container") val streamContainer: String? = null,
    val throttled: Boolean? = null,
)
