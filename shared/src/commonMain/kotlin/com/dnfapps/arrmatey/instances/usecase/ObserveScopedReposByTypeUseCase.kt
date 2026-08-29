package com.dnfapps.arrmatey.instances.usecase

import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.instances.repository.InstanceScopedRepository
import kotlinx.coroutines.flow.Flow

class ObserveScopedReposByTypeUseCase(
    private val instanceManager: InstanceManager,
) {
    operator fun invoke(type: InstanceType): Flow<List<InstanceScopedRepository>> = instanceManager.repositoriesByType(type)
}
