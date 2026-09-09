package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TracearrViolationRule(
    val id: String,
    val type: String? = null,
    val name: String? = null,
)
