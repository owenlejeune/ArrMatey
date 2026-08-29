package com.dnfapps.arrmatey.instances.usecase

import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.database.dao.InsertResult
import com.dnfapps.arrmatey.instances.model.Instance

class CreateInstanceUseCase(
    private val instanceRepository: InstanceRepository,
) {
    suspend operator fun invoke(instance: Instance): InsertResult = instanceRepository.createInstance(instance)
}
