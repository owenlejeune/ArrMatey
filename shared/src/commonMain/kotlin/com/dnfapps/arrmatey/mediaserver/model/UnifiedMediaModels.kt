package com.dnfapps.arrmatey.mediaserver.model

import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.Serializable

@Serializable
enum class WatchedFilter(val resource: StringResource) {
    ALL(MR.strings.all),
    UNWATCHED(MR.strings.unwatched),
    WATCHED(MR.strings.watched),
}

@Serializable
enum class MediaType {
    MOVIE,
    SHOW,
    SEASON,
    EPISODE,
    TRACK,
    ALBUM,
    UNKNOWN,
    ;

    companion object {
        fun fromString(value: String): MediaType = when (value.lowercase()) {
            "movie", "movies" -> MOVIE
            "show", "series", "tv", "tvshows" -> SHOW
            "season", "seasons" -> SEASON
            "episode", "episodes" -> EPISODE
            "track", "song" -> TRACK
            "album", "music" -> ALBUM
            else -> UNKNOWN
        }
    }
}

@Serializable
data class UnifiedMediaLibrary(
    val id: String,
    val title: String,
    val type: MediaType,
    val serverType: MediaServerType,
)

@Serializable
data class UnifiedMediaItem(
    val id: String,
    val title: String,
    val type: MediaType,
    val year: Int? = null,
    val summary: String? = null,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val tmdbId: Long? = null,
    val tvdbId: Long? = null,
    val imdbId: String? = null,
    val playbackPositionTicks: Long? = null,
    val durationTicks: Long? = null,
    val played: Boolean = false,
    val rating: Double? = null,
    val serverType: MediaServerType,
    val serverId: String? = null,
    val libraryId: String? = null,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val seriesTitle: String? = null,
    val lastPlayedDate: Long? = null,
    val audioCodec: String? = null,
    val videoCodec: String? = null,
    val videoResolution: String? = null,
) {
    val progressPercentage: Float?
        get() {
            val pos = playbackPositionTicks ?: return null
            val dur = durationTicks ?: return null
            if (dur <= 0) return null
            return (pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f)
        }
}
