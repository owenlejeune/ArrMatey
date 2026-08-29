package com.dnfapps.arrmatey.seerr.state

import com.dnfapps.arrmatey.arr.state.HttpErrorType

sealed interface RequestsState {
    object Initial : RequestsState

    object Loading : RequestsState

    data class Success(
        val items: List<Any>,
    ) : RequestsState

    data class Error(
        val message: String?,
        val type: HttpErrorType = HttpErrorType.Http,
    ) : RequestsState
}
