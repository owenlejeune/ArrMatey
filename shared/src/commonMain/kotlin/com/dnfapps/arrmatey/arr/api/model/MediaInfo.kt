package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class MediaInfo(
    val audioBitrate: String? = null,
    val audioBits: String? = null,
    val audioChannels: Double? = null,
    val audioCodec: String? = null,
    val audioLanguages: String? = null,
    val audioStreamCount: Int? = null,
    val audioSampleRate: String? = null,
    val videoBitDepth: Int? = null,
    val videoBitrate: Int? = null,
    val videoCodec: String? = null,
    val videoFps: Double? = null,
    val videoDynamicRange: String? = null,
    val videoDynamicRangeType: String? = null,
    val resolution: String? = null,
    val runTime: String? = null,
    val scanType: String? = null,
    val subtitles: String? = null,
)
