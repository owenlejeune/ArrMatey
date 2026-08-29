package com.dnfapps.arrmatey.bazarr.usecase

import com.dnfapps.arrmatey.bazarr.api.model.BazarrMediaType
import com.dnfapps.arrmatey.instances.repository.BazarrInstanceRepository
import com.dnfapps.networking.NetworkResult

class PerformBazarrAutomaticSearchUseCase {
    suspend operator fun invoke(
        id: Long,
        type: BazarrMediaType,
        repository: BazarrInstanceRepository,
    ): NetworkResult<Unit> =
        when (type) {
            BazarrMediaType.Series -> repository.autoSearchSeriesSubtitles(id)
            BazarrMediaType.Movie -> repository.autoSearchMovieSubtitles(id)
        }
}
