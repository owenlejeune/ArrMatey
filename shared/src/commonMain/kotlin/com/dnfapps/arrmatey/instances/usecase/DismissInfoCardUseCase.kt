package com.dnfapps.arrmatey.instances.usecase

import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.instances.model.InstanceType

class DismissInfoCardUseCase(
    private val preferenceStore: PreferencesStore,
) {
    operator fun invoke(type: InstanceType) {
        preferenceStore.dismissInfoCard(type)
    }
}
