package com.dnfapps.arrmatey.seerr.usecase

import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.state.SeerrDetailsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

class GetSeerrMediaDetailsUseCase {
    operator fun invoke(
        tmdbId: Long,
        type: RequestType,
        repository: SeerrInstanceRepository
    ): Flow<SeerrDetailsState> = channelFlow {
        send(SeerrDetailsState.Loading)

        repository.observeMediaDetails(tmdbId, type)
            .collect { detailsResult ->
                when (detailsResult) {
                    is NetworkResult.Loading -> send(SeerrDetailsState.Loading)
                    is NetworkResult.Error -> {
                        send(SeerrDetailsState.Error(
                            detailsResult.errorType,
                            detailsResult.message
                        ))
                    }
                    is NetworkResult.Success -> {
                        send(SeerrDetailsState.Success(detailsResult.data))
                    }
                }
            }
    }
}