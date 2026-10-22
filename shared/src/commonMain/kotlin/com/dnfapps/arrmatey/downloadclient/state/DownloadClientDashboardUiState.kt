package com.dnfapps.arrmatey.downloadclient.state

import com.dnfapps.arrmatey.arr.state.HttpErrorType
import com.dnfapps.arrmatey.downloadclient.model.DownloadClientSystemOverview

sealed interface DownloadClientDashboardUiState {
    object Initial : DownloadClientDashboardUiState

    object Loading : DownloadClientDashboardUiState

    data class Error(
        val type: HttpErrorType,
        val message: String?,
    ) : DownloadClientDashboardUiState

    data class Success(
        val overview: DownloadClientSystemOverview,
        val isRefreshing: Boolean = false,
    ) : DownloadClientDashboardUiState
}
