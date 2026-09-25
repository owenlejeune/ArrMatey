package com.dnfapps.arrmatey.seerr.usecase

import com.dnfapps.arrmatey.client.paging.PagingController
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.seerr.api.model.MediaRequestPackage
import com.dnfapps.arrmatey.seerr.api.model.RequestState
import kotlinx.coroutines.CoroutineScope

class GetRequestsUseCase {
    fun createPagingController(
        repository: SeerrInstanceRepository,
        scope: CoroutineScope,
        filter: RequestState = RequestState.All,
    ): PagingController<MediaRequestPackage> =
        PagingController(
            scope = scope,
            keySelector = { it.request.id },
        ) {
            repository.getRequestsPaging(filter)
        }
}
