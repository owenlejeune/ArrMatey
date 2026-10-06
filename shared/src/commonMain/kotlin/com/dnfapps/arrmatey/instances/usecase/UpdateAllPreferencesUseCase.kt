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

    suspend fun findGlobalPreferences(): InstancePreferences? {
        val allInstances = instanceRepository.observeAllInstances().first()
        return allInstances.firstNotNullOfOrNull { instance ->
            val prefs = instancePreferenceStoreRepository.getInstancePreferences(instance.id).observePreferences().first()
            if (prefs.applyGlobally) prefs else null
        }
    }

    suspend fun syncGlobalPreferencesToInstance(targetInstanceId: Long) {
        val global = findGlobalPreferences() ?: return
        val targetStore = instancePreferenceStoreRepository.getInstancePreferences(targetInstanceId)
        val current = targetStore.observePreferences().first()
        if (!current.applyGlobally) {
            val updated = current.copy(
                viewType = global.viewType,
                posterElevation = global.posterElevation,
                posterRadius = global.posterRadius,
                showFullDetails = global.showFullDetails,
                showOverlay = global.showOverlay,
                gridDensity = global.gridDensity,
                gridSpacing = global.gridSpacing,
                showBannerBackground = global.showBannerBackground,
                includeOverview = global.includeOverview,
                bannerBlur = global.bannerBlur,
                applyGlobally = global.applyGlobally,
                deleteDeleteFiles = global.deleteDeleteFiles,
                deleteAddExclusion = global.deleteAddExclusion,
            )
            targetStore.savePreferences(updated)
        }
    }
}
