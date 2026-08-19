package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.api.model.RadarrQueueItem
import com.dnfapps.arrmatey.arr.state.MovieFilesState
import com.dnfapps.arrmatey.arr.usecase.DeleteQueueItemUseCase
import com.dnfapps.arrmatey.arr.usecase.GetActivityTasksUseCase
import com.dnfapps.arrmatey.arr.usecase.GetMovieFilesUseCase
import com.dnfapps.networking.OperationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MovieFilesViewModel(
    private val movieId: Long,
    private val getMovieFilesUseCase: GetMovieFilesUseCase,
    getActivityTasksUseCase: GetActivityTasksUseCase,
    private val deleteQueueItemUseCase: DeleteQueueItemUseCase
): ViewModel() {

    private val _uiState = MutableStateFlow(MovieFilesState())
    val uiState: StateFlow<MovieFilesState> = _uiState.asStateFlow()

    val queueItems: StateFlow<List<QueueItem>> = getActivityTasksUseCase()
        .map { tasks ->
            tasks.filter { task ->
                (task as? RadarrQueueItem)?.movieId == movieId || task.mediaId == movieId
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
        observeMovieFiles()
        refreshHistory()
    }

    private fun observeMovieFiles() {
        viewModelScope.launch {
            getMovieFilesUseCase(movieId)
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    fun refreshHistory() {
        viewModelScope.launch {
            getMovieFilesUseCase.refreshHistory(movieId)
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