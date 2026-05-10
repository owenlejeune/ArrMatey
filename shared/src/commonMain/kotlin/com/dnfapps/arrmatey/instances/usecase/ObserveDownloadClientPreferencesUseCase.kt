package com.dnfapps.arrmatey.instances.usecase

import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.downloadclient.state.DownloadQueueSortState
import kotlinx.coroutines.flow.Flow

class ObserveDownloadClientPreferencesUseCase(
    private val preferencesStore: PreferencesStore
) {
    operator fun invoke(): Flow<DownloadQueueSortState> =
        preferencesStore.observeDownloadClientUiState()
}