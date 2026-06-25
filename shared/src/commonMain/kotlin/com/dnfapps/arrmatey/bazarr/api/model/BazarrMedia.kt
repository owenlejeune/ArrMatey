package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An episode as tracked by Bazarr (from `/api/episodes?seriesid[]=`), carrying its
 * currently downloaded [subtitles] and [missingSubtitles]. The ids match the Sonarr
 * instance Bazarr is synced with, so they can be correlated with ArrMatey's Sonarr data.
 */
@Serializable
data class BazarrEpisode(
    val sonarrSeriesId: Long = 0,
    val sonarrEpisodeId: Long = 0,
    val season: Int = 0,
    val episode: Int = 0,
    val title: String = "",
    val path: String? = null,
    val monitored: Boolean = false,
    val subtitles: List<BazarrSubtitle> = emptyList(),
    @SerialName("missing_subtitles") val missingSubtitles: List<BazarrSubtitleLanguage> = emptyList(),
    val sceneName: String? = null
)

@Serializable
data class BazarrEpisodesResponse(
    val data: List<BazarrEpisode> = emptyList()
)

/**
 * A movie as tracked by Bazarr (from `/api/movies?radarrid[]=`). The [radarrId] matches
 * the Radarr instance Bazarr is synced with.
 */
@Serializable
data class BazarrMovie(
    val radarrId: Long = 0,
    val title: String = "",
    val year: String? = null,
    val path: String? = null,
    val monitored: Boolean = false,
    val poster: String? = null,
    val overview: String? = null,
    val imdbId: String? = null,
    val profileId: Int? = null,
    val subtitles: List<BazarrSubtitle> = emptyList(),
    @SerialName("missing_subtitles") val missingSubtitles: List<BazarrSubtitleLanguage> = emptyList(),
    val sceneName: String? = null
)

@Serializable
data class BazarrMoviesResponse(
    val data: List<BazarrMovie> = emptyList(),
    val total: Int = 0
)
