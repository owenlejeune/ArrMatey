package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class SeriesScoresSettings(
    val audio_codec: Int,
    val episode: Int,
    val hash: Int,
    val hearing_impaired: Int,
    val release_group: Int,
    val resolution: Int,
    val season: Int,
    val series: Int,
    val source: Int,
    val streaming_service: Int,
    val video_codec: Int,
    val year: Int
)
