package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** An episode that is missing one or more subtitles, from `/api/episodes/wanted`. */
@Serializable
data class WantedEpisode(
    val seriesTitle: String = "",
    @SerialName("episode_number") val episodeNumber: String = "",
    val episodeTitle: String = "",
    @SerialName("missing_subtitles") val missingSubtitles: List<BazarrSubtitleLanguage> = emptyList(),
    val sonarrSeriesId: Long = 0,
    val sonarrEpisodeId: Long = 0,
    val sceneName: String? = null,
    val seriesType: String? = null
)

@Serializable
data class WantedEpisodesResponse(
    val data: List<WantedEpisode> = emptyList(),
    val total: Int = 0
)

/** A movie that is missing one or more subtitles, from `/api/movies/wanted`. */
@Serializable
data class WantedMovie(
    val title: String = "",
    @SerialName("missing_subtitles") val missingSubtitles: List<BazarrSubtitleLanguage> = emptyList(),
    val radarrId: Long = 0,
    val sceneName: String? = null
)

@Serializable
data class WantedMoviesResponse(
    val data: List<WantedMovie> = emptyList(),
    val total: Int = 0
)
