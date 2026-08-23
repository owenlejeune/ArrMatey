package com.dnfapps.arrmatey.instances.usecase

import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetArrInstanceRepositoryUseCase(
    private val instanceManager: InstanceManager
) {
    operator fun invoke(instanceId: Long): ArrInstanceRepository? =
        instanceManager.getArrRepository(instanceId)

    fun observeSelected(type: InstanceType): Flow<ArrInstanceRepository?> =
        instanceManager.getSelectedArrRepository(type)
}