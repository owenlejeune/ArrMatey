package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeleteAudiobookFileUseCase {
    operator fun invoke(
        audiobookId: Long,
        fileIds: List<Long>,
        repository: ArrInstanceRepository,
    ): Flow<OperationStatus> =
        flow {
            emit(OperationStatus.InProgress)
            repository
                .deleteAudiobookFiles(audiobookId, fileIds)
                .onSuccess {
                    repository.getMediaDetails(audiobookId)
                    emit(OperationStatus.Success("Audiobook file deleted successfully"))
                }.onError { code, message, cause ->
                    emit(OperationStatus.Error(code, message, cause))
                }
        }
}
