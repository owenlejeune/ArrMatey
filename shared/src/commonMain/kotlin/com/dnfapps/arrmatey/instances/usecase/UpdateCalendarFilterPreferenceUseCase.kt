package com.dnfapps.arrmatey.instances.usecase

import com.dnfapps.arrmatey.arr.state.CalendarFilterState
import com.dnfapps.arrmatey.datastore.PreferencesStore

class UpdateCalendarFilterPreferenceUseCase(
    private val preferencesStore: PreferencesStore,
) {
    suspend operator fun invoke(filterState: CalendarFilterState) {
        preferencesStore.saveCalendarFilterState(filterState)
    }
}
