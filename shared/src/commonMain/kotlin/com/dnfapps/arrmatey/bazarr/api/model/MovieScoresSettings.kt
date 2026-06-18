package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class MovieScoresSettings(
    val audio_codec: Int,
    val edition: Int,
    val hash: Int,
    val hearing_impaired: Int,
    val release_group: Int,
    val resolution: Int,
    val source: Int,
    val streaming_service: Int,
    val title: Int,
    val video_codec: Int,
    val year: Int
)
