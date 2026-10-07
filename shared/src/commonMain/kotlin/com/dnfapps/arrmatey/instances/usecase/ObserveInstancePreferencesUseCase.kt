package com.dnfapps.arrmatey.instances.usecase

import com.dnfapps.arrmatey.compose.utils.SortBy
import com.dnfapps.arrmatey.datastore.InstancePreferenceStoreRepository
import com.dnfapps.arrmatey.datastore.InstancePreferences
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest

class ObserveInstancePreferencesUseCase(
    private val instanceManager: InstanceManager,
    private val preferencesStoreRepository: InstancePreferenceStoreRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(type: InstanceType): Flow<InstancePreferences> = instanceManager
        .getSelectedArrRepository(type)
        .filterNotNull()
        .flatMapLatest {
            preferencesStoreRepository.getInstancePreferences(it.instance.id, SortBy.defaultFor(it.instance.type)).observePreferences()
        }

    operator fun invoke(instanceId: Long): Flow<InstancePreferences> {
        val type = instanceManager.getArrRepository(instanceId)?.instance?.type
        return preferencesStoreRepository.getInstancePreferences(instanceId, SortBy.defaultFor(type)).observePreferences()
    }
}
