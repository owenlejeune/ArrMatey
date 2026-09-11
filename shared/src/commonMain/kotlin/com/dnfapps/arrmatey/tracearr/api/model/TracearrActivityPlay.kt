package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TracearrActivityPlay(
    val date: String? = null,
    val serverId: String? = null,
    val count: Int = 0,
)
