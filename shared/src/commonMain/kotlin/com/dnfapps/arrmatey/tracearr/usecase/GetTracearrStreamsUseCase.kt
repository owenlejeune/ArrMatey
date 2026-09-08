package com.dnfapps.arrmatey.tracearr.usecase

import com.dnfapps.arrmatey.instances.repository.TracearrRepository
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStreamsResponse
import com.dnfapps.networking.NetworkResult

class GetTracearrStreamsUseCase {
    suspend operator fun invoke(repository: TracearrRepository): NetworkResult<TracearrStreamsResponse> =
        repository.getPublicStreams()
}
