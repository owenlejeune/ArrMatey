package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeleteBookFilesUseCase {
    operator fun invoke(
        bookFileIds: List<Long>,
        repository: ArrInstanceRepository,
    ): Flow<OperationStatus> =
        flow {
            emit(OperationStatus.InProgress)
            repository
                .deleteBookFiles(bookFileIds)
                .onSuccess {
                    emit(OperationStatus.Success("Books deleted successfully"))
                }.onError { code, message, cause ->
                    emit(OperationStatus.Error(code, message, cause))
                }
        }
}
