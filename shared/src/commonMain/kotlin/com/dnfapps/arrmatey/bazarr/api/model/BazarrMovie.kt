package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class BazarrMovie(
    val alternativeTitles: List<String>,
    val audio_language: List<BazarrAudioLanguage>,
    val fanart: String,
    val imdbId: String,
    val missing_subtitles: List<String>,
    val monitored: Boolean,
    val overview: String,
    val path: String,
    val poster: String,
    val profileId: Int,
    val radarrId: Int,
    val sceneName: String? = null,
    val subtitles: List<BazarrSubtitle>,
    val tags: List<String>,
    val title: String,
    val year: String
)
