package com.dnfapps.arrmatey.seerr.state

import com.dnfapps.arrmatey.arr.state.HttpErrorType
import com.dnfapps.arrmatey.seerr.api.model.ImdbRating
import com.dnfapps.arrmatey.seerr.api.model.RequestMediaDetails
import com.dnfapps.arrmatey.seerr.api.model.RottenTomatoesRating

sealed interface SeerrDetailsState {
    object Initial : SeerrDetailsState

    object Loading : SeerrDetailsState

    data class Error(
        val errorType: HttpErrorType,
        val message: String?,
    ) : SeerrDetailsState

    data class Success(
        val item: RequestMediaDetails,
        val rtRatings: RottenTomatoesRating? = null,
        val imdbRatings: ImdbRating? = null,
    ) : SeerrDetailsState
}
