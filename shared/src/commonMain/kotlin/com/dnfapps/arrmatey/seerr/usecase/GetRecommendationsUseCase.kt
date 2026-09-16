package com.dnfapps.arrmatey.seerr.usecase

import com.dnfapps.arrmatey.client.paging.PagingController
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.seerr.api.model.DiscoverResult
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import kotlinx.coroutines.CoroutineScope

class GetRecommendationsUseCase {
    fun createPagingController(
        repository: SeerrInstanceRepository,
        mediaType: RequestType,
        mediaId: Long,
        scope: CoroutineScope,
    ): PagingController<DiscoverResult> =
        PagingController(
            scope = scope,
            keySelector = { "${it.mediaType.name}_${it.id}" },
        ) {
            when (mediaType) {
                RequestType.Movie -> repository.getMovieRecommendationsPaging(mediaId)
                RequestType.Tv -> repository.getTvRecommendationsPaging(mediaId)
                RequestType.Person -> throw IllegalArgumentException("Recommendations not supported for Person")
            }
        }
}
