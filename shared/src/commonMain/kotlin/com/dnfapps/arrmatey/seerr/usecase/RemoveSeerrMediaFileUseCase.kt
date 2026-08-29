package com.dnfapps.arrmatey.seerr.usecase

import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.networking.NetworkResult

class RemoveSeerrMediaFileUseCase {
    suspend operator fun invoke(
        requestId: Long,
        mediaId: Long,
        is4k: Boolean,
        repository: SeerrInstanceRepository,
    ): NetworkResult<Unit> = repository.deleteMediaFile(requestId, mediaId, is4k)

    suspend operator fun invoke(
        mediaId: Long,
        is4k: Boolean,
        repository: SeerrInstanceRepository,
    ): NetworkResult<Unit> = repository.deleteMediaFile(mediaId, is4k)
}
