package com.dnfapps.arrmatey.tracearr.usecase

import com.dnfapps.arrmatey.instances.repository.TracearrRepository
import com.dnfapps.arrmatey.tracearr.api.model.TracearrActivityResponse
import com.dnfapps.arrmatey.tracearr.api.model.TracearrPeriod
import com.dnfapps.networking.NetworkResult

class GetTracearrActivityUseCase {
    suspend operator fun invoke(
        repository: TracearrRepository,
        period: TracearrPeriod = TracearrPeriod.Month,
    ): NetworkResult<TracearrActivityResponse> {
        return repository.getActivity(period)
    }
}
