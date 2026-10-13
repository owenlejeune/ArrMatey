package com.dnfapps.arrmatey.instances.usecase

import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.instances.repository.TautulliRepository
import kotlinx.coroutines.flow.Flow

class GetTautulliInstanceRepositoryUseCase(
    private val instanceManager: InstanceManager,
) {
    operator fun invoke(instanceId: Long): TautulliRepository? = instanceManager.getAllTautulliRepositories().find { it.instance.id == instanceId }

    fun observeSelected(): Flow<TautulliRepository?> = instanceManager.getSelectedTautulliRepository()
}
