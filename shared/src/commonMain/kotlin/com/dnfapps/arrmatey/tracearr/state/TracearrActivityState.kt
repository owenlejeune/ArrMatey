package com.dnfapps.arrmatey.tracearr.state

import com.dnfapps.arrmatey.tracearr.api.model.TracearrActivityResponse
import com.dnfapps.arrmatey.tracearr.api.model.TracearrPeriod

sealed interface TracearrActivityState {
    data object Initial : TracearrActivityState

    data object Loading : TracearrActivityState

    data object NoInstance : TracearrActivityState

    data class Error(
        val message: String,
    ) : TracearrActivityState

    data class Success(
        val response: TracearrActivityResponse,
        val period: TracearrPeriod = TracearrPeriod.Month,
    ) : TracearrActivityState
}
