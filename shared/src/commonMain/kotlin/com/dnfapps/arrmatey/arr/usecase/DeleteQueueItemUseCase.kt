package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeleteQueueItemUseCase(
    private val instanceManager: InstanceManager,
) {
    operator fun invoke(
        queueItem: QueueItem,
        removeFromClient: Boolean,
        addToBlocklist: Boolean,
        skipRedownload: Boolean,
    ): Flow<OperationStatus> =
        flow {
            val instanceId =
                queueItem.instanceId ?: run {
                    emit(OperationStatus.Error(message = "Queue item is not linked to any instance"))
                    return@flow
                }
            val repository =
                instanceManager.getArrRepository(instanceId) ?: run {
                    emit(OperationStatus.Error(message = "Instance cannot be found"))
                    return@flow
                }

            emit(OperationStatus.InProgress)
            repository
                .deleteActivityTask(
                    queueItem.id,
                    removeFromClient,
                    addToBlocklist,
                    skipRedownload,
                ).onSuccess {
                    emit(OperationStatus.Success("Queue item removed successfully"))
                    delay(100)
                    emit(OperationStatus.Idle)
                }.onError { code, message, cause ->
                    emit(OperationStatus.Error(code, message, cause))
                    delay(100)
                    emit(OperationStatus.Idle)
                }
        }
}
