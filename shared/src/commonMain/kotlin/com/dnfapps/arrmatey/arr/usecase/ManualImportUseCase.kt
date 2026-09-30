package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.ManualImportFile
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ManualImportUseCase(
    private val instanceManager: InstanceManager,
) {
    suspend fun getImportableFiles(queueItem: QueueItem): NetworkResult<List<ManualImportFile>> {
        val instanceId = queueItem.instanceId ?: return NetworkResult.Error(message = "Queue item is not linked to any instance")
        val repository = instanceManager.getArrRepository(instanceId) ?: return NetworkResult.Error(message = "Instance cannot be found")
        return repository.getManualImportFiles(
            downloadId = queueItem.downloadId,
            folder = queueItem.outputPath,
        )
    }

    fun executeManualImport(
        queueItem: QueueItem,
        selectedFiles: List<ManualImportFile>,
    ): Flow<OperationStatus> = flow {
        val instanceId = queueItem.instanceId ?: run {
            emit(OperationStatus.Error(message = "Queue item is not linked to any instance"))
            return@flow
        }
        val repository = instanceManager.getArrRepository(instanceId) ?: run {
            emit(OperationStatus.Error(message = "Instance cannot be found"))
            return@flow
        }

        emit(OperationStatus.InProgress)
        repository.executeManualImport(selectedFiles)
            .onSuccess {
                emit(OperationStatus.Success("Import queued successfully"))
            }
            .onError { code, message, cause ->
                emit(OperationStatus.Error(code, message, cause))
            }
    }
}
