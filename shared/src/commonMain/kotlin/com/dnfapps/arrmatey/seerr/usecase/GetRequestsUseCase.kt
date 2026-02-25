package com.dnfapps.arrmatey.seerr.usecase

import com.dnfapps.arrmatey.client.paging.PagingController
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.seerr.api.model.MediaRequest
import com.dnfapps.arrmatey.seerr.api.model.MediaRequestPackage
import kotlinx.coroutines.CoroutineScope

class GetRequestsUseCase {
    fun createPagingController(
        repository: SeerrInstanceRepository,
        scope: CoroutineScope
    ): PagingController<MediaRequestPackage> {
        return PagingController(scope) {
            repository.getRequestsPaging()
        }
    }
}