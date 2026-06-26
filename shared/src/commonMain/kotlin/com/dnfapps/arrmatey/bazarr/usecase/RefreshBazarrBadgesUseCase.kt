package com.dnfapps.arrmatey.bazarr.usecase

import com.dnfapps.arrmatey.instances.repository.BazarrInstanceRepository

class RefreshBazarrBadgesUseCase {
    suspend operator fun invoke(repository: BazarrInstanceRepository) {
        repository.refreshBadges()
    }
}
