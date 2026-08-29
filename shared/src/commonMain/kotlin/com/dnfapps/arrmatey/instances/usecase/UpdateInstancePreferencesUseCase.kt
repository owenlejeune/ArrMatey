package com.dnfapps.arrmatey.instances.usecase

import com.dnfapps.arrmatey.datastore.InstancePreferenceStoreRepository
import com.dnfapps.arrmatey.datastore.InstancePreferences

class UpdateInstancePreferencesUseCase(
    private val instancePreferencesStoreRepository: InstancePreferenceStoreRepository,
) {
    suspend operator fun invoke(
        instanceId: Long,
        preferences: InstancePreferences,
    ) {
        val preferenceStore = instancePreferencesStoreRepository.getInstancePreferences(instanceId)
        preferenceStore.savePreferences(preferences)
    }
}
