package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import kotlinx.coroutines.flow.firstOrNull

class PerformLookupUseCase(
    private val instanceManager: InstanceManager,
) {
    suspend operator fun invoke(
        type: InstanceType,
        query: String,
        instanceId: Long? = null,
    ) {
        val repo =
            if (instanceId != null) {
                instanceManager.getArrRepository(instanceId)
            } else {
                instanceManager.getSelectedArrRepository(type).firstOrNull()
            }
        repo?.performLookup(query)
    }

    suspend fun clear(
        type: InstanceType,
        instanceId: Long? = null,
    ) {
        val repo =
            if (instanceId != null) {
                instanceManager.getArrRepository(instanceId)
            } else {
                instanceManager.getSelectedArrRepository(type).firstOrNull()
            }
        repo?.clearLookup()
    }
}
