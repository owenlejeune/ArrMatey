package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TracearrActivityQuality(
    val directPlay: Int = 0,
    val directStream: Int = 0,
    val transcode: Int = 0,
    val total: Int = 0,
    val directPlayPercent: Int = 0,
    val directStreamPercent: Int = 0,
    val transcodePercent: Int = 0
)
