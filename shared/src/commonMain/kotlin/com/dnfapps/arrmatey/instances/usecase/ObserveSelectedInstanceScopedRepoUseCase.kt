package com.dnfapps.arrmatey.instances.usecase

import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import kotlinx.coroutines.flow.Flow

class ObserveSelectedInstanceScopedRepoUseCase(
    private val instanceManager: InstanceManager,
) {
    operator fun invoke(type: InstanceType): Flow<ArrInstanceRepository?> = instanceManager.getSelectedArrRepository(type)
}
