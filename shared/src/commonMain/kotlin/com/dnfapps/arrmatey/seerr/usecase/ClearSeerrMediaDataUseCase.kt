package com.dnfapps.arrmatey.seerr.usecase

import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.networking.NetworkResult

class ClearSeerrMediaDataUseCase {
    suspend operator fun invoke(
        mediaId: Long,
        repository: SeerrInstanceRepository,
    ): NetworkResult<Unit> = repository.clearMediaData(mediaId)
}
