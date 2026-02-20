package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.state.IndexersState
import com.dnfapps.arrmatey.client.ErrorType
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetProwlarrIndexersUseCase(
    private val instanceManager: InstanceManager
) {
    operator fun invoke(instanceId: Long): Flow<IndexersState> = flow {
        emit(IndexersState.Loading)

        val repository = instanceManager.getRepository(instanceId)
            ?: return@flow emit(IndexersState.Error("Instance not found", ErrorType.Unexpected))

        when (val result = repository.prowlarrClient.getIndexers()) {
            is NetworkResult.Success -> emit(IndexersState.Success(result.data))
            is NetworkResult.Error -> emit(
                IndexersState.Error(
                    message = result.message ?: "Failed to fetch indexers",
                    type = if (result.code == null) ErrorType.Network else ErrorType.Http
                )
            )
            is NetworkResult.Loading -> emit(IndexersState.Loading)
        }
    }
}
