package com.dnfapps.arrmatey.seerr.usecase

import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.seerr.api.model.PersonCredits
import com.dnfapps.networking.NetworkResult

class GetPersonCreditsUseCase {
    suspend operator fun invoke(
        personId: Long,
        repository: SeerrInstanceRepository,
    ): NetworkResult<PersonCredits> = repository.getPersonCredits(personId)
}
