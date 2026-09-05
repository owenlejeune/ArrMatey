package com.dnfapps.arrmatey.tracearr.state

import com.dnfapps.arrmatey.tracearr.api.model.TracearrStreamSession

sealed interface TracearrStreamsState {
    data object Initial : TracearrStreamsState
    data object Loading : TracearrStreamsState
    data object NoInstance : TracearrStreamsState
    data class Success(
        val streams: List<TracearrStreamSession>,
    ) : TracearrStreamsState
    data class Error(
        val message: String,
    ) : TracearrStreamsState
}
