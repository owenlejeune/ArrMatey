package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TracearrServer(
    val id: String? = null,
    val name: String? = null,
    val type: TracearrServerType? = null,
)
