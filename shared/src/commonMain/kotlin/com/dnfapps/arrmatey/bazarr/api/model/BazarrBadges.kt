package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Counts surfaced by Bazarr's `/api/badges` endpoint, used to drive UI badges:
 * episodes/movies with missing subtitles, providers with issues, etc.
 */
@Serializable
data class BazarrBadges(
    val episodes: Int = 0,
    val movies: Int = 0,
    val providers: Int = 0,
    val status: Int = 0,
    val announcements: Int = 0,
    @SerialName("sonarr_signalr") val sonarrSignalr: String? = null,
    @SerialName("radarr_signalr") val radarrSignalr: String? = null
)
