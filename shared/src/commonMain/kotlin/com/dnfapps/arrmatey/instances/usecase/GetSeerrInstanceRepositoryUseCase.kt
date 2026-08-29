package com.dnfapps.arrmatey.instances.usecase

import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import kotlinx.coroutines.flow.Flow

class GetSeerrInstanceRepositoryUseCase(
    private val instanceManager: InstanceManager,
) {
    operator fun invoke(instanceId: Long): SeerrInstanceRepository? = instanceManager.getSeerrRepository(instanceId)

    fun observeSelected(): Flow<SeerrInstanceRepository?> = instanceManager.getSelectedSeerrRepository()
}
