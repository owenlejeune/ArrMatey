package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.ProwlarrSearchResult
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.instances.repository.InstanceManager

class PerformProwlarrSearchUseCase(
    private val instanceManager: InstanceManager
) {
    suspend operator fun invoke(
        instanceId: Long,
        query: String,
        categories: List<Int> = emptyList(),
        indexerIds: List<Long> = emptyList()
    ): NetworkResult<List<ProwlarrSearchResult>> {
        val repository = instanceManager.getRepository(instanceId)
            ?: return NetworkResult.Error(message = "Instance not found")

        return repository.prowlarrClient.search(
            query = query,
            categories = categories,
            indexerIds = indexerIds
        )
    }
}
