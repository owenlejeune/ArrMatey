package com.dnfapps.arrmatey.bazarr.usecase

import com.dnfapps.arrmatey.instances.repository.BazarrInstanceRepository
import com.dnfapps.networking.NetworkResult

class ResetBazarrProvidersUseCase {
    suspend operator fun invoke(repository: BazarrInstanceRepository): NetworkResult<Unit> = repository.resetProviders()
}
