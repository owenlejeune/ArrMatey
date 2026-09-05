package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TracearrSubtitleInfo(
    val codec: String? = null,
    val decision: String? = null,
    val forced: Boolean? = null,
    val language: String? = null,
)
