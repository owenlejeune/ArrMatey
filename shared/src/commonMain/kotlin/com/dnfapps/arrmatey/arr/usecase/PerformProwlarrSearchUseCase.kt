package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.state.ProwlarrSearchState
import com.dnfapps.arrmatey.client.ErrorType
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PerformProwlarrSearchUseCase(
    private val instanceManager: InstanceManager
) {
    operator fun invoke(
        instanceId: Long,
        query: String,
        categories: List<Int> = emptyList(),
        indexerIds: List<Long> = emptyList()
    ): Flow<ProwlarrSearchState> = flow {
        if (query.isBlank()) {
            emit(ProwlarrSearchState.Initial)
            return@flow
        }

        val repository = instanceManager.getProwlarrRepository(instanceId)
        if (repository == null) {
            emit(ProwlarrSearchState.Error("Instance not found", ErrorType.Unexpected))
            return@flow
        }

        emit(ProwlarrSearchState.Loading)

        when (val result = repository.search(
            query = query,
            categories = categories,
            indexerIds = indexerIds
        )) {
            is NetworkResult.Success -> emit(ProwlarrSearchState.Success(result.data))
            is NetworkResult.Error -> emit(
                ProwlarrSearchState.Error(
                    message = result.message ?: "Failed to load search results",
                    type = if (result.code == null) ErrorType.Network else ErrorType.Http
                )
            )
            is NetworkResult.Loading -> emit(ProwlarrSearchState.Loading)
        }
    }
}
