package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TracearrViolation(
    val id: String,
    @SerialName("serverId") val serverId: String? = null,
    @SerialName("serverName") val serverName: String? = null,
    val severity: ViolationSeverity = ViolationSeverity.Unknown,
    val acknowledged: Boolean = false,
    @SerialName("createdAt") val createdAt: String? = null,
    val rule: TracearrViolationRule? = null,
    val user: TracearrUser? = null,
)
