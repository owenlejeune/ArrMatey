package com.dnfapps.arrmatey.instances.usecase

import com.dnfapps.arrmatey.instances.repository.BazarrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import kotlinx.coroutines.flow.Flow

class GetBazarrInstanceRepositoryUseCase(
    private val instanceManager: InstanceManager
) {
    operator fun invoke(instanceId: Long): BazarrInstanceRepository? =
        instanceManager.getBazarrRepository(instanceId)

    fun observeSelected(): Flow<BazarrInstanceRepository?> =
        instanceManager.getSelectedBazarrRepository()
}
