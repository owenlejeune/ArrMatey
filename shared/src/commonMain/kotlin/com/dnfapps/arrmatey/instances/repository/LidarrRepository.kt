package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.arr.api.client.LidarrClient
import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.LidarrTrack
import com.dnfapps.arrmatey.arr.api.model.LidarrTrackFile
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
import kotlinx.coroutines.flow.update

class LidarrRepository(
    instance: Instance,
    httpClient: HttpClient,
    logger: Logger,
) : ArrInstanceRepository(instance, httpClient, logger) {
    val lidarrClient: LidarrClient = client as? LidarrClient ?: LidarrClient(instance, httpClient)

    private val _artistAlbums = MutableStateFlow<Map<Long, List<ArrAlbum>>>(emptyMap())
    override val artistAlbums: StateFlow<Map<Long, List<ArrAlbum>>> = _artistAlbums.asStateFlow()

    private val _artistTracks = MutableStateFlow<Map<Long, Map<Long, List<LidarrTrack>>>>(emptyMap())
    override val artistTracks: StateFlow<Map<Long, Map<Long, List<LidarrTrack>>>> = _artistTracks.asStateFlow()

    private val _artistTrackFiles = MutableStateFlow<Map<Long, Map<Long, List<LidarrTrackFile>>>>(emptyMap())
    override val artistTrackFiles: StateFlow<Map<Long, Map<Long, List<LidarrTrackFile>>>> = _artistTrackFiles.asStateFlow()

    override suspend fun getArtistAlbums(artistId: Long): NetworkResult<List<ArrAlbum>> =
        lidarrClient
            .getAlbums(artistId)
            .onSuccess { albums ->
                val currentMap = _artistAlbums.value.toMutableMap()
                currentMap[artistId] = albums.sortedByDescending { it.releaseDate }
                _artistAlbums.value = currentMap
            }

    override suspend fun getArtistTracks(artistId: Long): NetworkResult<List<LidarrTrack>> =
        lidarrClient
            .getTracks(artistId = artistId)
            .onSuccess { tracks ->
                val currentMap = _artistTracks.value.toMutableMap()
                currentMap[artistId] = tracks.groupBy { it.albumId }
                _artistTracks.value = currentMap
            }

    override suspend fun getArtistTrackFiles(artistId: Long): NetworkResult<List<LidarrTrackFile>> =
        lidarrClient
            .getTrackFiles(artistId = artistId)
            .onSuccess { trackFiles ->
                val currentMap = _artistTrackFiles.value.toMutableMap()
                currentMap[artistId] = trackFiles.groupBy { it.albumId }
                _artistTrackFiles.value = currentMap
            }

    override suspend fun deleteAlbumFiles(
        artistId: Long,
        albumId: Long,
    ): NetworkResult<Unit> {
        val files = (_artistTrackFiles.value[artistId] ?: emptyMap())[albumId] ?: emptyList()
        return deleteTrackFiles(files)
            .onSuccess {
                getArtistAlbums(artistId)
            }
    }

    override suspend fun deleteTrackFiles(tracks: List<LidarrTrackFile>): NetworkResult<Unit> {
        val fileIds = tracks.map { it.id }
        return lidarrClient
            .deleteTracks(fileIds)
            .onSuccess {
                _artistTrackFiles.update { currentMap ->
                    currentMap
                        .mapValues { (_, albumMap) ->
                            albumMap
                                .mapValues { (_, tracks) ->
                                    tracks.filterNot { trackFile ->
                                        fileIds.contains(trackFile.id)
                                    }
                                }.filterValues { it.isNotEmpty() }
                        }.filterValues { it.isNotEmpty() }
                }
            }
    }

    override suspend fun toggleAlbumMonitor(album: ArrAlbum): NetworkResult<ArrAlbum> {
        libraryRepository.setMonitorStatus(OperationStatus.InProgress)

        return lidarrClient
            .toggleMonitored(album)
            .onSuccess { resultAlbum ->
                libraryRepository.setMonitorStatus(
                    OperationStatus.Success(
                        if (resultAlbum.monitored) "Album monitored" else "Album unmonitored",
                    ),
                )
                updateAlbumInCache(resultAlbum.copy(images = album.images))
                libraryRepository.setMonitorStatus(OperationStatus.Idle)
            }.onError { code, message, cause ->
                libraryRepository.setMonitorStatus(OperationStatus.Error(code, message, cause))
                libraryRepository.setMonitorStatus(OperationStatus.Idle)
            }
    }

    override suspend fun updateAlbum(album: ArrAlbum): NetworkResult<ArrAlbum> {
        libraryRepository.setEditItemStatus(OperationStatus.InProgress)

        return lidarrClient
            .updateAlbum(album)
            .onSuccess { resultAlbum ->
                libraryRepository.setEditItemStatus(OperationStatus.Success("Album updated successfully"))
                updateAlbumInCache(resultAlbum.copy(images = album.images))
            }.onError { code, message, cause ->
                libraryRepository.setEditItemStatus(OperationStatus.Error(code, message, cause))
            }
    }

    private fun updateAlbumInCache(album: ArrAlbum) {
        _artistAlbums.update { currentMap ->
            val updatedMap = currentMap.toMutableMap()
            updatedMap.forEach { (artistId, albumList) ->
                val index = albumList.indexOfFirst { it.id == album.id }
                if (index != -1) {
                    val updatedList = albumList.toMutableList()
                    updatedList[index] = album
                    updatedMap[artistId] = updatedList
                }
            }
            updatedMap
        }
    }
}
