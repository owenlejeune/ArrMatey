package com.dnfapps.arrmatey.seerr.usecase

import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.api.model.Season
import com.dnfapps.arrmatey.seerr.api.model.TvDetails
import com.dnfapps.arrmatey.seerr.state.SeerrDetailsState
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
                        val mediaDetails = detailsResult.data

                        if (type == RequestType.Tv && mediaDetails is TvDetails) {
                            val seasonCount = mediaDetails.numberOfSeasons
                            val seasons = (1..seasonCount).map { seasonNumber ->
                                async {
                                    repository.getSeasonDetails(tmdbId, seasonNumber)
                                }
                            }.awaitAll()
                                .filterIsInstance<NetworkResult.Success<Season>>()
                                .map { it.data }

                            val enrichedDetails = mediaDetails.copy(seasons = seasons)
                            send(SeerrDetailsState.Success(enrichedDetails))
                        } else {
                            send(SeerrDetailsState.Success(mediaDetails))
                        }
                    }
                }
            }
    }
}