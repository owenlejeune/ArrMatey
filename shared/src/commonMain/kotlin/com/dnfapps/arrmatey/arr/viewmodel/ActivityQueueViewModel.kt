package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.LidarrQueueItem
import com.dnfapps.arrmatey.arr.api.model.ListenarrQueueItem
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.api.model.RadarrQueueItem
import com.dnfapps.arrmatey.arr.api.model.ReadarrQueueItem
import com.dnfapps.arrmatey.arr.api.model.SonarrQueueItem
import com.dnfapps.arrmatey.arr.service.ActivityQueueService
import com.dnfapps.arrmatey.arr.state.ActivityQueueUiState
import com.dnfapps.arrmatey.arr.usecase.DeleteQueueItemUseCase
import com.dnfapps.arrmatey.arr.usecase.GetActivityTasksUseCase
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.compose.utils.QueueSortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder
import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.extensions.orderedSortedBy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ActivityQueueViewModel(
    private val activityQueueService: ActivityQueueService,
    getActivityTasksUseCase: GetActivityTasksUseCase,
    instanceRepository: InstanceRepository,
    private val deleteQueueItemUseCase: DeleteQueueItemUseCase
): ViewModel() {

    val activityTasks = getActivityTasksUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val tasksWithIssues = getActivityTasksUseCase.getTasksWithIssues()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val isPolling = activityQueueService.isPolling
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val instances = instanceRepository.observeAllInstances()
        .map { all ->
            all.filter { it.type.supportsActivityQueue }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _activityQueueUiState = MutableStateFlow(ActivityQueueUiState())
    val activityQueueUiState: StateFlow<ActivityQueueUiState> = _activityQueueUiState.asStateFlow()

    private val _removeItemState = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val removeItemState: StateFlow<OperationStatus> = _removeItemState.asStateFlow()

    val queueItems: StateFlow<List<QueueItem>> = combine(
        activityTasks,
        _activityQueueUiState
    ) { tasks, (instanceId, sortBy, sortOrder) ->
        val grouped = groupByTask(tasks)
        val filtered = filterByInstance(grouped, instanceId)
        applySorting(filtered, sortBy, sortOrder)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        startPolling()
    }

    fun startPolling() {
        activityQueueService.startPolling()
    }

    fun stopPolling() {
        activityQueueService.stopPolling()
    }

    fun setInstanceId(id: Long?) {
        val currentState = _activityQueueUiState.value
        _activityQueueUiState.value = currentState.copy(instanceId = id)
    }

    fun setSortBy(sortBy: QueueSortBy) {
        val currentState = _activityQueueUiState.value
        _activityQueueUiState.value = currentState.copy(sortBy = sortBy)
    }

    fun setSortOrder(order: SortOrder) {
        val currentState = _activityQueueUiState.value
        _activityQueueUiState.value = currentState.copy(sortOrder = order)
    }

    fun getQueueItemForEpisode(episode: Episode): SonarrQueueItem? {
        val tasks = activityTasks.value.filterIsInstance<SonarrQueueItem>()

        val episodeMatch = tasks.firstOrNull { it.calcEpisodeId == episode.id }
        if (episodeMatch != null) return episodeMatch

        return tasks.firstOrNull {
            it.calcSeriesId == episode.seriesId &&
                it.seasonNumber == episode.seasonNumber &&
                it.calcEpisodeId == null
        }
    }

    fun removeQueueItem(
        item: QueueItem,
        removeFromClient: Boolean,
        addToBlocklist: Boolean,
        skipRedownload: Boolean
    ) {
        viewModelScope.launch {
            deleteQueueItemUseCase(item, removeFromClient, addToBlocklist, skipRedownload)
                .collect { state ->
                    _removeItemState.value = state
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            activityQueueService.manualRefresh()
        }
    }

    override fun onCleared() {
        super.onCleared()
        activityQueueService.stopPolling()
    }

    private fun groupByTask(items: List<QueueItem>): List<QueueItem> =
        items.groupBy { it.taskGroup }
            .map { (_, groupItems) ->
                val first = groupItems.first()
                groupItems.size.takeIf { it > 0 }?.let { size ->
                    when (first) {
                        is SonarrQueueItem -> first.copy(taskGroupCount = size)
                        is RadarrQueueItem -> first.copy(taskGroupCount = size)
                        is LidarrQueueItem -> first.copy(taskGroupCount = size)
                        is ReadarrQueueItem -> first.copy(taskGroupCount = size)
                        is ListenarrQueueItem -> first
                    }
                } ?: first
            }

    private fun filterByInstance(items: List<QueueItem>, instanceId: Long?): List<QueueItem> =
        instanceId?.let { items.filter { it.instanceId == instanceId } } ?: items

    private fun applySorting(items: List<QueueItem>, sortBy: QueueSortBy, sortOrder: SortOrder) = when(sortBy) {
        QueueSortBy.Title -> items.orderedSortedBy(sortOrder) { it.titleLabel }
        QueueSortBy.Added -> items.orderedSortedBy(sortOrder) { it.added }
    }
}