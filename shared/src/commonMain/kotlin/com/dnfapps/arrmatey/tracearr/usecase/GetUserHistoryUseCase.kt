package com.dnfapps.arrmatey.tracearr.usecase

import com.dnfapps.arrmatey.instances.repository.TracearrRepository
import com.dnfapps.arrmatey.tracearr.api.model.TracearrHistoryResponse
import com.dnfapps.networking.NetworkResult

class GetUserHistoryUseCase {
    suspend operator fun invoke(
        repository: TracearrRepository,
        ref: String,
        cursor: String? = null,
        pageSize: Int? = null,
    ): NetworkResult<TracearrHistoryResponse> =
        repository.getUserHistory(ref = ref, cursor = cursor, pageSize = pageSize)
}
