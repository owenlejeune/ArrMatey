package com.dnfapps.arrmatey.seerr.usecase

import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.seerr.api.model.Genre
import com.dnfapps.networking.NetworkResult

class GetGenresUseCase {
    suspend fun getMovieGenres(repository: SeerrInstanceRepository): NetworkResult<List<Genre>> = repository.getMovieGenres()

    suspend fun getTvGenres(repository: SeerrInstanceRepository): NetworkResult<List<Genre>> = repository.getTvGenres()
}
