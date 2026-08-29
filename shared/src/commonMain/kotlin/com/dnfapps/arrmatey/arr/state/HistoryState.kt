package com.dnfapps.arrmatey.arr.state

import com.dnfapps.arrmatey.arr.api.model.HistoryItem

sealed interface HistoryState {
    object Initial : HistoryState

    object Loading : HistoryState

    data class Success(
        val items: List<HistoryItem>,
    ) : HistoryState

    data class Error(
        val message: String?,
    ) : HistoryState
}
