package com.dnfapps.arrmatey.seerr.usecase

import com.dnfapps.arrmatey.client.paging.PagingController
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.seerr.api.model.DiscoverResult
import kotlinx.coroutines.CoroutineScope

class GetDiscoverMoviesUseCase {
    fun createPagingController(
        repository: SeerrInstanceRepository,
        scope: CoroutineScope
    ): PagingController<DiscoverResult> {
        return PagingController(scope) {
            repository.getDiscoverMoviesPaging()
        }
    }
}
