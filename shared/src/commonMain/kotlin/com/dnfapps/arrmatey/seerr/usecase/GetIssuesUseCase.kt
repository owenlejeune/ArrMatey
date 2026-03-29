package com.dnfapps.arrmatey.seerr.usecase

import com.dnfapps.arrmatey.client.paging.PagingController
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.seerr.api.model.MediaIssuePackage
import kotlinx.coroutines.CoroutineScope

class GetIssuesUseCase {
    fun createPagingController(
        repository: SeerrInstanceRepository,
        scope: CoroutineScope
    ): PagingController<MediaIssuePackage> {
        return PagingController(scope) {
            repository.getIssuesPaging()
        }
    }
}