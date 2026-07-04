package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BazarrMovie(
    @SerialName("radarrId")
    override val serviceId: Long,
    @SerialName("audio_language")
    override val audioLanguage: List<BazarrAudioLanguage> = emptyList(),
    override val alternativeTitles: List<String> = emptyList(),
    override val fanart: String? = null,
    override val imdbId: String? = null,
    override val monitored: Boolean,
    override val overview: String,
    override val path: String,
    override val poster: String? = null,
    override val profileId: Int? = null,
    override val tags: List<String> = emptyList(),
    override val title: String,
    override val year: String,
    @SerialName("missing_subtitles")
    val missingSubtitles: List<BazarrSubtitleLanguage> = emptyList(),
    val sceneName: String? = null,
    val subtitles: List<BazarrSubtitle> = emptyList()
) : BazarrMedia
