package com.dnfapps.arrmatey.downloads.usecase

import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.downloads.api.model.DownloadQueueItem
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class GetDownloadQueueUseCase(
    private val instanceManager: InstanceManager
) {
    operator fun invoke(instanceId: Long): Flow<NetworkResult<List<DownloadQueueItem>>> = flow {
        val repository = instanceManager.getDownloadRepository(instanceId)
            ?: return@flow emit(NetworkResult.Error(message = "Instance not found"))
        
        repository.refresh()
        emitAll(repository.queue)
    }
}
