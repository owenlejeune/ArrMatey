package com.dnfapps.arrmatey.seerr.usecase

import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.seerr.api.model.MediaRequest
import com.dnfapps.arrmatey.seerr.api.model.RequestMediaBody
import com.dnfapps.networking.NetworkResult

class SubmitRequestUseCase {
    suspend operator fun invoke(
        request: RequestMediaBody,
        repository: SeerrInstanceRepository,
    ): NetworkResult<MediaRequest> = repository.createRequest(request)
}
