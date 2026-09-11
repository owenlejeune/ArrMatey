package com.dnfapps.arrmatey.tracearr.state

import com.dnfapps.arrmatey.tracearr.api.model.TracearrHistoryItem
import com.dnfapps.arrmatey.tracearr.api.model.TracearrUserDetail
import com.dnfapps.arrmatey.tracearr.api.model.TracearrUserStats

sealed class TracearrUserState {
    data object Initial : TracearrUserState()

    data object NoInstance : TracearrUserState()

    data object Loading : TracearrUserState()

    data class Error(
        val message: String,
    ) : TracearrUserState()

    data class Success(
        val userDetail: TracearrUserDetail?,
        val userStats: TracearrUserStats?,
        val history: List<TracearrHistoryItem>,
        val isLoadingMoreHistory: Boolean = false,
        val hasMoreHistory: Boolean = false,
        val nextCursor: String? = null,
    ) : TracearrUserState()
}
