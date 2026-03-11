package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GrabProwlarrReleaseUseCase(
    private val instanceManager: InstanceManager
) {
    operator fun invoke(instanceId: Long, guid: String, indexerId: Long): Flow<OperationStatus> = flow {
        emit(OperationStatus.InProgress)

        val repository = instanceManager.getProwlarrRepository(instanceId)
        if (repository == null) {
            emit(OperationStatus.Error(message = "Instance not found"))
            return@flow
        }

        when (val result = repository.grabRelease(guid, indexerId)) {
            is NetworkResult.Success -> emit(OperationStatus.Success())
            is NetworkResult.Error -> emit(
                OperationStatus.Error(
                    code = result.code,
                    message = result.message ?: "Failed to grab release"
                )
            )
            is NetworkResult.Loading -> { /* no-op */ }
        }
    }
}
