package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.arr.api.client.SonarrClient
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
import dev.shivathapaa.logger.api.Logger
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SonarrRepository(
    instance: Instance,
    httpClient: HttpClient,
    logger: Logger,
) : ArrInstanceRepository(instance, httpClient, logger) {
    val sonarrClient: SonarrClient = client as? SonarrClient ?: SonarrClient(instance, httpClient)

    private val _episodes = MutableStateFlow<Map<Long, List<Episode>>>(emptyMap())
    override val episodes: StateFlow<Map<Long, List<Episode>>> = _episodes.asStateFlow()

    override suspend fun getEpisodes(
        seriesId: Long,
        seasonNumber: Int?,
    ): NetworkResult<List<Episode>> =
        sonarrClient
            .getEpisodes(seriesId, seasonNumber)
            .onSuccess { epList ->
                val currentMap = _episodes.value.toMutableMap()
                currentMap[seriesId] = epList
                _episodes.value = currentMap
            }

    override suspend fun toggleSeasonMonitor(
        id: Long,
        seasonNumber: Int,
    ): NetworkResult<ArrMedia> {
        libraryRepository.setMonitorStatus(OperationStatus.InProgress)

        val currentSeries = libraryRepository.mediaDetailsCache.value[id] as? ArrSeries
        if (currentSeries == null) {
            libraryRepository.setMonitorStatus(OperationStatus.Error(message = "Series not found in cache"))
            return NetworkResult.Error(message = "Series not found in cache")
        }

        val updatedSeason =
            currentSeries.seasons.map { season ->
                if (season.seasonNumber == seasonNumber) {
                    season.copy(monitored = !season.monitored)
                } else {
                    season
                }
            }

        val updatedSeries = currentSeries.copy(seasons = updatedSeason)

        return sonarrClient
            .update(updatedSeries)
            .onSuccess { resultSeries ->
                val series = resultSeries as ArrSeries
                libraryRepository.setMonitorStatus(OperationStatus.Success("Season monitor toggled"))
                libraryRepository.updateMediaDetailsCache(id, series)
                libraryRepository.updateItemInLibraryCache(series)
            }.onError { code, message, cause ->
                libraryRepository.setMonitorStatus(OperationStatus.Error(code, message, cause))
            }.also {
                libraryRepository.setMonitorStatus(OperationStatus.Idle)
            }
    }

    override suspend fun toggleEpisodeMonitor(episode: Episode): NetworkResult<Episode> {
        libraryRepository.setMonitorStatus(OperationStatus.InProgress)

        val updatedEpisode = episode.copy(monitored = !episode.monitored)

        return sonarrClient
            .updateEpisode(updatedEpisode)
            .onSuccess { resultEpisode ->
                libraryRepository.setMonitorStatus(
                    OperationStatus.Success(
                        if (resultEpisode.monitored) "Episode monitored" else "Episode unmonitored",
                    ),
                )
                updateEpisodeInCache(
                    resultEpisode.copy(
                        images = episode.images,
                        episodeFile = episode.episodeFile,
                    ),
                )
            }.onError { code, message, cause ->
                libraryRepository.setMonitorStatus(OperationStatus.Error(code, message, cause))
            }.also {
                libraryRepository.setMonitorStatus(OperationStatus.Idle)
            }
    }

    private fun updateEpisodeInCache(episode: Episode) {
        val currentEpisodes = _episodes.value.toMutableMap()
        currentEpisodes.forEach { (seriesId, episodeList) ->
            val index = episodeList.indexOfFirst { it.id == episode.id }
            if (index != -1) {
                val updatedList = episodeList.toMutableList()
                updatedList[index] = episode
                currentEpisodes[seriesId] = updatedList
            }
        }
        _episodes.value = currentEpisodes
    }

    override suspend fun deleteSeasonFiles(
        seriesId: Long,
        seasonNumber: Int,
    ): NetworkResult<Unit> {
        var epList = _episodes.value[seriesId]?.filter { it.seasonNumber == seasonNumber } ?: emptyList()
        if (epList.isEmpty()) {
            val fetchResult = getEpisodes(seriesId)
            if (fetchResult is NetworkResult.Success) {
                epList = fetchResult.data.filter { it.seasonNumber == seasonNumber }
            }
        }
        val fileIds =
            epList
                .filter { it.hasFile || it.episodeFile != null || (it.episodeFileId != null && it.episodeFileId != 0L) }
                .mapNotNull { it.episodeFileId?.takeIf { id -> id != 0L } ?: it.episodeFile?.id }
        if (fileIds.isEmpty()) {
            return NetworkResult.Success(Unit)
        }
        return deleteEpisodes(seriesId, epList)
            .onSuccess {
                getEpisodes(seriesId)
                getMediaDetails(seriesId)
            }
    }

    override suspend fun deleteEpisodes(
        seriesId: Long,
        episodes: List<Episode>,
    ): NetworkResult<Unit> {
        val fileIds =
            episodes
                .filter { it.hasFile || it.episodeFile != null || (it.episodeFileId != null && it.episodeFileId != 0L) }
                .mapNotNull { it.episodeFileId?.takeIf { id -> id != 0L } ?: it.episodeFile?.id }
        if (fileIds.isEmpty()) {
            return NetworkResult.Success(Unit)
        }
        return sonarrClient
            .deleteEpisodes(fileIds)
            .onSuccess {
                getEpisodes(seriesId)
            }
    }

    override suspend fun deleteEpisodeFile(
        seriesId: Long,
        fileId: Long,
    ): NetworkResult<Unit> =
        sonarrClient
            .deleteEpisode(fileId)
            .onSuccess {
                getEpisodes(seriesId)
            }
}
