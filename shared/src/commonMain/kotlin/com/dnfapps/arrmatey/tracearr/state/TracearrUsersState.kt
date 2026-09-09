package com.dnfapps.arrmatey.tracearr.state

import com.dnfapps.arrmatey.tracearr.api.model.TracearrUserDetail
import com.dnfapps.arrmatey.tracearr.api.model.TracearrUserStats

sealed class TracearrUsersState {
    data object Initial : TracearrUsersState()
    data object NoInstance : TracearrUsersState()
    data object Loading : TracearrUsersState()
    data class Error(val message: String) : TracearrUsersState()
    data class Success(
        val users: List<TracearrUserDetail>,
        val filteredUsers: List<TracearrUserDetail> = users,
        val userStatsMap: Map<String, TracearrUserStats> = emptyMap(),
        val searchQuery: String = "",
        val isLoadingMore: Boolean = false,
        val hasMore: Boolean = false,
        val nextCursor: String? = null,
    ) : TracearrUsersState()
}
