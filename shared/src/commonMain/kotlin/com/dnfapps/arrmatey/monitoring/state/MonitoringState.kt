package com.dnfapps.arrmatey.monitoring.state

import com.dnfapps.arrmatey.arr.state.HttpErrorType
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.monitoring.model.PlaybackHistoryItem
import com.dnfapps.arrmatey.monitoring.model.PlaybackStreamSession
import com.dnfapps.arrmatey.monitoring.model.PlaybackSummaryStats
import com.dnfapps.arrmatey.tracearr.api.model.TracearrTodayStats

sealed interface MonitoringState {
    data object Initial : MonitoringState

    data object Loading : MonitoringState

    data object NoInstance : MonitoringState

    data class Success(
        val instance: Instance,
        val streams: List<PlaybackStreamSession>,
        val stats: PlaybackSummaryStats? = null,
        val tracearrStats: TracearrTodayStats? = null,
        val history: List<PlaybackHistoryItem> = emptyList(),
    ) : MonitoringState

    data class Error(
        val message: String,
        val type: HttpErrorType = HttpErrorType.Unexpected,
    ) : MonitoringState
}
