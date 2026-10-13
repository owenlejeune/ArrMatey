package com.dnfapps.arrmatey.monitoring.state

import com.dnfapps.arrmatey.arr.state.HttpErrorType
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.monitoring.model.PlaybackHistoryItem

sealed interface MonitoringHistoryState {
    data object Initial : MonitoringHistoryState

    data object Loading : MonitoringHistoryState

    data object NoInstance : MonitoringHistoryState

    data class Success(
        val instance: Instance,
        val items: List<PlaybackHistoryItem> = emptyList(),
        val hasMore: Boolean = true,
        val isLoadingMore: Boolean = false,
    ) : MonitoringHistoryState

    data class Error(
        val message: String,
        val type: HttpErrorType = HttpErrorType.Unexpected,
    ) : MonitoringHistoryState
}
