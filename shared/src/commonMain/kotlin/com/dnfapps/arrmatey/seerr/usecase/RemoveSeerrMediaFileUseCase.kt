package com.dnfapps.arrmatey.seerr.usecase

import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.client.onError
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RemoveSeerrMediaFileUseCase {
    operator fun invoke(
        mediaId: Long,
        is4k: Boolean,
        repository: SeerrInstanceRepository
    ): Flow<OperationStatus> = flow {
        emit(OperationStatus.InProgress)
        repository.deleteMediaFile(mediaId, is4k)
            .onSuccess { emit(OperationStatus.Success()) }
            .onError { code, message, cause ->
                emit(OperationStatus.Error(code, message, cause))
            }
    }
}