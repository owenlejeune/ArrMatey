package com.dnfapps.arrmatey.arr.state

import com.dnfapps.arrmatey.arr.api.model.ProwlarrIndexer
import com.dnfapps.arrmatey.client.ErrorType

sealed interface ProwlarrIndexersState {
    object Initial: ProwlarrIndexersState
    object Loading: ProwlarrIndexersState
    data class Success(val items: List<ProwlarrIndexer>): ProwlarrIndexersState
    data class Error(
        val message: String,
        val type: ErrorType = ErrorType.Http
    ): ProwlarrIndexersState
}
