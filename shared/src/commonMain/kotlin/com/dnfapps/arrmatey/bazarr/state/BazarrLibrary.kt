package com.dnfapps.arrmatey.bazarr.state

import com.dnfapps.arrmatey.bazarr.api.model.BazarrMovie
import com.dnfapps.arrmatey.bazarr.api.model.BazarrSeries
import com.dnfapps.arrmatey.bazarr.api.model.ProviderStatus
import com.dnfapps.arrmatey.bazarr.api.model.WantedEpisode
import com.dnfapps.arrmatey.bazarr.api.model.WantedMovie
import com.dnfapps.arrmatey.client.ErrorType

sealed interface BazarrLibrary {
    data object Initial: BazarrLibrary
    data object Loading: BazarrLibrary
    data class Success(
        val series: List<BazarrSeries> = emptyList(),
        val movies: List<BazarrMovie> = emptyList(),
        val wantedEpisodes: List<WantedEpisode> = emptyList(),
        val wantedMovies: List<WantedMovie> = emptyList(),
        val providers: List<ProviderStatus> = emptyList()
    ): BazarrLibrary
    data class Error(
        val message: String,
        val type: ErrorType = ErrorType.Http
    ): BazarrLibrary
}
