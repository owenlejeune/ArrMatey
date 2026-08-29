package com.dnfapps.arrmatey.instances.usecase

import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.instances.model.Instance

class UpdateInstanceUseCase(
    private val instanceRepository: InstanceRepository,
) {
    suspend operator fun invoke(instance: Instance) = instanceRepository.updateInstance(instance)
}
