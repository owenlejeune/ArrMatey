package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TracearrActivityConcurrent(
    val date: String? = null,
    val total: Int = 0,
    val direct: Int = 0,
    val directStream: Int = 0,
    val transcode: Int = 0,
)
