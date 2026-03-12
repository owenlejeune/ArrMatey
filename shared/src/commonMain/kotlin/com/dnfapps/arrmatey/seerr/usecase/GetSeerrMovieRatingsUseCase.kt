package com.dnfapps.arrmatey.seerr.usecase

import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.seerr.api.model.CombinedRatings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

class GetSeerrMovieRatingsUseCase(
    private val instanceManager: InstanceManager
) {
    operator fun invoke(tmdbId: Long): Flow<CombinedRatings?> = flow {
        val repository = instanceManager.getSelectedSeerrRepository()
            .firstOrNull()
        repository?.let { repository ->
            repository.getMovieRatings(tmdbId)
                .onSuccess { emit(it) }
        }
    }
}