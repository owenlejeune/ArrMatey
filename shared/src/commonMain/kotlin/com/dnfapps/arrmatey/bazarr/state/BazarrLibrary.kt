package com.dnfapps.arrmatey.bazarr.state

import com.dnfapps.arrmatey.bazarr.api.model.BazarrMovie
import com.dnfapps.arrmatey.bazarr.api.model.BazarrSeries
import com.dnfapps.arrmatey.client.ErrorType

sealed interface BazarrLibrary {
    object Initial: BazarrLibrary
    object Loading: BazarrLibrary
    data class Success(
        val series: List<BazarrSeries>,
        val movies: List<BazarrMovie>
    ): BazarrLibrary
    data class Error(
        val message: String,
        val type: ErrorType = ErrorType.Http
    ): BazarrLibrary
}