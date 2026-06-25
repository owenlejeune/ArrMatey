package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.bazarr.api.client.BazarrClient
import com.dnfapps.arrmatey.bazarr.api.client.BazarrClientImpl
import com.dnfapps.arrmatey.bazarr.api.model.BazarrEpisode
import com.dnfapps.arrmatey.bazarr.api.model.BazarrMovie
import com.dnfapps.arrmatey.bazarr.api.model.ProviderStatus
import com.dnfapps.arrmatey.bazarr.api.model.ProviderSubtitle
import com.dnfapps.arrmatey.bazarr.api.model.WantedEpisode
import com.dnfapps.arrmatey.bazarr.api.model.WantedMovie
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.client.paging.BasePagingSource
import com.dnfapps.arrmatey.client.paging.PageResult
import com.dnfapps.arrmatey.client.paging.PagingSource
import com.dnfapps.arrmatey.instances.model.Instance
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Instance-scoped repository for a Bazarr server. Mirrors [SeerrInstanceRepository]:
 * it owns a typed [BazarrClient], exposes badge counts as observable state, and provides
 * paged sources for the "wanted" subtitle lists plus subtitle search/download/delete ops.
 */
class BazarrInstanceRepository(
    override val instance: Instance,
    httpClient: HttpClient
) : InstanceScopedRepository {
    val client: BazarrClient = BazarrClientImpl(instance, httpClient)

    private val _wantedEpisodesCount = MutableStateFlow(0)
    val wantedEpisodesCount: StateFlow<Int> = _wantedEpisodesCount.asStateFlow()

    private val _wantedMoviesCount = MutableStateFlow(0)
    val wantedMoviesCount: StateFlow<Int> = _wantedMoviesCount.asStateFlow()

    private val _providerIssuesCount = MutableStateFlow(0)
    val providerIssuesCount: StateFlow<Int> = _providerIssuesCount.asStateFlow()

    override suspend fun testConnection(): NetworkResult<Unit> =
        client.testConnection()

    /** Refresh the badge counts surfaced on the dashboard and tab. */
    suspend fun refreshBadges() {
        client.getBadges().onSuccess { badges ->
            _wantedEpisodesCount.value = badges.episodes
            _wantedMoviesCount.value = badges.movies
            _providerIssuesCount.value = badges.providers
        }
    }

    fun getWantedEpisodesPaging(): PagingSource<WantedEpisode> =
        BasePagingSource(
            fetcher = { page ->
                client.getWantedEpisodes(start = (page - 1) * PAGE_SIZE, length = PAGE_SIZE)
            },
            processor = { response ->
                PageResult(
                    items = response.data,
                    totalItemCount = response.total,
                    hasNextPage = response.data.size >= PAGE_SIZE
                )
            }
        )

    fun getWantedMoviesPaging(): PagingSource<WantedMovie> =
        BasePagingSource(
            fetcher = { page ->
                client.getWantedMovies(start = (page - 1) * PAGE_SIZE, length = PAGE_SIZE)
            },
            processor = { response ->
                PageResult(
                    items = response.data,
                    totalItemCount = response.total,
                    hasNextPage = response.data.size >= PAGE_SIZE
                )
            }
        )

    suspend fun getProviders(): NetworkResult<List<ProviderStatus>> =
        client.getProviders().map { it.data }

    suspend fun resetProviders(): NetworkResult<Unit> =
        client.resetProviders()

    /** All Bazarr-tracked episodes for a Sonarr series id (carries current + missing subs). */
    suspend fun getEpisodes(seriesId: Long): NetworkResult<List<BazarrEpisode>> =
        client.getEpisodes(seriesId).map { it.data }

    /** The Bazarr-tracked movie for a Radarr movie id, or null if Bazarr isn't tracking it. */
    suspend fun getMovie(radarrId: Long): NetworkResult<BazarrMovie?> =
        client.getMovie(radarrId).map { it.data.firstOrNull() }

    suspend fun searchEpisodeSubtitles(episodeId: Long): NetworkResult<List<ProviderSubtitle>> =
        client.searchEpisodeSubtitles(episodeId)

    suspend fun searchMovieSubtitles(radarrId: Long): NetworkResult<List<ProviderSubtitle>> =
        client.searchMovieSubtitles(radarrId)

    suspend fun downloadEpisodeSubtitle(
        seriesId: Long,
        episodeId: Long,
        result: ProviderSubtitle
    ): NetworkResult<Unit> =
        client.downloadEpisodeSubtitle(
            seriesId = seriesId,
            episodeId = episodeId,
            hi = result.isHearingImpaired,
            forced = result.isForced,
            originalFormat = false,
            provider = result.provider,
            subtitle = result.subtitle
        )

    suspend fun downloadMovieSubtitle(
        radarrId: Long,
        result: ProviderSubtitle
    ): NetworkResult<Unit> =
        client.downloadMovieSubtitle(
            radarrId = radarrId,
            hi = result.isHearingImpaired,
            forced = result.isForced,
            originalFormat = false,
            provider = result.provider,
            subtitle = result.subtitle
        )

    suspend fun autoSearchEpisodeSubtitles(
        seriesId: Long,
        episodeId: Long,
        language: String,
        forced: Boolean,
        hi: Boolean
    ): NetworkResult<Unit> =
        client.autoSearchEpisodeSubtitles(seriesId, episodeId, language, forced, hi)

    suspend fun autoSearchMovieSubtitles(
        radarrId: Long,
        language: String,
        forced: Boolean,
        hi: Boolean
    ): NetworkResult<Unit> =
        client.autoSearchMovieSubtitles(radarrId, language, forced, hi)

    suspend fun deleteEpisodeSubtitle(
        seriesId: Long,
        episodeId: Long,
        language: String,
        forced: Boolean,
        hi: Boolean,
        path: String
    ): NetworkResult<Unit> =
        client.deleteEpisodeSubtitle(seriesId, episodeId, language, forced, hi, path)

    suspend fun deleteMovieSubtitle(
        radarrId: Long,
        language: String,
        forced: Boolean,
        hi: Boolean,
        path: String
    ): NetworkResult<Unit> =
        client.deleteMovieSubtitle(radarrId, language, forced, hi, path)

    companion object {
        private const val PAGE_SIZE = 50
    }
}
