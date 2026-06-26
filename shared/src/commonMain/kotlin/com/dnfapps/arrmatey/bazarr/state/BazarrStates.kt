package com.dnfapps.arrmatey.bazarr.state

import com.dnfapps.arrmatey.bazarr.api.model.BazarrSubtitle
import com.dnfapps.arrmatey.bazarr.api.model.BazarrSubtitleLanguage
import com.dnfapps.arrmatey.bazarr.api.model.ProviderStatus
import com.dnfapps.arrmatey.bazarr.api.model.ProviderSubtitle

/** The sub-sections shown within the Bazarr tab. */
enum class BazarrSection {
    WantedEpisodes,
    WantedMovies,
    Series,
    Movies,
    Providers
}

/**
 * Identifies the media item a subtitle operation targets. Episode operations need both the
 * Sonarr series id and episode id; movie operations need the Radarr movie id. These ids
 * come from the Sonarr/Radarr instance Bazarr is synced with.
 */
sealed interface BazarrMediaTarget {
    data class Episode(val seriesId: Long, val episodeId: Long) : BazarrMediaTarget
    data class Movie(val radarrId: Long) : BazarrMediaTarget
}

/** State of a manual provider subtitle search. */
sealed interface SubtitleSearchState {
    data object Idle : SubtitleSearchState
    data object Loading : SubtitleSearchState
    data class Success(val results: List<ProviderSubtitle>) : SubtitleSearchState
    data class Error(val message: String) : SubtitleSearchState
}

/**
 * State of the subtitle section shown on a Sonarr/Radarr detail screen: the currently
 * downloaded subtitles and the languages still missing for the item.
 */
sealed interface BazarrSubtitlesUiState {
    data object Loading : BazarrSubtitlesUiState
    /** No Bazarr instance is configured, so the section should be hidden. */
    data object NoInstance : BazarrSubtitlesUiState
    /** A Bazarr instance is configured but isn't tracking this item. */
    data object NotTracked : BazarrSubtitlesUiState
    data class Error(val message: String) : BazarrSubtitlesUiState
    data class Success(
        val present: List<BazarrSubtitle>,
        val embedded: List<BazarrSubtitle>,
        val missing: List<BazarrSubtitleLanguage>
    ) : BazarrSubtitlesUiState
}

/** State of the Bazarr providers list. */
data class ProvidersUiState(
    val isLoading: Boolean = false,
    val providers: List<ProviderStatus> = emptyList(),
    val error: String? = null
)
