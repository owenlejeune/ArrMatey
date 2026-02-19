package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.ProwlarrIndexer
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.instances.repository.InstanceManager

class GetProwlarrIndexersUseCase(
    private val instanceManager: InstanceManager
) {
    suspend operator fun invoke(instanceId: Long): NetworkResult<List<ProwlarrIndexer>> {
        val repository = instanceManager.getRepository(instanceId)
            ?: return NetworkResult.Error(message = "Instance not found")
        
        return repository.prowlarrClient.getIndexers()
    }
}
