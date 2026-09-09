package com.dnfapps.arrmatey.tracearr.usecase

import com.dnfapps.arrmatey.instances.repository.TracearrRepository
import com.dnfapps.arrmatey.tracearr.api.model.TracearrUserStats
import com.dnfapps.networking.NetworkResult

class GetUserStatsUseCase {
    suspend operator fun invoke(
        repository: TracearrRepository,
        ref: String,
    ): NetworkResult<TracearrUserStats> =
        repository.getUserStats(ref)
}
