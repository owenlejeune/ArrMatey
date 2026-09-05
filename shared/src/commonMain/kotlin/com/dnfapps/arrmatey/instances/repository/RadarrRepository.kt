package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.arr.api.client.RadarrClient
import com.dnfapps.arrmatey.arr.api.model.ExtraFile
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.onSuccess
import dev.shivathapaa.logger.api.Logger
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RadarrRepository(
    instance: Instance,
    httpClient: HttpClient,
    logger: Logger,
) : ArrInstanceRepository(instance, httpClient, logger) {
    val radarrClient: RadarrClient = client as? RadarrClient ?: RadarrClient(instance, httpClient)

    private val _movieExtraFiles = MutableStateFlow<Map<Long, List<ExtraFile>>>(emptyMap())
    override val movieExtraFiles: StateFlow<Map<Long, List<ExtraFile>>> = _movieExtraFiles.asStateFlow()

    override suspend fun getMovieExtraFiles(movieId: Long): NetworkResult<List<ExtraFile>> =
        radarrClient
            .getMovieExtraFile(movieId)
            .onSuccess { files ->
                val currentMap = _movieExtraFiles.value.toMutableMap()
                currentMap[movieId] = files
                _movieExtraFiles.value = currentMap
            }

    override suspend fun deleteMovieFile(movieId: Long): NetworkResult<Unit> = radarrClient.deleteMovieFile(movieId)
}
