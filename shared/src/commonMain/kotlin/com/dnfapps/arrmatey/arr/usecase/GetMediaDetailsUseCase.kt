package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.instances.repository.InstanceScopedRepository
import com.dnfapps.arrmatey.arr.state.MediaDetailsUiState
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class GetMediaDetailsUseCase(
    private val instanceManager: InstanceManager,
) {
    operator fun invoke(mediaId: Long, instanceId: Long): Flow<MediaDetailsUiState> = channelFlow {
        val repository = instanceManager.getRepository(instanceId)
        if (repository == null) {
            send(MediaDetailsUiState.Error(
                message = "Instance not found"
            ))
            return@channelFlow
        }

        send(MediaDetailsUiState.Loading)

        repository.observeMediaDetails(mediaId)
            .collectLatest { detailsResult ->
                when (detailsResult) {
                    is NetworkResult.Loading -> send(MediaDetailsUiState.Loading)
                    is NetworkResult.Error -> {
                        send(MediaDetailsUiState.Error(detailsResult.message))
                    }
                    is NetworkResult.Success -> {
                        when (repository.instance.type) {
                            InstanceType.Sonarr -> {
                                loadSonarrDetails(repository, mediaId, detailsResult.data)
                                    .collect { send(it) }
                            }
                            InstanceType.Radarr -> {
                                loadRadarrDetails(repository, mediaId, detailsResult.data)
                                    .collect { send(it) }
                            }
                            InstanceType.Lidarr -> {
                                loadLidarrDetails(repository, mediaId, detailsResult.data)
                                    .collect { send(it) }
                            }
                            InstanceType.Prowlarr -> {
                                // Prowlarr has no media details, should not be here
                            }
                        }
                    }
                }
            }
    }

    private fun loadSonarrDetails(
        repository: InstanceScopedRepository,
        seriesId: Long,
        series: ArrMedia
    ): Flow<MediaDetailsUiState> = flow {
        val episodeResult = repository.getEpisodes(seriesId)
        when (episodeResult) {
            is NetworkResult.Success -> {
                repository.episodes
                    .map { it[seriesId] ?: emptyList() }
                    .collect { episodes ->
                        emit(MediaDetailsUiState.Success(
                            item = series,
                            episodes = episodes
                        ))
                    }
            }
            else -> {} // do nothing for now
        }
    }

    private fun loadRadarrDetails(
        repository: InstanceScopedRepository,
        movieId: Long,
        movie: ArrMedia
    ): Flow<MediaDetailsUiState> = flow {
        val movieFileResult = repository.getMovieExtraFiles(movieId)
        when (movieFileResult) {
            is NetworkResult.Success -> {
                repository.movieExtraFiles
                    .map { it[movieId] ?: emptyList() }
                    .collect { extraFiles ->
                        emit(MediaDetailsUiState.Success(
                            item = movie,
                            extraFiles = extraFiles
                        ))
                    }
            }
            else -> emit(MediaDetailsUiState.Success(item = movie))
        }
    }

    private fun loadLidarrDetails(
        repository: InstanceScopedRepository,
        artistId: Long,
        artist: ArrMedia
    ): Flow<MediaDetailsUiState> = flow {
        repository.getArtistAlbums(artistId)
        repository.getArtistTracks(artistId)
        repository.getArtistTrackFiles(artistId)

        combine(
            repository.artistAlbums,
            repository.artistTracks,
            repository.artistTrackFiles
        ) { albumMap, tracksMap, filesMap ->
            val albums = albumMap[artistId] ?: emptyList()
            val tracks = tracksMap[artistId] ?: emptyMap()
            val files = filesMap[artistId] ?: emptyMap()

            MediaDetailsUiState.Success(
                item = artist,
                albums = albums,
                tracks = tracks,
                trackFiles = files
            )
        }.collect { state ->
            emit(state)
        }
    }
}