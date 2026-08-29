package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.ListenarrQueueItem
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.state.AudiobookFilesState
import com.dnfapps.arrmatey.arr.usecase.DeleteQueueItemUseCase
import com.dnfapps.arrmatey.arr.usecase.GetActivityTasksUseCase
import com.dnfapps.arrmatey.arr.usecase.GetAudiobookFilesUseCase
import com.dnfapps.arrmatey.model.OperationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AudiobookFilesViewModel(
    private val audiobookId: Long,
    private val getAudiobookFilesUseCase: GetAudiobookFilesUseCase,
    getActivityTasksUseCase: GetActivityTasksUseCase,
    private val deleteQueueItemUseCase: DeleteQueueItemUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AudiobookFilesState())
    val uiState: StateFlow<AudiobookFilesState> = _uiState.asStateFlow()

    val queueItems: StateFlow<List<QueueItem>> =
        getActivityTasksUseCase()
            .map { tasks ->
                tasks.filterIsInstance<ListenarrQueueItem>().filter { task ->
                    task.audiobookId == audiobookId || task.mediaId == audiobookId
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    private val _removeQueueItemStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val removeQueueItemStatus: StateFlow<OperationStatus> = _removeQueueItemStatus.asStateFlow()

    init {
        observeAudiobookFiles()
        refreshHistory()
    }

    private fun observeAudiobookFiles() {
        viewModelScope.launch {
            getAudiobookFilesUseCase(audiobookId)
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    fun refreshHistory() {
        viewModelScope.launch {
            getAudiobookFilesUseCase.refreshHistory(audiobookId)
        }
    }

    fun removeQueueItem(
        queueItem: QueueItem,
        removeFromClient: Boolean,
        addToBlocklist: Boolean,
        skipRedownload: Boolean,
    ) {
        viewModelScope.launch {
            deleteQueueItemUseCase(
                queueItem = queueItem,
                removeFromClient = removeFromClient,
                addToBlocklist = addToBlocklist,
                skipRedownload = skipRedownload,
            ).collect { status ->
                _removeQueueItemStatus.value = status
            }
        }
    }
}
