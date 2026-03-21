package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.ProwlarrIndexer
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TestProwlarrIndexerUseCase(
    private val instanceManager: InstanceManager
) {
    operator fun invoke(instanceId: Long, indexer: ProwlarrIndexer): Flow<OperationStatus> = flow {
        emit(OperationStatus.InProgress)

        val repository = instanceManager.getProwlarrRepository(instanceId)
        if (repository == null) {
            emit(OperationStatus.Error(message = "Instance not found"))
            return@flow
        }

        when (val result = repository.testIndexer(indexer)) {
            is NetworkResult.Success -> emit(OperationStatus.Success())
            is NetworkResult.Error -> emit(
                OperationStatus.Error(
                    message = result.message ?: "Indexer test failed"
                )
            )
            is NetworkResult.Loading -> emit(OperationStatus.InProgress)
        }
    }
}
