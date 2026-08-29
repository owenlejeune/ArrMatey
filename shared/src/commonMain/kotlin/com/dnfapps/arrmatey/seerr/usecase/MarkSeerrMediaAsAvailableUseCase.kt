package com.dnfapps.arrmatey.seerr.usecase

import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.networking.NetworkResult

class MarkSeerrMediaAsAvailableUseCase {
    suspend operator fun invoke(
        mediaId: Long,
        is4k: Boolean = false,
        repository: SeerrInstanceRepository,
    ): NetworkResult<Unit> = repository.markMediaAsAvailable(mediaId, is4k)
}
