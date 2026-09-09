package com.dnfapps.arrmatey.tracearr.usecase

import com.dnfapps.arrmatey.instances.repository.TracearrRepository
import com.dnfapps.arrmatey.tracearr.api.model.TracearrViolationsResponse
import com.dnfapps.networking.NetworkResult

class GetTracearrViolationsUseCase {
    suspend operator fun invoke(
        repository: TracearrRepository,
        page: Int? = null,
        pageSize: Int? = null,
    ): NetworkResult<TracearrViolationsResponse> =
        repository.getViolations(page = page, pageSize = pageSize)
}
