package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Arrtist
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.BookshelfHistoryItem
import com.dnfapps.arrmatey.arr.api.model.DownloadedMediaItem
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.HistoryEventType
import com.dnfapps.arrmatey.arr.api.model.HistoryItem
import com.dnfapps.arrmatey.arr.api.model.HistoryStateFilter
import com.dnfapps.arrmatey.arr.api.model.LidarrHistoryItem
import com.dnfapps.arrmatey.arr.api.model.ListenarrHistoryItem
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.api.model.RadarrHistoryItem
import com.dnfapps.arrmatey.arr.api.model.SonarrHistoryItem
import com.dnfapps.arrmatey.arr.api.model.SonarrQueueItem
import com.dnfapps.arrmatey.arr.api.model.groupByTask
import com.dnfapps.arrmatey.arr.service.ActivityQueueService
import com.dnfapps.arrmatey.arr.state.ActivityQueueUiState
import com.dnfapps.arrmatey.arr.state.ActivityTabSegment
import com.dnfapps.arrmatey.arr.usecase.DeleteQueueItemUseCase
import com.dnfapps.arrmatey.arr.usecase.GetActivityTasksUseCase
import com.dnfapps.arrmatey.compose.utils.QueueSortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder
import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.extensions.orderedSortedBy
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.networking.asSuccess
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityQueueViewModel(
    private val activityQueueService: ActivityQueueService,
    getActivityTasksUseCase: GetActivityTasksUseCase,
    instanceRepository: InstanceRepository,
    private val deleteQueueItemUseCase: DeleteQueueItemUseCase,
    private val instanceManager: InstanceManager,
) : ViewModel() {
    val activityTasks =
        getActivityTasksUseCase()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    val tasksWithIssues =
        getActivityTasksUseCase
            .getTasksWithIssues()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0,
            )

    val isPolling =
        activityQueueService.isPolling
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

    val hasLoaded: StateFlow<Boolean> = activityQueueService.hasLoaded

    val isHistoryLoading =
        activityQueueService.isHistoryLoading
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

    val hasHistoryLoaded: StateFlow<Boolean> = activityQueueService.hasHistoryLoaded

    val instances =
        instanceRepository
            .observeAllInstances()
            .map { all ->
                all.filter { it.type.supportsActivityQueue }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    private val _activityQueueUiState = MutableStateFlow(ActivityQueueUiState())
    val activityQueueUiState: StateFlow<ActivityQueueUiState> = _activityQueueUiState.asStateFlow()

    private val _removeItemState = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val removeItemState: StateFlow<OperationStatus> = _removeItemState.asStateFlow()

    val queueItems: StateFlow<List<QueueItem>> =
        combine(
            activityTasks,
            _activityQueueUiState,
        ) { tasks, uiState ->
            val grouped = tasks.groupByTask()
            val filtered = filterByInstance(grouped, uiState.instanceId)
            applySorting(filtered, uiState.sortBy, uiState.sortOrder)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    val historyItems: StateFlow<List<HistoryItem>> =
        combine(
            activityQueueService.allHistory,
            _activityQueueUiState,
        ) { history, uiState ->
            val byInstance =
                if (uiState.historyInstanceId != null) {
                    history.filter { it.instanceId == uiState.historyInstanceId }
                } else {
                    history
                }
            if (uiState.historyStateFilter != HistoryStateFilter.All) {
                byInstance.filter { uiState.historyStateFilter.matches(it.eventType) }
            } else {
                byInstance
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    val downloadedItems: StateFlow<List<DownloadedMediaItem>> =
        combine(
            activityQueueService.allHistory,
            instanceManager.instanceRepositories,
            _activityQueueUiState,
        ) { history, repos, uiState ->
            val arrRepos = repos.values.filterIsInstance<ArrInstanceRepository>()
            val repoMap = arrRepos.associateBy { it.instance.id }

            val downloadedEvents =
                setOf(
                    HistoryEventType.DownloadFolderImported,
                    HistoryEventType.MovieFolderImported,
                    HistoryEventType.SeriesFolderImported,
                    HistoryEventType.BookFileImported,
                    HistoryEventType.AudiobookFileAdded,
                    HistoryEventType.Added,
                )

            val filteredHistory =
                history
                    .filter { it.eventType in downloadedEvents }
                    .let { list ->
                        if (uiState.downloadedInstanceId != null) {
                            list.filter { it.instanceId == uiState.downloadedInstanceId }
                        } else {
                            list
                        }
                    }

            val historyItemsList =
                filteredHistory.map { hist ->
                    val repo = hist.instanceId?.let { repoMap[it] }
                    val library =
                        repo
                            ?.library
                            ?.value
                            ?.asSuccess()
                            ?.data ?: emptyList()
                    val instType = hist.instanceType ?: repo?.instance?.type ?: InstanceType.Radarr
                    val instName = hist.instanceName ?: repo?.instance?.label ?: ""
                    val instId = hist.instanceId ?: repo?.instance?.id ?: 0L

                    val instanceUrl = repo?.instance?.url

                    when (hist) {
                        is SonarrHistoryItem -> {
                            val rawSeries = hist.series ?: (library.firstOrNull { it.id == hist.seriesId } as? ArrSeries)
                            val series = if (instanceUrl != null) rawSeries?.withLocalImages(instanceUrl) else rawSeries
                            val episode = hist.episode
                            val epLabel = episode?.seasonEpLabel
                            val epTitle = episode?.title
                            val sub =
                                when {
                                    epLabel != null && !epTitle.isNullOrBlank() -> "$epLabel • $epTitle"
                                    epLabel != null -> epLabel
                                    !epTitle.isNullOrBlank() -> epTitle
                                    else -> hist.sourceTitle?.split("/")?.last()
                                }
                            DownloadedMediaItem(
                                id = "history_${instId}_${hist.id}",
                                title = series?.title ?: hist.data["seriesTitle"] ?: hist.sourceTitle ?: "Unknown Series",
                                subtitle = sub,
                                episodeInfo = epLabel,
                                episodeTitle = epTitle,
                                media = series,
                                date = hist.date,
                                quality = hist.quality.qualityLabel,
                                size = hist.data["fileSize"]?.toLongOrNull() ?: episode?.episodeFile?.size,
                                languages = hist.languages.mapNotNull { it.name },
                                indexer = hist.indexerLabel ?: hist.data["indexer"],
                                customFormats = hist.customFormats.mapNotNull { it.name },
                                instanceId = instId,
                                instanceName = instName,
                                instanceType = instType,
                            )
                        }

                        is RadarrHistoryItem -> {
                            val rawMovie = hist.movie ?: (library.firstOrNull { it.id == hist.movieId } as? ArrMovie)
                            val movie = if (instanceUrl != null) rawMovie?.withLocalImages(instanceUrl) else rawMovie
                            DownloadedMediaItem(
                                id = "history_${instId}_${hist.id}",
                                title = movie?.title ?: hist.data["movieTitle"] ?: hist.sourceTitle ?: "Unknown Movie",
                                subtitle = movie?.year?.toString(),
                                media = movie,
                                date = hist.date,
                                quality = hist.quality.qualityLabel,
                                size = hist.data["fileSize"]?.toLongOrNull() ?: movie?.fileSize,
                                languages = hist.languages.mapNotNull { it.name },
                                indexer = hist.indexerLabel ?: hist.data["indexer"],
                                customFormats = hist.customFormats.mapNotNull { it.name },
                                instanceId = instId,
                                instanceName = instName,
                                instanceType = instType,
                            )
                        }

                        is LidarrHistoryItem -> {
                            val rawArtist = hist.artist ?: (library.firstOrNull { it.id == hist.artistId } as? Arrtist)
                            val artist = if (instanceUrl != null) rawArtist?.withLocalImages(instanceUrl) else rawArtist
                            val title =
                                when {
                                    hist.track?.title != null -> hist.track.title
                                    hist.album?.title != null -> hist.album.title
                                    else -> hist.displayTitle ?: "Unknown Track"
                                }
                            DownloadedMediaItem(
                                id = "history_${instId}_${hist.id}",
                                title = title,
                                subtitle = artist?.title,
                                media = artist,
                                date = hist.date,
                                quality = hist.quality.qualityLabel,
                                size = hist.data["fileSize"]?.toLongOrNull() ?: hist.album?.statistics?.sizeOnDisk,
                                indexer = hist.indexerLabel ?: hist.data["indexer"],
                                instanceId = instId,
                                instanceName = instName,
                                instanceType = instType,
                            )
                        }

                        is BookshelfHistoryItem -> {
                            val rawAuthor = hist.author ?: (library.firstOrNull { it.id == hist.authorId } as? Author)
                            val author = if (instanceUrl != null) rawAuthor?.withLocalImages(instanceUrl) else rawAuthor
                            DownloadedMediaItem(
                                id = "history_${instId}_${hist.id}",
                                title = hist.book?.title ?: hist.displayTitle ?: "Unknown Book",
                                subtitle = author?.title,
                                media = author,
                                date = hist.date,
                                quality = hist.quality.qualityLabel,
                                size = hist.data["fileSize"]?.toLongOrNull() ?: author?.statistics?.sizeOnDisk,
                                indexer = hist.indexerLabel ?: hist.data["indexer"],
                                instanceId = instId,
                                instanceName = instName,
                                instanceType = instType,
                            )
                        }

                        is ListenarrHistoryItem -> {
                            val audiobook = library.firstOrNull { it.id == hist.audiobookId } as? Audiobook
                            DownloadedMediaItem(
                                id = "history_${instId}_${hist.id}",
                                title = audiobook?.title ?: hist.audiobookTitle,
                                subtitle = audiobook?.authors?.joinToString(", "),
                                media = audiobook,
                                date = hist.date,
                                size = audiobook?.fileSize,
                                indexer = hist.source,
                                instanceId = instId,
                                instanceName = instName,
                                instanceType = instType,
                            )
                        }

                        else -> {
                            DownloadedMediaItem(
                                id = "history_${instId}_${hist.id}",
                                title = hist.displayTitle ?: "Unknown Media",
                                subtitle = hist.sourceTitle,
                                date = hist.date,
                                quality = hist.quality?.qualityLabel,
                                languages = hist.languages.mapNotNull { it.name },
                                indexer = hist.indexerLabel,
                                customFormats = hist.customFormats.mapNotNull { it.name },
                                instanceId = instId,
                                instanceName = instName,
                                instanceType = instType,
                            )
                        }
                    }
                }

            historyItemsList.sortedByDescending { it.date }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    val isDownloadedLoading: StateFlow<Boolean> =
        activityQueueService.isHistoryLoading

    val hasDownloadedLoaded: StateFlow<Boolean> =
        activityQueueService.hasHistoryLoaded

    init {
        startPolling()
    }

    fun startPolling() {
        activityQueueService.startPolling()
    }

    fun stopPolling() {
        activityQueueService.stopPolling()
    }

    fun setSelectedTab(tab: ActivityTabSegment) {
        val currentState = _activityQueueUiState.value
        _activityQueueUiState.value = currentState.copy(selectedTab = tab)
        if (tab == ActivityTabSegment.Downloaded) {
            val arrRepos = instanceManager.getAllArrRepositories()
            arrRepos.forEach { repo ->
                if (repo.library.value == null) {
                    viewModelScope.launch { repo.refreshLibrary() }
                }
            }
        }
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

    fun setHistoryStateFilter(filter: HistoryStateFilter) {
        val currentState = _activityQueueUiState.value
        _activityQueueUiState.value = currentState.copy(historyStateFilter = filter)
    }

    fun setHistoryInstanceId(id: Long?) {
        val currentState = _activityQueueUiState.value
        _activityQueueUiState.value = currentState.copy(historyInstanceId = id)
    }

    fun setDownloadedInstanceId(id: Long?) {
        val currentState = _activityQueueUiState.value
        _activityQueueUiState.value = currentState.copy(downloadedInstanceId = id)
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
        skipRedownload: Boolean,
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
            val arrRepos = instanceManager.getAllArrRepositories()
            arrRepos.forEach { repo ->
                launch { repo.refreshLibrary() }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        activityQueueService.stopPolling()
    }

    private fun filterByInstance(
        items: List<QueueItem>,
        instanceId: Long?,
    ): List<QueueItem> = instanceId?.let { items.filter { it.instanceId == instanceId } } ?: items

    private fun applySorting(
        items: List<QueueItem>,
        sortBy: QueueSortBy,
        sortOrder: SortOrder,
    ) = when (sortBy) {
        QueueSortBy.Title -> items.orderedSortedBy(sortOrder) { it.titleLabel }
        QueueSortBy.Added -> items.orderedSortedBy(sortOrder) { it.added }
    }
}
