package com.dnfapps.arrmatey.seerr.usecase

import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.seerr.api.model.CombinedRatings
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

class GetSeerrMovieRatingsUseCase(
    private val instanceManager: InstanceManager,
) {
    operator fun invoke(tmdbId: Long): Flow<CombinedRatings?> = flow {
        val repository = runCatching {
            instanceManager
                .getSelectedSeerrRepository()
                .firstOrNull()
        }.getOrNull()

        if (repository != null) {
            runCatching {
                repository
                    .getMovieRatings(tmdbId)
            }.getOrNull()
                ?.onSuccess { emit(it) }
                ?.onError { _, _, _ -> emit(null) }
                ?: emit(null)
        } else {
            emit(null)
        }
    }.catch { emit(null) }
}
