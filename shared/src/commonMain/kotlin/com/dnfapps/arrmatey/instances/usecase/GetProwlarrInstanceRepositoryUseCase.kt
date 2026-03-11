package com.dnfapps.arrmatey.instances.usecase

import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.instances.repository.ProwlarrInstanceRepository
import kotlinx.coroutines.flow.Flow

class GetProwlarrInstanceRepositoryUseCase(
    private val instanceManager: InstanceManager
) {
    operator fun invoke(instanceId: Long): ProwlarrInstanceRepository? =
        instanceManager.getProwlarrRepository(instanceId)

    fun observeSelected(): Flow<ProwlarrInstanceRepository?> =
        instanceManager.getSelectedProwlarrRepository()
}