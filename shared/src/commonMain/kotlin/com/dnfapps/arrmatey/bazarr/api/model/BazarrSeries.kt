package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class BazarrSeries(
    val alternativeTitles: List<String> = emptyList(),
    val audio_language: List<String> = emptyList(),
    val episodeFileCount: Int,
    val ended: Boolean,
    val episodeMissingCount: Int,
    val fanart: String,
    val imdbId: String,
    val lastAired: String,
    val monitored: Boolean,
    val overview: String,
    val path: String,
    val poster: String,
    val profileId: Int,
    val seriesType: String,
    val sonarrSeriesId: Int,
    val tags: List<String>,
    val title: String,
    val tvdbId: Int,
    val year: String
)
