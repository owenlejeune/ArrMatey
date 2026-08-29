package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeleteMovieFileUseCase {
    operator fun invoke(
        movieId: Long,
        repository: ArrInstanceRepository,
    ): Flow<OperationStatus> =
        flow {
            emit(OperationStatus.InProgress)
            repository
                .deleteMovieFile(movieId)
                .onSuccess {
                    repository.getMediaDetails(movieId)
                    repository.getMovieExtraFiles(movieId)
                    emit(OperationStatus.Success("Movie file deleted successfully"))
                }.onError { code, message, cause ->
                    emit(OperationStatus.Error(code, message, cause))
                }
        }
}
