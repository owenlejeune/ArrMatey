package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.state.ProwlarrIndexersState
import com.dnfapps.arrmatey.client.ErrorType
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetProwlarrIndexersUseCase(
    private val instanceManager: InstanceManager
) {
    operator fun invoke(instanceId: Long): Flow<ProwlarrIndexersState> = flow {
        emit(ProwlarrIndexersState.Loading)

        val repository = instanceManager.getProwlarrRepository(instanceId)
        if (repository == null) {
            emit(ProwlarrIndexersState.Error("Instance not found", ErrorType.Unexpected))
            return@flow
        }

        when (val result = repository.getIndexers()) {
            is NetworkResult.Success -> emit(ProwlarrIndexersState.Success(result.data))
            is NetworkResult.Error -> emit(
                ProwlarrIndexersState.Error(
                    message = result.message ?: "Failed to fetch indexers",
                    type = if (result.code == null) ErrorType.Network else ErrorType.Http
                )
            )
            is NetworkResult.Loading -> emit(ProwlarrIndexersState.Loading)
        }
    }
}
