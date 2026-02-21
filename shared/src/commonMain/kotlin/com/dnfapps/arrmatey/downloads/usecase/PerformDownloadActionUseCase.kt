package com.dnfapps.arrmatey.downloads.usecase

import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.instances.repository.InstanceManager

class PerformDownloadActionUseCase(
    private val instanceManager: InstanceManager
) {
    suspend fun pause(instanceId: Long, itemId: String): NetworkResult<Unit> {
        val repository = instanceManager.getDownloadRepository(instanceId)
            ?: return NetworkResult.Error(message = "Instance not found")
        val result = repository.pause(itemId)
        if (result is NetworkResult.Success) repository.refresh()
        return result
    }

    suspend fun resume(instanceId: Long, itemId: String): NetworkResult<Unit> {
        val repository = instanceManager.getDownloadRepository(instanceId)
            ?: return NetworkResult.Error(message = "Instance not found")
        val result = repository.resume(itemId)
        if (result is NetworkResult.Success) repository.refresh()
        return result
    }

    suspend fun delete(instanceId: Long, itemId: String, deleteFiles: Boolean): NetworkResult<Unit> {
        val repository = instanceManager.getDownloadRepository(instanceId)
            ?: return NetworkResult.Error(message = "Instance not found")
        val result = repository.delete(itemId, deleteFiles)
        if (result is NetworkResult.Success) repository.refresh()
        return result
    }
}
