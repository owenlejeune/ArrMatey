package com.dnfapps.arrmatey.bazarr.usecase

import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.instances.repository.BazarrInstanceRepository

class ResetBazarrProvidersUseCase {
    suspend operator fun invoke(repository: BazarrInstanceRepository): NetworkResult<Unit> {
        return repository.resetProviders()
    }
}
