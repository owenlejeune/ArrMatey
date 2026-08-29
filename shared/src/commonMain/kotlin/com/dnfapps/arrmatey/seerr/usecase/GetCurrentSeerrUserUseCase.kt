package com.dnfapps.arrmatey.seerr.usecase

import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.seerr.api.model.SeerrUser
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class GetCurrentSeerrUserUseCase {
    operator fun invoke(repository: SeerrInstanceRepository): Flow<SeerrUser?> =
        flow {
            coroutineScope {
                launch {
                    repository.getLoggedInUser()
                }
            }
            repository.loggedInUser
                .collect { emit(it) }
        }
}
