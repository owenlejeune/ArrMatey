package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Arrtist
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.BlocklistItem
import com.dnfapps.arrmatey.arr.api.model.BookshelfHistoryItem
import com.dnfapps.arrmatey.arr.api.model.ComicVolume
import com.dnfapps.arrmatey.arr.api.model.DownloadedMediaItem
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.FilesystemItem
import com.dnfapps.arrmatey.arr.api.model.HistoryEventType
import com.dnfapps.arrmatey.arr.api.model.HistoryItem
import com.dnfapps.arrmatey.arr.api.model.HistoryStateFilter
import com.dnfapps.arrmatey.arr.api.model.KapowarrHistoryItem
import com.dnfapps.arrmatey.arr.api.model.LidarrHistoryItem
import com.dnfapps.arrmatey.arr.api.model.ListenarrHistoryItem
import com.dnfapps.arrmatey.arr.api.model.ManualImportFile
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.api.model.RadarrHistoryItem
import com.dnfapps.arrmatey.arr.api.model.SonarrHistoryItem
import com.dnfapps.arrmatey.arr.api.model.SonarrQueueItem
import com.dnfapps.arrmatey.arr.api.model.WantedMediaItem
import com.dnfapps.arrmatey.arr.api.model.groupByTask
import com.dnfapps.arrmatey.arr.service.ActivityQueueService
import com.dnfapps.arrmatey.arr.state.ActivityQueueUiState
import com.dnfapps.arrmatey.arr.state.ActivityTabSegment
import com.dnfapps.arrmatey.arr.usecase.DeleteQueueItemUseCase
import com.dnfapps.arrmatey.arr.usecase.ExecuteArrCommandUseCase
import com.dnfapps.arrmatey.arr.usecase.GetActivityTasksUseCase
import com.dnfapps.arrmatey.compose.utils.QueueSortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder
import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.extensions.orderedSortedBy
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.asSuccess
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
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
    private val executeArrCommandUseCase: ExecuteArrCommandUseCase,
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

    val isBlocklistLoading =
        activityQueueService.isBlocklistLoading
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

    val hasBlocklistLoaded: StateFlow<Boolean> = activityQueueService.hasBlocklistLoaded

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
            val filteredByInstance = filterByInstance(grouped, uiState.instanceId)
            val filtered =
                if (uiState.searchQuery.isBlank()) {
                    filteredByInstance
                } else {
                    val q = uiState.searchQuery.lowercase()
                    filteredByInstance.filter {
                        (it.title?.lowercase()?.contains(q) == true) ||
                            (it.titleLabel.lowercase().contains(q)) ||
                            (it.indexer?.lowercase()?.contains(q) == true)
                    }
                }
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
            val byState =
                if (uiState.historyStateFilter != HistoryStateFilter.All) {
                    byInstance.filter { uiState.historyStateFilter.matches(it.eventType) }
                } else {
                    byInstance
                }
            if (uiState.searchQuery.isBlank()) {
                byState
            } else {
                val q = uiState.searchQuery.lowercase()
                byState.filter {
                    (it.displayTitle?.lowercase()?.contains(q) == true) ||
                        (it.sourceTitle?.lowercase()?.contains(q) == true) ||
                        (it.indexerLabel?.lowercase()?.contains(q) == true)
                }
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
                    HistoryEventType.DownloadImported,
                    HistoryEventType.AlbumImportIncomplete,
                    HistoryEventType.BookImportIncomplete,
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

                    val repoInstance = repo?.instance

                    when (hist) {
                        is SonarrHistoryItem -> {
                            val rawSeries = hist.series ?: (library.firstOrNull { it.id == hist.seriesId } as? ArrSeries)
                            val series = if (repoInstance != null) rawSeries?.withLocalImages(repoInstance) else rawSeries
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
                            val movie = if (repoInstance != null) rawMovie?.withLocalImages(repoInstance) else rawMovie
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
                            val artist = if (repoInstance != null) rawArtist?.withLocalImages(repoInstance) else rawArtist
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
                            val author = if (repoInstance != null) rawAuthor?.withLocalImages(repoInstance) else rawAuthor
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
                                title = audiobook?.title ?: hist.audiobookTitle ?: hist.displayTitle ?: "Unknown Audiobook",
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

                        is KapowarrHistoryItem -> {
                            val volume = library.firstOrNull { it.id == hist.volumeId } as? ComicVolume
                            val issue = volume?.issues?.firstOrNull { it.id == hist.issueId }
                            val issueSubtitle = when {
                                issue != null -> {
                                    val num = issue.issueNumber ?: issue.calculatedIssueNumber?.toInt()?.toString()
                                    val issueTitle = issue.title
                                    when {
                                        num != null && !issueTitle.isNullOrBlank() -> "#$num • $issueTitle"
                                        num != null -> "#$num"
                                        !issueTitle.isNullOrBlank() -> issueTitle
                                        else -> null
                                    }
                                }
                                else -> null
                            }
                            val fallbackSubtitle = hist.fileTitle ?: hist.webSubTitle ?: hist.webTitle
                            val finalSubtitle = (issueSubtitle ?: fallbackSubtitle)
                                ?.takeUnless { it.equals(volume?.title, ignoreCase = true) }
                                ?: volume?.publisher

                            DownloadedMediaItem(
                                id = "history_${instId}_${hist.id}",
                                title = volume?.title ?: hist.displayTitle ?: "Unknown Volume",
                                subtitle = finalSubtitle,
                                media = volume,
                                date = hist.date,
                                size = volume?.fileSize,
                                indexer = hist.source,
                                instanceId = instId,
                                instanceName = instName,
                                instanceType = instType,
                            )
                        }
                    }
                }

            val sortedList = historyItemsList.sortedByDescending { it.date }
            if (uiState.searchQuery.isBlank()) {
                sortedList
            } else {
                val q = uiState.searchQuery.lowercase()
                sortedList.filter {
                    (it.title.lowercase().contains(q)) ||
                        (it.subtitle?.lowercase()?.contains(q) == true) ||
                        (it.indexer?.lowercase()?.contains(q) == true)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    val isDownloadedLoading: StateFlow<Boolean> =
        activityQueueService.isHistoryLoading

    val hasDownloadedLoaded: StateFlow<Boolean> =
        activityQueueService.hasHistoryLoaded

    val blocklistItems: StateFlow<List<BlocklistItem>> =
        combine(
            activityQueueService.allBlocklist,
            _activityQueueUiState,
        ) { blocklist, uiState ->
            val byInstance =
                if (uiState.blocklistInstanceId != null) {
                    blocklist.filter { it.instanceId == uiState.blocklistInstanceId }
                } else {
                    blocklist
                }
            if (uiState.searchQuery.isBlank()) {
                byInstance
            } else {
                val q = uiState.searchQuery.lowercase()
                byInstance.filter {
                    (it.displayTitle?.lowercase()?.contains(q) == true) ||
                        (it.sourceTitle?.lowercase()?.contains(q) == true) ||
                        (it.message?.lowercase()?.contains(q) == true) ||
                        (it.indexerLabel?.lowercase()?.contains(q) == true)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    val isMissingLoading =
        activityQueueService.isMissingLoading
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

    val hasMissingLoaded: StateFlow<Boolean> = activityQueueService.hasMissingLoaded

    val isCutoffLoading =
        activityQueueService.isCutoffLoading
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

    val hasCutoffLoaded: StateFlow<Boolean> = activityQueueService.hasCutoffLoaded

    val WantedMediaItem.sortTimestamp: Long
        get() {
            val instant = when (val m = media) {
                is ArrMovie -> m.releaseDate ?: m.added
                else -> m?.added ?: album?.releaseDate ?: book?.releaseDate
            }
            val yr = year ?: 2026
            return instant?.toEpochMilliseconds() ?: (yr.toLong() * 31536000000L)
        }

    val missingItems: StateFlow<List<WantedMediaItem>> =
        combine(
            activityQueueService.allMissing,
            _activityQueueUiState,
        ) { missing, uiState ->
            val byInstance =
                if (uiState.missingInstanceId != null) {
                    missing.filter { it.instanceId == uiState.missingInstanceId }
                } else {
                    missing
                }
            val sorted = byInstance.sortedByDescending { it.sortTimestamp }
            if (uiState.searchQuery.isBlank()) {
                sorted
            } else {
                val q = uiState.searchQuery.lowercase()
                sorted.filter { it.title?.lowercase()?.contains(q) == true }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    val cutoffUnmetItems: StateFlow<List<WantedMediaItem>> =
        combine(
            activityQueueService.allCutoff,
            _activityQueueUiState,
        ) { cutoff, uiState ->
            val byInstance =
                if (uiState.cutoffInstanceId != null) {
                    cutoff.filter { it.instanceId == uiState.cutoffInstanceId }
                } else {
                    cutoff
                }
            if (uiState.searchQuery.isBlank()) {
                byInstance
            } else {
                val q = uiState.searchQuery.lowercase()
                byInstance.filter { it.title?.lowercase()?.contains(q) == true }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    init {
        startPolling()
        refresh()
        viewModelScope.launch {
            instanceManager.instanceRepositories
                .filter { it.values.any { repo -> repo is ArrInstanceRepository && repo.instance.type.supportsActivityQueue } }
                .collect {
                    refresh()
                }
        }
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

    fun setBlocklistInstanceId(id: Long?) {
        val currentState = _activityQueueUiState.value
        _activityQueueUiState.value = currentState.copy(blocklistInstanceId = id)
    }

    fun setMissingInstanceId(id: Long?) {
        val currentState = _activityQueueUiState.value
        _activityQueueUiState.value = currentState.copy(missingInstanceId = id)
    }

    fun setCutoffInstanceId(id: Long?) {
        val currentState = _activityQueueUiState.value
        _activityQueueUiState.value = currentState.copy(cutoffInstanceId = id)
    }

    fun setSearchQuery(query: String) {
        val currentState = _activityQueueUiState.value
        _activityQueueUiState.value = currentState.copy(searchQuery = query)
    }

    fun searchAllMissing() {
        viewModelScope.launch {
            val missingInst = _activityQueueUiState.value.missingInstanceId
            val cutoffInst = _activityQueueUiState.value.cutoffInstanceId
            val targetInst = missingInst ?: cutoffInst ?: _activityQueueUiState.value.instanceId
            val instancesToSearch =
                if (targetInst != null) {
                    listOf(targetInst)
                } else {
                    instances.value.map { it.id }
                }
            instancesToSearch.forEach { instanceId ->
                executeArrCommandUseCase.searchAllMissing(instanceId)
            }
        }
    }

    private val _filesystemItems = MutableStateFlow<List<FilesystemItem>>(emptyList())
    val filesystemItems: StateFlow<List<FilesystemItem>> = _filesystemItems.asStateFlow()

    private val _isFilesystemLoading = MutableStateFlow(false)
    val isFilesystemLoading: StateFlow<Boolean> = _isFilesystemLoading.asStateFlow()

    private val _filesystemError = MutableStateFlow<String?>(null)
    val filesystemError: StateFlow<String?> = _filesystemError.asStateFlow()

    fun loadFilesystem(instanceId: Long, path: String?) {
        _isFilesystemLoading.value = true
        _filesystemError.value = null
        viewModelScope.launch {
            when (val res = getFilesystem(instanceId, path)) {
                is NetworkResult.Success -> {
                    _filesystemItems.value = res.data
                    _isFilesystemLoading.value = false
                }
                is NetworkResult.Error -> {
                    _filesystemError.value = res.message ?: "Failed to load directory"
                    _isFilesystemLoading.value = false
                }
                else -> {
                    _isFilesystemLoading.value = false
                }
            }
        }
    }

    suspend fun getFilesystem(instanceId: Long, path: String?): NetworkResult<List<FilesystemItem>> {
        val repo = instanceManager.getArrRepository(instanceId) ?: return NetworkResult.Error(message = "Instance not found")
        return repo.getFilesystem(path)
    }

    suspend fun getManualImportFilesForFolder(instanceId: Long, folder: String, filterExisting: Boolean): Result<List<ManualImportFile>> {
        val repo = instanceManager.getArrRepository(instanceId) ?: return Result.failure(Exception("Instance not found"))
        return when (val res = repo.getManualImportFiles(downloadId = null, folder = folder, filterExistingFiles = filterExisting)) {
            is NetworkResult.Success -> Result.success(res.data)
            is NetworkResult.Error -> Result.failure(Exception(res.message ?: "Failed"))
            else -> Result.failure(Exception("Unknown error"))
        }
    }

    fun executeManualImportForFolder(instanceId: Long, files: List<ManualImportFile>, importMode: String): Flow<OperationStatus> = flow {
        val repo = instanceManager.getArrRepository(instanceId) ?: run {
            emit(OperationStatus.Error(message = "Instance not found"))
            return@flow
        }
        emit(OperationStatus.InProgress)
        val normalizedFiles = files.map { file ->
            file.copy(
                seriesId = file.effectiveSeriesId,
                episodeIds = file.effectiveEpisodeIds,
                movieId = file.effectiveMovieId,
            )
        }
        repo.executeManualImport(normalizedFiles, importMode)
            .onSuccess {
                emit(OperationStatus.Success("Import queued successfully"))
                activityQueueService.manualRefresh()
            }
            .onError { code, message, cause ->
                emit(OperationStatus.Error(code, message, cause))
            }
    }

    fun performManualImportForFolder(
        instanceId: Long,
        folderPath: String,
        filterExisting: Boolean,
        importMode: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val filesResult = getManualImportFilesForFolder(instanceId, folderPath, filterExisting)
                if (filesResult.isSuccess) {
                    val files = filesResult.getOrNull() ?: emptyList()
                    if (files.isEmpty()) {
                        onError("No importable files found in directory")
                    } else {
                        executeManualImportForFolder(instanceId, files, importMode)
                            .collectLatest { status ->
                                when (status) {
                                    is OperationStatus.Success -> onSuccess()
                                    is OperationStatus.Error -> onError(status.message ?: "Manual import failed")
                                    else -> {}
                                }
                            }
                    }
                } else {
                    onError(filesResult.exceptionOrNull()?.message ?: "Failed to get manual import files")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error")
            }
        }
    }

    fun scanManualImportFolder(
        instanceId: Long,
        folderPath: String,
        filterExisting: Boolean,
        onSuccess: (List<ManualImportFile>) -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val filesResult = getManualImportFilesForFolder(instanceId, folderPath, filterExisting)
                if (filesResult.isSuccess) {
                    val files = filesResult.getOrNull() ?: emptyList()
                    if (files.isEmpty()) {
                        onError("No importable files found in directory")
                    } else {
                        onSuccess(files)
                    }
                } else {
                    onError(filesResult.exceptionOrNull()?.message ?: "Failed to scan folder")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error")
            }
        }
    }

    fun executeManualImport(
        instanceId: Long,
        files: List<ManualImportFile>,
        importMode: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                executeManualImportForFolder(instanceId, files, importMode)
                    .collectLatest { status ->
                        when (status) {
                            is OperationStatus.Success -> onSuccess()
                            is OperationStatus.Error -> onError(status.message ?: "Manual import failed")
                            else -> {}
                        }
                    }
            } catch (e: Exception) {
                onError(e.message ?: "Manual import failed")
            }
        }
    }

    fun getMediaForInstance(
        instanceId: Long,
        onSuccess: (List<ArrMedia>) -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val repo = instanceManager.getArrRepository(instanceId)
                if (repo == null) {
                    onError("Instance not found")
                    return@launch
                }
                val cached = (repo.library.value as? NetworkResult.Success)?.data
                if (!cached.isNullOrEmpty()) {
                    onSuccess(cached)
                    return@launch
                }
                when (val res = repo.client.getLibrary()) {
                    is NetworkResult.Success -> onSuccess(res.data)
                    is NetworkResult.Error -> onError(res.message ?: "Failed to load library")
                    else -> onError("Unknown error")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Failed to load library")
            }
        }
    }

    fun getEpisodesForSeries(
        instanceId: Long,
        seriesId: Long,
        seasonNumber: Int? = null,
        onSuccess: (List<Episode>) -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val repo = instanceManager.getArrRepository(instanceId)
                if (repo == null) {
                    onError("Instance not found")
                    return@launch
                }
                when (val res = repo.getEpisodes(seriesId, seasonNumber)) {
                    is NetworkResult.Success -> onSuccess(res.data)
                    is NetworkResult.Error -> onError(res.message ?: "Failed to load episodes")
                    else -> onError("Unknown error")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Failed to load episodes")
            }
        }
    }

    fun deleteBlocklistItem(item: BlocklistItem) {
        viewModelScope.launch {
            val instanceId = item.instanceId ?: return@launch
            val repo = instanceManager.getAllArrRepositories().firstOrNull { it.instance.id == instanceId } ?: return@launch
            repo.deleteBlocklistItem(item.id)
            activityQueueService.manualRefresh()
        }
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
                    if (state is OperationStatus.Success) {
                        activityQueueService.removeTaskLocally(item.id)
                    }
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

    fun refreshActivity() {
        viewModelScope.launch {
            activityQueueService.refreshActivity()
        }
    }

    fun refreshHistory() {
        viewModelScope.launch {
            activityQueueService.refreshHistory()
        }
    }

    fun refreshDownloaded() {
        viewModelScope.launch {
            activityQueueService.refreshHistory()
        }
    }

    fun refreshBlocklist() {
        viewModelScope.launch {
            activityQueueService.refreshBlocklist()
        }
    }

    fun refreshMissing() {
        viewModelScope.launch {
            activityQueueService.refreshMissing()
        }
    }

    fun refreshCutoff() {
        viewModelScope.launch {
            activityQueueService.refreshCutoff()
        }
    }

    fun executeAutomaticSearch(item: WantedMediaItem) {
        viewModelScope.launch {
            val instanceId = item.instanceId ?: return@launch
            val repo = instanceManager.getAllArrRepositories().firstOrNull { it.instance.id == instanceId } ?: return@launch
            item.id?.let { repo.executeAutomaticSearch(it) }
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
        QueueSortBy.Progress -> items.orderedSortedBy(sortOrder) { it.progressPercent }
    }
}
