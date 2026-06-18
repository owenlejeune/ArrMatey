package com.dnfapps.arrmatey.bazarr.usecase

import com.dnfapps.arrmatey.bazarr.state.BazarrLibrary
import com.dnfapps.arrmatey.client.ErrorType
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class GetBazarrLibraryUseCase(
    private val instanceManager: InstanceManager
) {
    operator fun invoke(): Flow<BazarrLibrary> = flow {
        val repository = instanceManager.getSelectedBazarrRepository()
            .first()
        if (repository == null) {
            emit(BazarrLibrary.Error("Instance not found", ErrorType.Unexpected))
            return@flow
        }

        emit(BazarrLibrary.Loading)

        coroutineScope {
            val seriesDeferred = async { repository.getSeries() }
            val moviesDeferred = async { repository.getMovies() }

            val seriesResult = seriesDeferred.await()
            val moviesResult = moviesDeferred.await()

            if (seriesResult is NetworkResult.Success && moviesResult is NetworkResult.Success) {
                emit(BazarrLibrary.Success(seriesResult.data, moviesResult.data))
            } else if (seriesResult is NetworkResult.Error) {
                emit(BazarrLibrary.Error(seriesResult.message ?: "Failed to get series", seriesResult.errorType))
            } else if (moviesResult is NetworkResult.Error) {
                emit(BazarrLibrary.Error(moviesResult.message ?: "Failed to get movies", moviesResult.errorType))
            } else {
                emit(BazarrLibrary.Error("An unknown error occurred", ErrorType.Unexpected))
            }
        }
    }
}
