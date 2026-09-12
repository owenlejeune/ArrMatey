package com.dnfapps.arrmatey.tracearr.usecase

import com.dnfapps.arrmatey.instances.repository.TracearrRepository
import com.dnfapps.arrmatey.tracearr.api.model.TracearrUsersResponse
import com.dnfapps.networking.NetworkResult

class GetTracearrUsersUseCase {
    suspend operator fun invoke(
        repository: TracearrRepository,
        cursor: String? = null,
        pageSize: Int? = null,
    ): NetworkResult<TracearrUsersResponse> = repository.getUsers(cursor = cursor, pageSize = pageSize)
}
