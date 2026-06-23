package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.bazarr.api.client.BazarrClient
import com.dnfapps.arrmatey.bazarr.api.model.BazarrEpisode
import com.dnfapps.arrmatey.bazarr.api.model.BazarrMovie
import com.dnfapps.arrmatey.bazarr.api.model.BazarrSeries
import com.dnfapps.arrmatey.bazarr.api.model.BazarrSystem
import com.dnfapps.arrmatey.bazarr.api.model.BazarrSystemStatus
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

    private val bazarrClient = BazarrClient(instance, httpClient)

    private val _systemSettings = MutableStateFlow<BazarrSystem?>(null)
    val systemSettings: StateFlow<BazarrSystem?> = _systemSettings.asStateFlow()

    private val _systemStatus = MutableStateFlow<BazarrSystemStatus?>(null)
    val systemStatus: StateFlow<BazarrSystemStatus?> = _systemStatus.asStateFlow()

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
            .onSuccess {
                val current = _episodes.value.toMutableMap()
                current[seriesId] = it
                _episodes.value = current
            }

    suspend fun refresh() {
        getSeries()
        getMovies()
    }

}
