package com.dnfapps.arrmatey.instances.usecase

import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.coroutines.flow.Flow

class ObserveSelectedInstanceUseCase(
    private val instanceRepository: InstanceRepository,
) {
    operator fun invoke(type: InstanceType): Flow<Instance?> = instanceRepository.observeSelectedInstance(type)
}
