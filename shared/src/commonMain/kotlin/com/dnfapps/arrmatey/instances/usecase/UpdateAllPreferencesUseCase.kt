package com.dnfapps.arrmatey.instances.usecase

import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.datastore.InstancePreferenceStoreRepository
import com.dnfapps.arrmatey.datastore.InstancePreferences
import kotlinx.coroutines.flow.first

class UpdateAllPreferencesUseCase(
    private val instanceRepository: InstanceRepository,
    private val instancePreferenceStoreRepository: InstancePreferenceStoreRepository,
) {
    suspend operator fun invoke(preferences: InstancePreferences) {
        instanceRepository.observeAllInstances().first().forEach { instance ->
            val preferenceStore = instancePreferenceStoreRepository.getInstancePreferences(instance.id)
            val current = preferenceStore.observePreferences().first()
            val updated =
                current.copy(
                    viewType = preferences.viewType,
                    posterElevation = preferences.posterElevation,
                    posterRadius = preferences.posterRadius,
                    showFullDetails = preferences.showFullDetails,
                    showOverlay = preferences.showOverlay,
                    gridDensity = preferences.gridDensity,
                    gridSpacing = preferences.gridSpacing,
                    showBannerBackground = preferences.showBannerBackground,
                    includeOverview = preferences.includeOverview,
                    bannerBlur = preferences.bannerBlur,
                    applyGlobally = preferences.applyGlobally,
                    deleteDeleteFiles = preferences.deleteDeleteFiles,
                    deleteAddExclusion = preferences.deleteAddExclusion,
                )
            preferenceStore.savePreferences(updated)
        }
    }
}
