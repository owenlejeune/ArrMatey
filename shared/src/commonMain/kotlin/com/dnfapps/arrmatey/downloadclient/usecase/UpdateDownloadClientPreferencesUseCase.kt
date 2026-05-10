package com.dnfapps.arrmatey.downloadclient.usecase

import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.downloadclient.state.DownloadQueueSortState

class UpdateDownloadClientPreferencesUseCase(
    private val preferencesStore: PreferencesStore
) {
    suspend operator fun invoke(uiState: DownloadQueueSortState) {
        preferencesStore.saveDownloadClientUiState(uiState)
    }
}