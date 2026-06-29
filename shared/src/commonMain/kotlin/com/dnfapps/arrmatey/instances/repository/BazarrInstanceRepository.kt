package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.bazarr.api.client.BazarrClientImpl
import com.dnfapps.arrmatey.bazarr.api.model.BazarrEpisode
import com.dnfapps.arrmatey.bazarr.api.model.BazarrMovie
import com.dnfapps.arrmatey.bazarr.api.model.BazarrSeries
import com.dnfapps.arrmatey.bazarr.api.model.BazarrSystem
import com.dnfapps.arrmatey.bazarr.api.model.BazarrSystemStatus
import com.dnfapps.arrmatey.bazarr.api.model.ProviderStatus
import com.dnfapps.arrmatey.bazarr.api.model.ProviderSubtitle
import com.dnfapps.arrmatey.bazarr.api.model.WantedEpisode
import com.dnfapps.arrmatey.bazarr.api.model.WantedMovie
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.client.mapValues
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.instances.model.Instance
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BazarrInstanceRepository(
    override val instance: Instance,
    httpClient: HttpClient
): InstanceScopedRepository {

    companion object {
        private const val PAGE_SIZE = 50
    }

    private val bazarrClient = BazarrClientImpl(instance, httpClient)

    private val _systemSettings = MutableStateFlow<BazarrSystem?>(null)
    val systemSettings: StateFlow<BazarrSystem?> = _systemSettings.asStateFlow()

    private val _systemStatus = MutableStateFlow<BazarrSystemStatus?>(null)
    val systemStatus: StateFlow<BazarrSystemStatus?> = _systemStatus.asStateFlow()

    private val _wantedEpisodesCount = MutableStateFlow(0)
    val wantedEpisodesCount: StateFlow<Int> = _wantedEpisodesCount.asStateFlow()

    private val _wantedMoviesCount = MutableStateFlow(0)
    val wantedMoviesCount: StateFlow<Int> = _wantedMoviesCount.asStateFlow()

    private val _providerIssuesCount = MutableStateFlow(0)
    val providerIssuesCount: StateFlow<Int> = _providerIssuesCount.asStateFlow()

    private val _series = MutableStateFlow<NetworkResult<List<BazarrSeries>>?>(null)
    val series: StateFlow<NetworkResult<List<BazarrSeries>>?> = _series.asStateFlow()

    private val _movies = MutableStateFlow<NetworkResult<List<BazarrMovie>>?>(null)
    val movies: StateFlow<NetworkResult<List<BazarrMovie>>?> = _movies.asStateFlow()

    private val _episodes = MutableStateFlow<Map<Long, List<BazarrEpisode>>>(emptyMap())
    val episodes: StateFlow<Map<Long, List<BazarrEpisode>>> = _episodes.asStateFlow()

    override suspend fun testConnection(): NetworkResult<Unit> =
        bazarrClient.testConnection()

    suspend fun getSystemSettings(): NetworkResult<BazarrSystem> =
        bazarrClient.getSystemSettings()
            .onSuccess { _systemSettings.value = it }

    suspend fun getSystemStatus(): NetworkResult<BazarrSystemStatus> =
        bazarrClient.getSystemStatus()
            .onSuccess { _systemStatus.value = it }

    suspend fun refreshBadges() {
        bazarrClient.getBadges().onSuccess { badges ->
            _wantedEpisodesCount.value = badges.episodes
            _wantedMoviesCount.value = badges.movies
            _providerIssuesCount.value = badges.providers
        }
    }

    suspend fun getWantedEpisodes(): NetworkResult<List<WantedEpisode>> =
        bazarrClient.getWantedEpisodes().map { it.data }

    suspend fun getWantedMovies(): NetworkResult<List<WantedMovie>> =
        bazarrClient.getWantedMovies().map { it.data }

    suspend fun getProviders(): NetworkResult<List<ProviderStatus>> =
        bazarrClient.getProviders().map { it.data }

    suspend fun resetProviders(): NetworkResult<Unit> =
        bazarrClient.resetProviders()

    suspend fun getMovie(radarrId: Long): NetworkResult<BazarrMovie?> =
        bazarrClient.getMovie(radarrId).map { it.data.firstOrNull() }

    suspend fun searchEpisodeSubtitles(episodeId: Long): NetworkResult<List<ProviderSubtitle>> =
        bazarrClient.searchEpisodeSubtitles(episodeId)

    suspend fun searchMovieSubtitles(radarrId: Long): NetworkResult<List<ProviderSubtitle>> =
        bazarrClient.searchMovieSubtitles(radarrId)

    suspend fun getSeries(): NetworkResult<List<BazarrSeries>> =
        bazarrClient.getSeries()
            .mapValues { it.withLocalImages(instance.url) as BazarrSeries }
            .onSuccess { _series.value = NetworkResult.Success(it) }

    suspend fun getMovies(): NetworkResult<List<BazarrMovie>> =
        bazarrClient.getMovies()
            .mapValues { it.withLocalImages(instance.url) as BazarrMovie }
            .onSuccess { _movies.value = NetworkResult.Success(it) }

    suspend fun getEpisodes(seriesId: Long): NetworkResult<List<BazarrEpisode>> =
        bazarrClient.getEpisodes(seriesId)
            .map { it.data }
            .onSuccess {
                val current = _episodes.value.toMutableMap()
                current[seriesId] = it
                _episodes.value = current
            }

    suspend fun downloadEpisodeSubtitle(
        seriesId: Long,
        episodeId: Long,
        result: ProviderSubtitle
    ): NetworkResult<Unit> =
        bazarrClient.downloadEpisodeSubtitle(
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
        bazarrClient.downloadMovieSubtitle(
            radarrId = radarrId,
            hi = result.isHearingImpaired,
            forced = result.isForced,
            originalFormat = false,
            provider = result.provider,
            subtitle = result.subtitle
        )

    suspend fun autoSearchSeriesSubtitles(seriesId: Long): NetworkResult<Unit> =
        bazarrClient.autoSearchSeriesSubtitles(seriesId)

    suspend fun autoSearchMovieSubtitles(radarrId: Long): NetworkResult<Unit> =
        bazarrClient.autoSearchMovieSubtitles(radarrId)

    suspend fun autoSearchEpisodeSubtitles(
        seriesId: Long,
        episodeId: Long,
        language: String,
        forced: Boolean,
        hi: Boolean
    ): NetworkResult<Unit> =
        bazarrClient.autoSearchEpisodeSubtitles(seriesId, episodeId, language, forced, hi)

    suspend fun autoSearchMovieSubtitles(
        radarrId: Long,
        language: String,
        forced: Boolean,
        hi: Boolean
    ): NetworkResult<Unit> =
        bazarrClient.autoSearchMovieSubtitles(radarrId, language, forced, hi)

    suspend fun deleteEpisodeSubtitle(
        seriesId: Long,
        episodeId: Long,
        language: String,
        forced: Boolean,
        hi: Boolean,
        path: String
    ): NetworkResult<Unit> =
        bazarrClient.deleteEpisodeSubtitle(seriesId, episodeId, language, forced, hi, path)

    suspend fun deleteMovieSubtitle(
        radarrId: Long,
        language: String,
        forced: Boolean,
        hi: Boolean,
        path: String
    ): NetworkResult<Unit> =
        bazarrClient.deleteMovieSubtitle(radarrId, language, forced, hi, path)

    suspend fun refresh() {
        getSeries()
        getMovies()
    }
}
