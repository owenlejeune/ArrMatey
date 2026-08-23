package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.api.model.SonarrQueueItem
import com.dnfapps.arrmatey.arr.state.HistoryState
import com.dnfapps.arrmatey.arr.usecase.DeleteEpisodeFileUseCase
import com.dnfapps.arrmatey.arr.usecase.DeleteQueueItemUseCase
import com.dnfapps.arrmatey.arr.usecase.GetActivityTasksUseCase
import com.dnfapps.arrmatey.arr.usecase.GetEpisodeHistoryUseCase
import com.dnfapps.arrmatey.arr.usecase.PerformAutomaticSearchUseCase
import com.dnfapps.arrmatey.arr.usecase.ToggleMonitorUseCase
import com.dnfapps.networking.OperationStatus
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.instances.usecase.GetArrInstanceRepositoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EpisodeDetailsViewModel(
    private val seriesId: Long,
    episode: Episode,
    private val getArrInstanceRepositoryUseCase: GetArrInstanceRepositoryUseCase,
    private val toggleMonitorUseCase: ToggleMonitorUseCase,
    private val performAutomaticSearchUseCase: PerformAutomaticSearchUseCase,
    private val getEpisodeHistoryUseCase: GetEpisodeHistoryUseCase,
    private val deleteEpisodeUseCase: DeleteEpisodeFileUseCase,
    getActivityTasksUseCase: GetActivityTasksUseCase,
    private val deleteQueueItemUseCase: DeleteQueueItemUseCase
): ViewModel() {

    private val _episode = MutableStateFlow(episode)
    val episode: StateFlow<Episode> = _episode.asStateFlow()

    private val _history = MutableStateFlow<HistoryState>(HistoryState.Initial)
    val history: StateFlow<HistoryState> = _history.asStateFlow()

    private val _monitorStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val monitorStatus: StateFlow<OperationStatus> = _monitorStatus.asStateFlow()

    private val _deleteStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val deleteStatus: StateFlow<OperationStatus> = _deleteStatus.asStateFlow()

    val queueItems: StateFlow<List<QueueItem>> = getActivityTasksUseCase()
        .map { tasks ->
            tasks.filterIsInstance<SonarrQueueItem>().filter { task ->
                task.calcEpisodeId == episode.id ||
                    (task.calcSeriesId == seriesId && task.seasonNumber == episode.seasonNumber && task.calcEpisodeId == null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _removeQueueItemStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val removeQueueItemStatus: StateFlow<OperationStatus> = _removeQueueItemStatus.asStateFlow()

    private var currentRepository: ArrInstanceRepository? = null

    init {
        observeSelectedInstance()
    }

    private fun observeSelectedInstance() {
        viewModelScope.launch {
            getArrInstanceRepositoryUseCase.observeSelected(InstanceType.Sonarr)
                .filterNotNull()
                .collectLatest { repository ->
                    currentRepository = repository
                    observeData(repository)
                    refreshHistory()
                }
        }
    }

    private fun observeData(repository: ArrInstanceRepository) {
        viewModelScope.launch {
            repository.episodes
                .map { episodesMap ->
                    episodesMap[seriesId]?.firstOrNull { it.id == episode.value.id }
                }
                .collect { episode ->
                    episode?.let { _episode.value = it }
                }
        }

        viewModelScope.launch {
            repository.monitorStatus.collect { status ->
                _monitorStatus.value = status
            }
        }
    }

    fun toggleMonitor() {
        viewModelScope.launch {
            currentRepository?.let {
               toggleMonitorUseCase.toggleEpisode(_episode.value, it)
            }
        }
    }

    fun executeAutomaticSearch() {
        viewModelScope.launch {
            currentRepository?.let {
                performAutomaticSearchUseCase(
                    mediaId = seriesId,
                    type = InstanceType.Sonarr,
                    repository = it,
                    episodeId = _episode.value.id
                )
            }
        }
    }

    fun refreshHistory() {
        viewModelScope.launch {
            val repository = currentRepository ?: return@launch
            getEpisodeHistoryUseCase(_episode.value.id, repository)
                .collect { state ->
                    _history.value = state
                }
        }
    }

    fun deleteEpisode() {
        viewModelScope.launch {
            val repository = currentRepository ?: return@launch
            _episode.value.episodeFileId?.let { fileId ->
                deleteEpisodeUseCase(seriesId, fileId, repository)
                    .collect { state ->
                        _deleteStatus.value = state
                        refreshHistory()
                    }
            }
        }
    }

    fun resetMonitorStatus() {
        _monitorStatus.value = OperationStatus.Idle
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