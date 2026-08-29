package com.dnfapps.arrmatey.arr.state

import com.dnfapps.arrmatey.arr.api.model.ProwlarrIndexer

sealed interface ProwlarrIndexersState {
    object Initial : ProwlarrIndexersState

    object Loading : ProwlarrIndexersState

    data class Success(
        val items: List<ProwlarrIndexer>,
    ) : ProwlarrIndexersState

    data class Error(
        val message: String,
        val type: HttpErrorType = HttpErrorType.Http,
    ) : ProwlarrIndexersState
}
