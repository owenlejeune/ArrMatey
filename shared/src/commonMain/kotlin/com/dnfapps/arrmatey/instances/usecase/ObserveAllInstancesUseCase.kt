package com.dnfapps.arrmatey.instances.usecase

import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.instances.model.Instance
import kotlinx.coroutines.flow.Flow

class ObserveAllInstancesUseCase(
    private val instanceRepository: InstanceRepository
) {
    operator fun invoke(): Flow<List<Instance>> =
        instanceRepository.observeAllInstances()
}
