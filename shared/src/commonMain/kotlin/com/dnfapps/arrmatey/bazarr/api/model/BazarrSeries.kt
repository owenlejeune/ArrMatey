package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BazarrSeries(
    @SerialName("sonarrSeriesId")
    override val serviceId: Long,
    @SerialName("audio_language")
    override val audioLanguage: List<BazarrAudioLanguage> = emptyList(),
    override val alternativeTitles: List<String> = emptyList(),
    override val fanart: String? = null,
    override val imdbId: String,
    override val monitored: Boolean,
    override val overview: String,
    override val path: String,
    override val poster: String? = null,
    override val profileId: Int,
    override val tags: List<String> = emptyList(),
    override val title: String,
    override val year: String,
    val episodeFileCount: Int,
    val ended: Boolean,
    val episodeMissingCount: Int,
    val lastAired: String? = null,
    val seriesType: String,
    val tvdbId: Int,
) : BazarrMedia
