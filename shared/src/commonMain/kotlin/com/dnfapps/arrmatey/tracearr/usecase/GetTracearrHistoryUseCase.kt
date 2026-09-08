package com.dnfapps.arrmatey.tracearr.usecase

import com.dnfapps.arrmatey.instances.repository.TracearrRepository
import com.dnfapps.arrmatey.tracearr.api.model.TracearrHistoryResponse
import com.dnfapps.networking.NetworkResult

class GetTracearrHistoryUseCase {
    suspend operator fun invoke(
        repository: TracearrRepository,
        cursor: String? = null,
        pageSize: Int? = null,
    ): NetworkResult<TracearrHistoryResponse> =
        repository.getHistory(cursor = cursor, pageSize = pageSize)
}
