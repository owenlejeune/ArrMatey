package com.dnfapps.arrmatey.arr.state

import com.dnfapps.arrmatey.arr.api.model.ProwlarrIndexer
import com.dnfapps.arrmatey.client.ErrorType

sealed interface IndexersState {
    object Initial: IndexersState
    object Loading: IndexersState
    data class Success(val items: List<ProwlarrIndexer>): IndexersState
    data class Error(
        val message: String,
        val type: ErrorType = ErrorType.Http
    ): IndexersState
}
