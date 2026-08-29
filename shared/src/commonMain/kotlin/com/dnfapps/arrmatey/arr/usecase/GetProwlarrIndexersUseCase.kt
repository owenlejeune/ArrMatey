package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.state.HttpErrorType
import com.dnfapps.arrmatey.arr.state.ProwlarrIndexersState
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.networking.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetProwlarrIndexersUseCase(
    private val instanceManager: InstanceManager,
) {
    operator fun invoke(instanceId: Long): Flow<ProwlarrIndexersState> =
        flow {
            emit(ProwlarrIndexersState.Loading)

            val repository = instanceManager.getProwlarrRepository(instanceId)
            if (repository == null) {
                emit(ProwlarrIndexersState.Error("Instance not found", HttpErrorType.Unexpected))
                return@flow
            }

            when (val result = repository.getIndexers()) {
                is NetworkResult.Success -> emit(ProwlarrIndexersState.Success(result.data))
                is NetworkResult.Error ->
                    emit(
                        ProwlarrIndexersState.Error(
                            message =
                                result.message ?: result.cause?.let {
                                    "${it::class.simpleName}: ${it.message}"
                                } ?: "Failed to fetch indexers",
                            type = if (result.code == null) HttpErrorType.Network else HttpErrorType.Http,
                        ),
                    )
                is NetworkResult.Loading -> emit(ProwlarrIndexersState.Loading)
            }
        }
}
