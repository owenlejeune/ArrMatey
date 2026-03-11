package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.instances.repository.InstanceManager

class GetProwlarrIndexersStatusUseCase(
    private val instanceManager: InstanceManager
) {
    suspend operator fun invoke(instanceId: Long) {
        val repository = instanceManager.getProwlarrRepository(instanceId)
        repository?.getIndexerStatus()
    }
}