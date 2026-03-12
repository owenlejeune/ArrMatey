package com.dnfapps.arrmatey.seerr.usecase

import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.seerr.api.model.CombinedRatings
import com.dnfapps.arrmatey.seerr.api.model.RottenTomatoesRating
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

class GetSeerrTvRatingsUseCase(
    private val instanceManager: InstanceManager
) {
    operator fun invoke(tmdbId: Long): Flow<RottenTomatoesRating?> = flow {
        val repository = instanceManager.getSelectedSeerrRepository()
            .firstOrNull()
        repository?.let { repository ->
            repository.getTvRatings(tmdbId)
                .onSuccess { emit(it) }
        }
    }
}