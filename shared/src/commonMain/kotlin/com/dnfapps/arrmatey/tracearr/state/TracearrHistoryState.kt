package com.dnfapps.arrmatey.tracearr.state

import com.dnfapps.arrmatey.tracearr.api.model.TracearrHistoryItem
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStreamSession

sealed interface TracearrHistoryState {
    data object Initial : TracearrHistoryState

    data object Loading : TracearrHistoryState

    data object NoInstance : TracearrHistoryState

    data class Success(
        val activeStreams: List<TracearrStreamSession> = emptyList(),
        val items: List<TracearrHistoryItem> = emptyList(),
        val isLoadingMore: Boolean = false,
        val hasMore: Boolean = false,
        val nextCursor: String? = null,
    ) : TracearrHistoryState

    data class Error(
        val message: String,
    ) : TracearrHistoryState
}
