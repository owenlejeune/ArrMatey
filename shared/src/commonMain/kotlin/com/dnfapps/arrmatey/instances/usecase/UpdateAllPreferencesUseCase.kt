package com.dnfapps.arrmatey.instances.usecase

import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.datastore.InstancePreferences
import kotlinx.coroutines.flow.first

class UpdateAllPreferencesUseCase(
    private val instanceRepository: InstanceRepository,
    private val updateInstancePreferencesUseCase: UpdateInstancePreferencesUseCase
) {
    suspend operator fun invoke(preferences: InstancePreferences) {
        instanceRepository.observeAllInstances().first().forEach { instance ->
            updateInstancePreferencesUseCase(instance.id, preferences)
        }
    }
}
