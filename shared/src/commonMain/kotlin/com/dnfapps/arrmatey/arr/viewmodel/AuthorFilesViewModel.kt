package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.api.model.ReadarrQueueItem
import com.dnfapps.arrmatey.arr.state.AuthorFilesState
import com.dnfapps.arrmatey.arr.usecase.DeleteQueueItemUseCase
import com.dnfapps.arrmatey.arr.usecase.GetActivityTasksUseCase
import com.dnfapps.arrmatey.arr.usecase.GetAuthorFilesUseCase
import com.dnfapps.networking.OperationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthorFilesViewModel(
    private val authorId: Long,
    private val getAuthorFilesUseCase: GetAuthorFilesUseCase,
    getActivityTasksUseCase: GetActivityTasksUseCase,
    private val deleteQueueItemUseCase: DeleteQueueItemUseCase
): ViewModel() {

    private val _uiState = MutableStateFlow(AuthorFilesState())
    val uiState: StateFlow<AuthorFilesState> = _uiState.asStateFlow()

    val queueItems: StateFlow<List<QueueItem>> = getActivityTasksUseCase()
        .map { tasks ->
            tasks.filterIsInstance<ReadarrQueueItem>().filter { task ->
                task.authorId == authorId || task.author?.id == authorId || task.mediaId == authorId
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _removeQueueItemStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val removeQueueItemStatus: StateFlow<OperationStatus> = _removeQueueItemStatus.asStateFlow()

    init {
        observeAuthorFiles()
        refreshHistory()
    }

    private fun observeAuthorFiles() {
        viewModelScope.launch {
            getAuthorFilesUseCase(authorId)
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    fun refreshHistory() {
        viewModelScope.launch {
            getAuthorFilesUseCase.refreshHistory(authorId)
        }
    }

    fun removeQueueItem(
        queueItem: QueueItem,
        removeFromClient: Boolean,
        addToBlocklist: Boolean,
        skipRedownload: Boolean
    ) {
        viewModelScope.launch {
            deleteQueueItemUseCase(
                queueItem = queueItem,
                removeFromClient = removeFromClient,
                addToBlocklist = addToBlocklist,
                skipRedownload = skipRedownload
            ).collect { status ->
                _removeQueueItemStatus.value = status
            }
        }
    }
}