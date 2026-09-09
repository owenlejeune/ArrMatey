package com.dnfapps.arrmatey.tracearr.usecase

import com.dnfapps.arrmatey.instances.repository.TracearrRepository
import com.dnfapps.arrmatey.tracearr.api.model.TracearrUserDetail
import com.dnfapps.networking.NetworkResult

class GetUserDetailUseCase {
    suspend operator fun invoke(
        repository: TracearrRepository,
        ref: String,
    ): NetworkResult<TracearrUserDetail> =
        repository.getUserDetails(ref)
}
