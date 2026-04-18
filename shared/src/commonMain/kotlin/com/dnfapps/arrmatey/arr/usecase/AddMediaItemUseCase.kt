package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

class AddMediaItemUseCase(
    private val instanceManager: InstanceManager
) {
    operator fun invoke(
        instanceType: InstanceType,
        item: ArrMedia,
        searchOnAdd: Boolean
    ): Flow<OperationStatus> = flow {
        val repository = instanceManager.getSelectedArrRepository(instanceType)
            .firstOrNull()

        if (repository == null) {
            emit(OperationStatus.Error(message = "Instance not found"))
            return@flow
        }

        repository.addItem(item, searchOnAdd)

        repository.addItemStatus.collect { status ->
            emit(status)
            delay(100)
            emit(OperationStatus.Idle)
        }
    }
}