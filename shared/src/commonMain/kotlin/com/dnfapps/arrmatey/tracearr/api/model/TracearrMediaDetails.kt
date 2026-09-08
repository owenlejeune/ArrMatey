package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TracearrMediaDetails(
    val id: String,
    @SerialName("media_type") val mediaType: TracearrMediaType? = null,
    val title: String? = null,
    val year: Int? = null,
    @SerialName("imdb_id") val imdbId: String? = null,
    @SerialName("tmdb_id") val tmdbId: Long? = null,
    @SerialName("tvdb_id") val tvdbId: Long? = null,
    val genres: List<String> = emptyList(),
    @SerialName("show_media_id") val showMediaId: String? = null,
    @SerialName("merged_ids") val mergedIds: List<String> = emptyList(),
    val availability: List<TracearrMediaAvailability> = emptyList(),
    @SerialName("season_count") val seasonCount: Int? = null,
    @SerialName("episode_count") val episodeCount: Int? = null,
)
