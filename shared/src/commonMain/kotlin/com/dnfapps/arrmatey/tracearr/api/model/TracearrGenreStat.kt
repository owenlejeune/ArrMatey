package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TracearrGenreStat(
    val genre: String? = null,
    val plays: Long = 0,
)
