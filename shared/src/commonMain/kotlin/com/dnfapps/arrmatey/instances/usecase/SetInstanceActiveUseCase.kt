package com.dnfapps.arrmatey.instances.usecase

import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType

class SetInstanceActiveUseCase(
    private val instanceRepository: InstanceRepository,
) {
    suspend operator fun invoke(instance: Instance, types: List<InstanceType> = listOf(instance.type)) {
        instanceRepository.setInstanceActive(instance, types)
    }
}
