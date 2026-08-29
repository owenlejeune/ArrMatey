package com.dnfapps.arrmatey.instances.usecase

import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.instances.model.Instance

class GetInstanceByIdUseCase(
    private val instanceRepository: InstanceRepository,
) {
    suspend operator fun invoke(instanceId: Long): Instance? = instanceRepository.getInstanceById(instanceId)
}
