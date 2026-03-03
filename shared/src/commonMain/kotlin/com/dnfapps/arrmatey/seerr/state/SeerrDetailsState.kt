package com.dnfapps.arrmatey.seerr.state

import com.dnfapps.arrmatey.client.ErrorType
import com.dnfapps.arrmatey.seerr.api.model.RequestMediaDetails

sealed interface SeerrDetailsState {
    object Initial: SeerrDetailsState
    object Loading: SeerrDetailsState
    data class Error(
        val errorType: ErrorType,
        val message: String?
    ): SeerrDetailsState
    data class Success(
        val item: RequestMediaDetails
    ): SeerrDetailsState
}