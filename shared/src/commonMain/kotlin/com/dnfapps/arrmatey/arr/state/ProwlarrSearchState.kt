package com.dnfapps.arrmatey.arr.state

import com.dnfapps.arrmatey.arr.api.model.ProwlarrSearchResult
import com.dnfapps.arrmatey.client.ErrorType

sealed interface ProwlarrSearchState {
    object Initial: ProwlarrSearchState
    object Loading: ProwlarrSearchState
    data class Success(val items: List<ProwlarrSearchResult>): ProwlarrSearchState
    data class Error(
        val message: String,
        val type: ErrorType = ErrorType.Http
    ): ProwlarrSearchState
}
