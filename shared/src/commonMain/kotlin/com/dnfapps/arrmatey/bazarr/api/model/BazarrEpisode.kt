package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BazarrEpisode(
    @SerialName("audio_language")
    val audioLanguages: List<BazarrAudioLanguage> = emptyList(),
    val episode: Int,
    val season: Int,
    @SerialName("missing_subtitles")
    val missingSubtitles: List<BazarrSubtitleLanguage> = emptyList(),
    val monitored: Boolean,
    val path: String,
    val sonarrEpisodeId: Long,
    val sonarrSeriesId: Long,
    val subtitles: List<BazarrSubtitle> = emptyList(),
    val title: String,
    val sceneName: String? = null
)