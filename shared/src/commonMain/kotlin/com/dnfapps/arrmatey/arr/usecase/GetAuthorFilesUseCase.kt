package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.state.AuthorFilesState
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

class GetAuthorFilesUseCase(
    private val instanceManager: InstanceManager
) {
    operator fun invoke(authorId: Long): Flow<AuthorFilesState> = channelFlow {
        instanceManager.getSelectedArrRepository(InstanceType.Booksehelf)
            .filterNotNull()
            .collectLatest { repository ->
                repository.getAuthorBookFiles(authorId)
                combine(
                    repository.authorBookFiles.map { it[authorId] ?: emptyList() },
                    repository.observeItemHistory(authorId),
                    repository.historyStatus
                ) { bookFiles, history, status ->
                    AuthorFilesState(
                        files = bookFiles,
                        history = history,
                        isRefreshing = status is OperationStatus.InProgress
                    )
                }.collect { send(it) }
            }
    }

    suspend fun refreshHistory(authorId: Long) {
        instanceManager.getSelectedArrRepository(InstanceType.Booksehelf)
            .firstOrNull()
            ?.getItemHistory(authorId)
    }
}