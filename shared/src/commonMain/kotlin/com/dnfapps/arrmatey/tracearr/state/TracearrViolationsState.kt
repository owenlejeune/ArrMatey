package com.dnfapps.arrmatey.tracearr.state

import com.dnfapps.arrmatey.tracearr.api.model.TracearrViolation

sealed class TracearrViolationsState {
    data object Initial : TracearrViolationsState()
    data object NoInstance : TracearrViolationsState()
    data object Loading : TracearrViolationsState()
    data class Error(val message: String) : TracearrViolationsState()
    data class Success(
        val violations: List<TracearrViolation>,
        val filteredViolations: List<TracearrViolation> = violations,
        val searchQuery: String = "",
        val isLoadingMore: Boolean = false,
        val hasMore: Boolean = false,
        val page: Int = 1,
        val total: Int = 0,
    ) : TracearrViolationsState()
}
