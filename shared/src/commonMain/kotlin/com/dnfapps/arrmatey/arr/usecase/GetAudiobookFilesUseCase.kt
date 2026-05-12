package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.state.AudiobookFilesState
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class GetAudiobookFilesUseCase(
    private val instanceManager: InstanceManager
) {
    operator fun invoke(audiobookId: Long): Flow<AudiobookFilesState> = channelFlow {
        instanceManager.getSelectedArrRepository(InstanceType.Listenarr)
            .filterNotNull()
            .collectLatest { repository ->
                repository.getAudiobookFiles(audiobookId)
                combine(
                    repository.audiobookFiles.map { it[audiobookId] ?: emptyList() },
                    repository.observeItemHistory(audiobookId),
                    repository.historyStatus
                ) { audiobookFiles, history, status  ->
                    AudiobookFilesState(
                        files = audiobookFiles,
                        history = history,
                        isRefreshing = status is OperationStatus.InProgress
                    )
                }.collect { send(it) }
            }
    }

    suspend fun refreshHistory(audiobookId: Long) {
        instanceManager.getSelectedArrRepository(InstanceType.Listenarr)
            .firstOrNull()
            ?.getItemHistory(audiobookId)
    }
}