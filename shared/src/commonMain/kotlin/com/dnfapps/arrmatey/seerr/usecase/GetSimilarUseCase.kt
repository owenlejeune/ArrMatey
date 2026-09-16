package com.dnfapps.arrmatey.seerr.usecase

import com.dnfapps.arrmatey.client.paging.PagingController
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.seerr.api.model.DiscoverResult
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import kotlinx.coroutines.CoroutineScope

class GetSimilarUseCase {
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
                RequestType.Movie -> repository.getMovieSimilarPaging(mediaId)
                RequestType.Tv -> repository.getTvSimilarPaging(mediaId)
                RequestType.Person -> throw IllegalArgumentException("Similar not supported for Person")
            }
        }
}
