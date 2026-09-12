package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TracearrActivityByHoD(
    val hour: Int = 0,
    val count: Int = 0,
)
