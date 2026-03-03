package com.dnfapps.arrmatey.seerr.usecase

import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository

class RemoveSeerrMediaFileUseCase {
    suspend operator fun invoke(
        requestId: Long,
        mediaId: Long,
        is4k: Boolean,
        repository: SeerrInstanceRepository
    ): NetworkResult<Unit> =
        repository.deleteMediaFile(requestId, mediaId, is4k)
}