package com.dnfapps.arrmatey.tracearr.usecase

import com.dnfapps.arrmatey.instances.repository.TracearrRepository
import com.dnfapps.arrmatey.tracearr.api.model.TracearrTodayStats
import com.dnfapps.networking.NetworkResult

class GetTracearrStatsTodayUseCase {
    suspend operator fun invoke(repository: TracearrRepository): NetworkResult<TracearrTodayStats> =
        repository.getTodayStats()
}
