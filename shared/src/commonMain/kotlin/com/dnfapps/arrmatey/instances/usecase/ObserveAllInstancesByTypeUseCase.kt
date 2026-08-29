package com.dnfapps.arrmatey.instances.usecase

import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.coroutines.flow.Flow

class ObserveAllInstancesByTypeUseCase(
    private val instanceRepository: InstanceRepository,
) {
    operator fun invoke(type: InstanceType): Flow<List<Instance>> = instanceRepository.observeInstancesByType(type)
}
