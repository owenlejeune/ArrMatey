package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
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
        val repository = instanceManager.getArrRepository(instanceId)
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
                            InstanceType.Booksehelf -> {
                                loadReadarrDetails(repository, mediaId, detailsResult.data)
                                    .collect { send(it) }
                            }
                            InstanceType.Listenarr -> {
                                send(MediaDetailsUiState.Success(item = detailsResult.data))
                            }
                            else -> throw IllegalStateException("Unsupported instance type ${repository.instance.type}")
                        }
                    }
                }
            }
    }

    private fun loadSonarrDetails(
        repository: ArrInstanceRepository,
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
        repository: ArrInstanceRepository,
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
        repository: ArrInstanceRepository,
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

    private fun loadReadarrDetails(
        repository: ArrInstanceRepository,
        authorId: Long,
        author: ArrMedia
    ): Flow<MediaDetailsUiState> = flow {
        repository.getAuthorBookFiles(authorId)
        repository.getAuthorSeries(authorId)

        combine(
            repository.authorBookFiles,
            repository.authorSeries,
            repository.authorBooks
        ) { bookFilesMap, bookSeriesMap, booksMap ->
            val bookFiles = bookFilesMap[authorId] ?: emptyList()
            val bookSeries = bookSeriesMap[authorId] ?: emptyList()
            val books = booksMap[authorId] ?: emptyList()

            MediaDetailsUiState.Success(
                item = author,
                bookFiles = bookFiles,
                bookSeries = bookSeries,
                books = books
            )
        }.collect { state ->
            emit(state)
        }
    }
}