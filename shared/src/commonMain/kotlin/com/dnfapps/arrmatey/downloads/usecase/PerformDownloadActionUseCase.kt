package com.dnfapps.arrmatey.downloads.usecase

import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.instances.repository.InstanceManager

class PerformDownloadActionUseCase(
    private val instanceManager: InstanceManager
) {
    suspend fun pause(instanceId: Long, itemId: String): NetworkResult<Unit> {
        val repository = instanceManager.getDownloadRepository(instanceId)
            ?: return NetworkResult.Error(message = "Instance not found")
        return repository.pause(itemId)
    }

    suspend fun resume(instanceId: Long, itemId: String): NetworkResult<Unit> {
        val repository = instanceManager.getDownloadRepository(instanceId)
            ?: return NetworkResult.Error(message = "Instance not found")
        return repository.resume(itemId)
    }

    suspend fun delete(instanceId: Long, itemId: String, deleteFiles: Boolean): NetworkResult<Unit> {
        val repository = instanceManager.getDownloadRepository(instanceId)
            ?: return NetworkResult.Error(message = "Instance not found")
        return repository.delete(itemId, deleteFiles)
    }
}
