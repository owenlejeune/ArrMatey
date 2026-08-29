package com.dnfapps.arrmatey.instances.usecase

import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import kotlinx.coroutines.flow.Flow

class GetArrInstanceRepositoryUseCase(
    private val instanceManager: InstanceManager,
) {
    operator fun invoke(instanceId: Long): ArrInstanceRepository? = instanceManager.getArrRepository(instanceId)

    fun observeSelected(type: InstanceType): Flow<ArrInstanceRepository?> = instanceManager.getSelectedArrRepository(type)
}
