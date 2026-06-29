package com.dnfapps.arrmatey.bazarr.usecase

import com.dnfapps.arrmatey.bazarr.api.model.BazarrMediaType
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.instances.repository.BazarrInstanceRepository

class PerformBazarrAutomaticSearchUseCase {
    suspend operator fun invoke(
        id: Long,
        type: BazarrMediaType,
        repository: BazarrInstanceRepository
    ): NetworkResult<Unit> {
        return when (type) {
            BazarrMediaType.Series -> repository.autoSearchSeriesSubtitles(id)
            BazarrMediaType.Movie -> repository.autoSearchMovieSubtitles(id)
        }
    }
}
