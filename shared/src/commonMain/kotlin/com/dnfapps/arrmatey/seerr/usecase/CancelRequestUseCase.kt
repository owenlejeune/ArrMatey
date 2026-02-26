package com.dnfapps.arrmatey.seerr.usecase

import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.client.onError
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CancelRequestUseCase {
    operator fun invoke(requestId: Long, repository: SeerrInstanceRepository): Flow<OperationStatus> = flow {
        emit(OperationStatus.InProgress)
        repository.deleteRequest(requestId)
            .onSuccess {
                emit(OperationStatus.Success())
            }
            .onError { code, message, cause ->
                emit(OperationStatus.Error(code, message, cause))
            }
    }
}