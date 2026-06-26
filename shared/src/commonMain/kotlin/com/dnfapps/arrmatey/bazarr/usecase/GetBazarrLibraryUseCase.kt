package com.dnfapps.arrmatey.bazarr.usecase

import com.dnfapps.arrmatey.bazarr.state.BazarrLibrary
import com.dnfapps.arrmatey.client.ErrorType
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.instances.repository.BazarrInstanceRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetBazarrLibraryUseCase() {
    operator fun invoke(repository: BazarrInstanceRepository): Flow<BazarrLibrary> = flow {
        emit(BazarrLibrary.Loading)

        coroutineScope {
            val seriesDeferred = async { repository.getSeries() }
            val moviesDeferred = async { repository.getMovies() }
            val wantedEpisodesDeferred = async { repository.getWantedEpisodes() }
            val wantedMoviesDeferred = async { repository.getWantedMovies() }
            val providersDeferred = async { repository.getProviders() }

            val seriesResult = seriesDeferred.await()
            val moviesResult = moviesDeferred.await()
            val wantedEpisodesResult = wantedEpisodesDeferred.await()
            val wantedMoviesResult = wantedMoviesDeferred.await()
            val providersResult = providersDeferred.await()

            if (seriesResult is NetworkResult.Success &&
                moviesResult is NetworkResult.Success &&
                wantedEpisodesResult is NetworkResult.Success &&
                wantedMoviesResult is NetworkResult.Success &&
                providersResult is NetworkResult.Success
            ) {
                emit(
                    BazarrLibrary.Success(
                        series = seriesResult.data,
                        movies = moviesResult.data,
                        wantedEpisodes = wantedEpisodesResult.data,
                        wantedMovies = wantedMoviesResult.data,
                        providers = providersResult.data
                    )
                )
            } else {
                val error = listOf(
                    seriesResult,
                    moviesResult,
                    wantedEpisodesResult,
                    wantedMoviesResult,
                    providersResult
                ).filterIsInstance<NetworkResult.Error>().firstOrNull()

                if (error != null) {
                    emit(BazarrLibrary.Error(error.message ?: "An error occurred", error.errorType))
                } else {
                    emit(BazarrLibrary.Error("An unknown error occurred", ErrorType.Unexpected))
                }
            }
        }
    }
}
