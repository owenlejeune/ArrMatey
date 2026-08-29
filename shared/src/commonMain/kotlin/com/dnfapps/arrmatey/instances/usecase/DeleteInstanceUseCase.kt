package com.dnfapps.arrmatey.instances.usecase

import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.instances.model.Instance

class DeleteInstanceUseCase(
    private val instanceRepository: InstanceRepository,
) {
    suspend operator fun invoke(instance: Instance) {
        instanceRepository.deleteInstance(instance)
    }
}
