package com.dnfapps.arrmatey.instances.usecase

import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.instances.repository.TracearrRepository
import kotlinx.coroutines.flow.Flow

class GetTracearrInstanceRepositoryUseCase(
    private val instanceManager: InstanceManager,
) {
    operator fun invoke(instanceId: Long): TracearrRepository? =
        instanceManager.getAllTracearrRepositories().find { it.instance.id == instanceId }

    fun observeSelected(): Flow<TracearrRepository?> =
        instanceManager.getSelectedTracearrRepository()
}
