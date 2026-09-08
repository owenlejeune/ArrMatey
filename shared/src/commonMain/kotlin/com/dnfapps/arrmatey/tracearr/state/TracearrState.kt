package com.dnfapps.arrmatey.tracearr.state

import com.dnfapps.arrmatey.tracearr.api.model.TracearrHistoryItem
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStreamSession
import com.dnfapps.arrmatey.tracearr.api.model.TracearrTodayStats

sealed interface TracearrState {
    data object Initial : TracearrState
    data object Loading : TracearrState
    data object NoInstance : TracearrState
    data class Success(
        val streams: List<TracearrStreamSession>,
        val stats: TracearrTodayStats? = null,
        val history: List<TracearrHistoryItem> = emptyList(),
    ) : TracearrState
    data class Error(
        val message: String,
    ) : TracearrState
}
