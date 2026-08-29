package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.state.MovieFilesState
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.model.OperationStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class GetMovieFilesUseCase(
    private val instanceManager: InstanceManager,
) {
    operator fun invoke(movieId: Long): Flow<MovieFilesState> =
        channelFlow {
            instanceManager
                .getSelectedArrRepository(InstanceType.Radarr)
                .filterNotNull()
                .collectLatest { repository ->
                    repository.getMovieExtraFiles(movieId)
                    combine(
                        repository.movieExtraFiles.map { it[movieId] ?: emptyList() },
                        repository.observeItemHistory(movieId),
                        repository.historyStatus,
                    ) { extraFiles, history, status ->
                        MovieFilesState(
                            extraFiles = extraFiles,
                            history = history,
                            isRefreshing = status is OperationStatus.InProgress,
                        )
                    }.collect { send(it) }
                }
        }

    suspend fun refreshHistory(movieId: Long) {
        instanceManager
            .getSelectedArrRepository(InstanceType.Radarr)
            .firstOrNull()
            ?.getItemHistory(movieId)
    }
}
