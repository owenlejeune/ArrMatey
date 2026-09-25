package com.dnfapps.arrmatey.viewmodel.details

import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Arrtist
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.usecase.DeleteAlbumFilesUseCase
import com.dnfapps.arrmatey.arr.usecase.DeleteAudiobookFileUseCase
import com.dnfapps.arrmatey.arr.usecase.DeleteEpisodeFileUseCase
import com.dnfapps.arrmatey.arr.usecase.DeleteMediaUseCase
import com.dnfapps.arrmatey.arr.usecase.DeleteMovieFileUseCase
import com.dnfapps.arrmatey.arr.usecase.DeleteQueueItemUseCase
import com.dnfapps.arrmatey.arr.usecase.DeleteSeasonFilesUseCase
import com.dnfapps.arrmatey.arr.usecase.PerformAutomaticSearchUseCase
import com.dnfapps.arrmatey.arr.usecase.SmartAddMediaUseCase
import com.dnfapps.arrmatey.arr.usecase.ToggleMonitorUseCase
import com.dnfapps.arrmatey.arr.usecase.UpdateMediaUseCase
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.arrmatey.model.SmartAddSeerrAction
import com.dnfapps.arrmatey.model.UnifiedMediaDetailsUiState
import com.dnfapps.arrmatey.seerr.api.model.ApprovalStatus
import com.dnfapps.arrmatey.seerr.api.model.MediaRequest
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.usecase.SetRequestApprovalStatusUseCase
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
import dev.shivathapaa.logger.api.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class UnifiedMediaDetailsArrActionsHandler(
    private val toggleMonitorUseCase: ToggleMonitorUseCase,
    private val performAutomaticSearchUseCase: PerformAutomaticSearchUseCase,
    private val updateMediaUseCase: UpdateMediaUseCase,
    private val deleteMediaUseCase: DeleteMediaUseCase,
    private val deleteSeasonFilesUseCase: DeleteSeasonFilesUseCase,
    private val deleteAlbumFilesUseCase: DeleteAlbumFilesUseCase,
    private val deleteMovieFileUseCase: DeleteMovieFileUseCase,
    private val deleteAudiobookFileUseCase: DeleteAudiobookFileUseCase,
    private val deleteEpisodeFileUseCase: DeleteEpisodeFileUseCase,
    private val deleteQueueItemUseCase: DeleteQueueItemUseCase,
    private val smartAddMediaUseCase: SmartAddMediaUseCase,
) {
    private val _addItemStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val addItemStatus: StateFlow<OperationStatus> = _addItemStatus.asStateFlow()

    private val _editStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val editStatus: StateFlow<OperationStatus> = _editStatus.asStateFlow()

    private val _deleteStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val deleteStatus: StateFlow<OperationStatus> = _deleteStatus.asStateFlow()

    private val _deleteSeasonStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val deleteSeasonStatus: StateFlow<OperationStatus> = _deleteSeasonStatus.asStateFlow()

    private val _deleteAlbumStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val deleteAlbumStatus: StateFlow<OperationStatus> = _deleteAlbumStatus.asStateFlow()

    private val _deleteMovieFileStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val deleteMovieFileStatus: StateFlow<OperationStatus> = _deleteMovieFileStatus.asStateFlow()

    private val _deleteAudiobookFileStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val deleteAudiobookFileStatus: StateFlow<OperationStatus> = _deleteAudiobookFileStatus.asStateFlow()

    private val _deleteEpisodeStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val deleteEpisodeStatus: StateFlow<OperationStatus> = _deleteEpisodeStatus.asStateFlow()

    private val _removeQueueItemStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val removeQueueItemStatus: StateFlow<OperationStatus> = _removeQueueItemStatus.asStateFlow()

    private val _lastSearchResult = MutableStateFlow<Boolean?>(null)
    val lastSearchResult: StateFlow<Boolean?> = _lastSearchResult.asStateFlow()

    private val _automaticSearchIds = MutableStateFlow<Set<Long>>(emptySet())
    val automaticSearchIds: StateFlow<Set<Long>> = _automaticSearchIds.asStateFlow()

    private val _pendingSeerrRequest = MutableStateFlow<MediaRequest?>(null)
    val pendingSeerrRequest: StateFlow<MediaRequest?> = _pendingSeerrRequest.asStateFlow()

    fun updateAddItemStatus(status: OperationStatus) {
        _addItemStatus.value = status
    }

    fun resetAddItemStatus() {
        _addItemStatus.value = OperationStatus.Idle
    }

    fun updateEditStatus(status: OperationStatus) {
        _editStatus.value = status
    }

    fun resetEditStatus() {
        _editStatus.value = OperationStatus.Idle
    }

    fun removeQueueItem(
        scope: CoroutineScope,
        queueItem: QueueItem,
        removeFromClient: Boolean,
        addToBlocklist: Boolean,
        skipRedownload: Boolean,
    ) {
        scope.launch {
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

    fun toggleMonitored(
        scope: CoroutineScope,
        repositoryProvider: suspend () -> ArrInstanceRepository?,
        mediaProvider: () -> ArrMedia?,
    ) {
        scope.launch {
            val repository = repositoryProvider() ?: return@launch
            val item = mediaProvider() ?: return@launch
            toggleMonitorUseCase.toggleMedia(item, repository)
        }
    }

    fun performAutomaticLookup(
        scope: CoroutineScope,
        repositoryProvider: suspend () -> ArrInstanceRepository?,
        effectiveIdProvider: () -> Long?,
        resolvedInstanceType: InstanceType?,
    ) {
        val effectiveId = effectiveIdProvider() ?: return
        runSearch(
            scope,
            trackingId = effectiveId,
            repositoryProvider = repositoryProvider,
            effectiveIdProvider = effectiveIdProvider,
            resolvedInstanceType = resolvedInstanceType,
        )
    }

    fun performEpisodeAutomaticLookup(
        scope: CoroutineScope,
        repositoryProvider: suspend () -> ArrInstanceRepository?,
        effectiveIdProvider: () -> Long?,
        resolvedInstanceType: InstanceType?,
        episodeId: Long,
    ) {
        runSearch(
            scope,
            trackingId = episodeId,
            repositoryProvider = repositoryProvider,
            effectiveIdProvider = effectiveIdProvider,
            resolvedInstanceType = resolvedInstanceType,
            episodeId = episodeId,
        )
    }

    fun performSeasonAutomaticLookup(
        scope: CoroutineScope,
        repositoryProvider: suspend () -> ArrInstanceRepository?,
        effectiveIdProvider: () -> Long?,
        resolvedInstanceType: InstanceType?,
        seasonNumber: Int,
    ) {
        val effectiveId = effectiveIdProvider() ?: return
        runSearch(
            scope,
            trackingId = effectiveId,
            repositoryProvider = repositoryProvider,
            effectiveIdProvider = effectiveIdProvider,
            resolvedInstanceType = resolvedInstanceType,
            seasonNumber = seasonNumber,
        )
    }

    fun performAlbumAutomaticLookup(
        scope: CoroutineScope,
        repositoryProvider: suspend () -> ArrInstanceRepository?,
        effectiveIdProvider: () -> Long?,
        resolvedInstanceType: InstanceType?,
        albumId: Long,
    ) {
        runSearch(
            scope,
            trackingId = albumId,
            repositoryProvider = repositoryProvider,
            effectiveIdProvider = effectiveIdProvider,
            resolvedInstanceType = resolvedInstanceType,
            albumId = albumId,
        )
    }

    fun performBookAutomaticLookup(
        scope: CoroutineScope,
        repositoryProvider: suspend () -> ArrInstanceRepository?,
        effectiveIdProvider: () -> Long?,
        resolvedInstanceType: InstanceType?,
        bookId: Long,
    ) {
        runSearch(
            scope,
            trackingId = bookId,
            repositoryProvider = repositoryProvider,
            effectiveIdProvider = effectiveIdProvider,
            resolvedInstanceType = resolvedInstanceType,
            albumId = bookId,
        )
    }

    private fun runSearch(
        scope: CoroutineScope,
        trackingId: Long,
        repositoryProvider: suspend () -> ArrInstanceRepository?,
        effectiveIdProvider: () -> Long?,
        resolvedInstanceType: InstanceType?,
        episodeId: Long? = null,
        seasonNumber: Int? = null,
        albumId: Long? = null,
    ) {
        scope.launch {
            val repository = repositoryProvider() ?: return@launch
            val effectiveId = effectiveIdProvider() ?: return@launch
            updateSearchIds(trackingId, add = true)

            performAutomaticSearchUseCase(
                effectiveId,
                resolvedInstanceType ?: return@launch,
                repository,
                episodeId,
                seasonNumber,
                albumId,
            ).onSuccess { _lastSearchResult.value = true }
                .onError { _, _, _ -> _lastSearchResult.value = false }

            updateSearchIds(trackingId, add = false)
            _lastSearchResult.value = null
        }
    }

    private fun updateSearchIds(
        id: Long,
        add: Boolean,
    ) {
        _automaticSearchIds.update { current ->
            if (add) current + id else current - id
        }
    }

    fun editItem(
        scope: CoroutineScope,
        repositoryProvider: suspend () -> ArrInstanceRepository?,
        effectiveIdProvider: () -> Long?,
        item: ArrMedia,
        moveFiles: Boolean = false,
        onSuccessRefresh: (() -> Unit)? = null,
    ) {
        scope.launch {
            val repository = repositoryProvider() ?: return@launch
            val effectiveId = effectiveIdProvider()
            val contextualItem =
                if (effectiveId != null && effectiveId != item.id && effectiveId != 0L) {
                    when (item) {
                        is ArrSeries -> item.copy(id = effectiveId)
                        is ArrMovie -> item.copy(id = effectiveId)
                        is Arrtist -> item.copy(id = effectiveId)
                        is Author -> item.copy(id = effectiveId)
                        is Audiobook -> item.copy(id = effectiveId)
                        else -> item
                    }
                } else {
                    item
                }
            updateMediaUseCase.edit(contextualItem, moveFiles, repository).onSuccess {
                contextualItem.id?.let { id ->
                    repository.getMediaDetails(id)
                }
                onSuccessRefresh?.invoke()
            }
        }
    }

    fun updateAlbum(
        scope: CoroutineScope,
        repositoryProvider: suspend () -> ArrInstanceRepository?,
        album: ArrAlbum,
        onSuccessReobserve: () -> Unit,
    ) {
        scope.launch {
            val repository = repositoryProvider() ?: return@launch
            updateMediaUseCase
                .updateAlbum(album, repository)
                .onSuccess {
                    delay(1.seconds)
                    repository.resetEditItemStatus()
                    onSuccessReobserve()
                }.onError { _, _, _ ->
                    delay(3.seconds)
                    repository.resetEditItemStatus()
                }
        }
    }

    fun deleteMedia(
        scope: CoroutineScope,
        repositoryProvider: suspend () -> ArrInstanceRepository?,
        effectiveIdProvider: () -> Long?,
        deleteFiles: Boolean,
        addImportExclusion: Boolean,
    ) {
        scope.launch {
            val repository = repositoryProvider() ?: return@launch
            val effectiveId = effectiveIdProvider() ?: return@launch
            deleteMediaUseCase(effectiveId, deleteFiles, addImportExclusion, repository)
                .collect { status ->
                    _deleteStatus.value = status
                }
        }
    }

    fun deleteSeasonFiles(
        scope: CoroutineScope,
        repositoryProvider: suspend () -> ArrInstanceRepository?,
        effectiveIdProvider: () -> Long?,
        seasonNumber: Int,
        onSuccessRefresh: () -> Unit,
    ) {
        scope.launch {
            val repository = repositoryProvider() ?: return@launch
            val effectiveId = effectiveIdProvider() ?: return@launch
            deleteSeasonFilesUseCase(effectiveId, seasonNumber, repository)
                .collect { status ->
                    _deleteSeasonStatus.value = status
                    if (status is OperationStatus.Success) {
                        delay(500.milliseconds)
                        _deleteSeasonStatus.value = OperationStatus.Idle
                        onSuccessRefresh()
                    } else if (status is OperationStatus.Error) {
                        delay(2000.milliseconds)
                        _deleteSeasonStatus.value = OperationStatus.Idle
                    }
                }
        }
    }

    fun deleteAlbumFiles(
        scope: CoroutineScope,
        repositoryProvider: suspend () -> ArrInstanceRepository?,
        effectiveIdProvider: () -> Long?,
        albumId: Long,
        onSuccessRefresh: () -> Unit,
    ) {
        scope.launch {
            val repository = repositoryProvider() ?: return@launch
            val effectiveId = effectiveIdProvider() ?: return@launch
            deleteAlbumFilesUseCase(effectiveId, albumId, repository)
                .collect { status ->
                    _deleteAlbumStatus.value = status
                    if (status is OperationStatus.Success) {
                        delay(500.milliseconds)
                        _deleteAlbumStatus.value = OperationStatus.Idle
                        onSuccessRefresh()
                    } else if (status is OperationStatus.Error) {
                        delay(2000.milliseconds)
                        _deleteAlbumStatus.value = OperationStatus.Idle
                    }
                }
        }
    }

    fun deleteMovieFile(
        scope: CoroutineScope,
        repositoryProvider: suspend () -> ArrInstanceRepository?,
        mediaProvider: () -> ArrMedia?,
        onSuccessRefresh: () -> Unit,
    ) {
        scope.launch {
            val repository = repositoryProvider() ?: return@launch
            val movie = mediaProvider() as? ArrMovie ?: return@launch
            val movieId = movie.id?.takeIf { it != 0L } ?: return@launch
            val movieFileId = movie.movieFile?.id ?: movie.movieFileId?.toLong()
            if (movieFileId == null) {
                _deleteMovieFileStatus.value = OperationStatus.Error(message = "No movie file to delete")
                delay(2000.milliseconds)
                _deleteMovieFileStatus.value = OperationStatus.Idle
                return@launch
            }
            deleteMovieFileUseCase(movieId, movieFileId, repository)
                .collect { status ->
                    _deleteMovieFileStatus.value = status
                    if (status is OperationStatus.Success) {
                        delay(500.milliseconds)
                        _deleteMovieFileStatus.value = OperationStatus.Idle
                        onSuccessRefresh()
                    } else if (status is OperationStatus.Error) {
                        delay(2000.milliseconds)
                        _deleteMovieFileStatus.value = OperationStatus.Idle
                    }
                }
        }
    }

    fun deleteAudiobookFile(
        scope: CoroutineScope,
        repositoryProvider: suspend () -> ArrInstanceRepository?,
        mediaProvider: () -> ArrMedia?,
        onSuccessRefresh: () -> Unit,
    ) {
        scope.launch {
            val repository = repositoryProvider() ?: return@launch
            val audiobook = mediaProvider() as? Audiobook ?: return@launch
            val audiobookId = audiobook.id?.takeIf { it != 0L } ?: return@launch
            val fileIds = audiobook.files.map { it.id }
            if (fileIds.isEmpty()) {
                _deleteAudiobookFileStatus.value = OperationStatus.Error(message = "No audiobook file to delete")
                delay(2000.milliseconds)
                _deleteAudiobookFileStatus.value = OperationStatus.Idle
                return@launch
            }
            deleteAudiobookFileUseCase(audiobookId, fileIds, repository)
                .collect { status ->
                    _deleteAudiobookFileStatus.value = status
                    if (status is OperationStatus.Success) {
                        delay(500.milliseconds)
                        _deleteAudiobookFileStatus.value = OperationStatus.Idle
                        onSuccessRefresh()
                    } else if (status is OperationStatus.Error) {
                        delay(2000.milliseconds)
                        _deleteAudiobookFileStatus.value = OperationStatus.Idle
                    }
                }
        }
    }

    fun deleteEpisodeFile(
        scope: CoroutineScope,
        repositoryProvider: suspend () -> ArrInstanceRepository?,
        effectiveIdProvider: () -> Long?,
        episodeId: Long,
        onSuccessRefresh: () -> Unit,
    ) {
        scope.launch {
            val repository = repositoryProvider() ?: return@launch
            val effectiveId = effectiveIdProvider() ?: return@launch
            deleteEpisodeFileUseCase(effectiveId, episodeId, repository)
                .collect { status ->
                    _deleteEpisodeStatus.value = status
                    if (status is OperationStatus.Success) {
                        delay(500.milliseconds)
                        _deleteEpisodeStatus.value = OperationStatus.Idle
                        onSuccessRefresh()
                    } else if (status is OperationStatus.Error) {
                        delay(2000.milliseconds)
                        _deleteEpisodeStatus.value = OperationStatus.Idle
                    }
                }
        }
    }

    fun toggleSeasonMonitored(
        scope: CoroutineScope,
        repositoryProvider: suspend () -> ArrInstanceRepository?,
        effectiveIdProvider: () -> Long?,
        seasonNumber: Int,
    ) {
        scope.launch {
            val repository = repositoryProvider() ?: return@launch
            val effectiveId = effectiveIdProvider() ?: return@launch
            toggleMonitorUseCase.toggleSeason(effectiveId, seasonNumber, repository)
        }
    }

    fun toggleEpisodeMonitored(
        scope: CoroutineScope,
        repositoryProvider: suspend () -> ArrInstanceRepository?,
        episode: Episode,
    ) {
        scope.launch {
            val repository = repositoryProvider() ?: return@launch
            toggleMonitorUseCase.toggleEpisode(episode, repository)
        }
    }

    fun toggleAlbumMonitored(
        scope: CoroutineScope,
        repositoryProvider: suspend () -> ArrInstanceRepository?,
        album: ArrAlbum,
    ) {
        scope.launch {
            val repository = repositoryProvider() ?: return@launch
            toggleMonitorUseCase.toggleAlbum(album, repository)
        }
    }

    fun toggleBookMonitored(
        scope: CoroutineScope,
        repositoryProvider: suspend () -> ArrInstanceRepository?,
        book: Book,
    ) {
        scope.launch {
            val repository = repositoryProvider() ?: return@launch
            toggleMonitorUseCase.toggleBook(book, repository)
        }
    }

    fun toggleBookSeriesMonitored(
        scope: CoroutineScope,
        repositoryProvider: suspend () -> ArrInstanceRepository?,
        books: List<Book>,
    ) {
        scope.launch {
            val repository = repositoryProvider() ?: return@launch
            books.forEach { book ->
                toggleMonitorUseCase.toggleBook(book, repository)
            }
        }
    }

    fun smartAdd(
        scope: CoroutineScope,
        resolvedInstanceType: InstanceType?,
        requestType: RequestType?,
        instanceType: InstanceType?,
        item: ArrMedia,
        searchOnAdd: Boolean = false,
        targetInstanceId: Long? = null,
        uiState: UnifiedMediaDetailsUiState,
        preferencesStore: PreferencesStore,
        targetRepoProvider: (Long) -> ArrInstanceRepository?,
        activeRepoProvider: suspend () -> ArrInstanceRepository?,
        selectedInstanceIdProvider: () -> Long?,
        addSheetTargetInstanceIdProvider: () -> Long?,
        selectInstance: (Long) -> Unit,
        refresh: () -> Unit,
        logger: Logger,
    ) {
        scope.launch {
            val type = resolvedInstanceType
            if (type == null) {
                logger.error {
                    "UnifiedMediaDetailsViewModel.smartAdd: resolvedInstanceType is null (requestType=$requestType, instanceType=$instanceType); cannot add '${item.title}'"
                }
                emitFallbackAddError("Unsupported media type")
                return@launch
            }

            val successState = uiState as? UnifiedMediaDetailsUiState.Success
            val seerrMediaDetails = successState?.seerrMedia
            val pendingRequest = seerrMediaDetails?.mediaInfo?.requests?.firstOrNull { it.status == 1 }

            if (pendingRequest != null) {
                val action = preferencesStore.smartAddSeerrAction.first()
                if (action == SmartAddSeerrAction.AlwaysAsk) {
                    _pendingSeerrRequest.value = pendingRequest
                } else if (action == SmartAddSeerrAction.Approve) {
                    handlePendingRequestActionInternal(
                        scope = scope,
                        preferencesStore = preferencesStore,
                        requestId = pendingRequest.id,
                        action = SmartAddSeerrAction.Approve,
                        rememberChoice = false,
                        refresh = refresh,
                    )
                } else if (action == SmartAddSeerrAction.Decline) {
                    handlePendingRequestActionInternal(
                        scope = scope,
                        preferencesStore = preferencesStore,
                        requestId = pendingRequest.id,
                        action = SmartAddSeerrAction.Decline,
                        rememberChoice = false,
                        refresh = refresh,
                    )
                }
            }

            val effectiveInstanceId =
                targetInstanceId ?: addSheetTargetInstanceIdProvider() ?: selectedInstanceIdProvider()

            val targetRepo =
                if (effectiveInstanceId != null) {
                    targetRepoProvider(effectiveInstanceId)
                } else {
                    activeRepoProvider()
                }

            if (targetRepo == null) {
                logger.error {
                    "UnifiedMediaDetailsViewModel.smartAdd: no repository resolved (type=$type, effectiveInstanceId=$effectiveInstanceId); cannot add '${item.title}'"
                }
                emitFallbackAddError("No instance available")
                return@launch
            }

            val collectJob =
                launch {
                    targetRepo.addItemStatus.collect { _addItemStatus.value = it }
                }

            logger.info {
                "UnifiedMediaDetailsViewModel.smartAdd: adding '${item.title}' to instance ${targetRepo.instance.id} (${targetRepo.instance.label}) type=$type searchOnAdd=$searchOnAdd"
            }

            smartAddMediaUseCase(
                instanceType = type,
                repository = targetRepo,
                item = item,
                searchOnAdd = searchOnAdd,
            )
            if (effectiveInstanceId != null) {
                selectInstance(effectiveInstanceId)
            }
            refresh()
            collectJob.cancel()
        }
    }

    fun handlePendingRequestAction(
        scope: CoroutineScope,
        preferencesStore: PreferencesStore,
        seerrRepoProvider: suspend () -> SeerrInstanceRepository?,
        setRequestApprovalStatusUseCase: SetRequestApprovalStatusUseCase,
        requestId: Long,
        action: SmartAddSeerrAction,
        rememberChoice: Boolean,
        refresh: () -> Unit,
    ) {
        scope.launch {
            if (rememberChoice) {
                preferencesStore.setSmartAddSeerrAction(action)
            }

            val seerrRepo = seerrRepoProvider() ?: return@launch
            if (action == SmartAddSeerrAction.Approve) {
                setRequestApprovalStatusUseCase(
                    requestId = requestId,
                    approvalStatus = ApprovalStatus.Approve,
                    repository = seerrRepo,
                )
            } else if (action == SmartAddSeerrAction.Decline) {
                setRequestApprovalStatusUseCase(
                    requestId = requestId,
                    approvalStatus = ApprovalStatus.Decline,
                    repository = seerrRepo,
                )
            }
            _pendingSeerrRequest.value = null
            refresh()
        }
    }

    private suspend fun handlePendingRequestActionInternal(
        scope: CoroutineScope,
        preferencesStore: PreferencesStore,
        requestId: Long,
        action: SmartAddSeerrAction,
        rememberChoice: Boolean,
        refresh: () -> Unit,
    ) {
        _pendingSeerrRequest.value = null
        refresh()
    }

    fun dismissPendingRequestDialog() {
        _pendingSeerrRequest.value = null
    }

    private suspend fun emitFallbackAddError(message: String) {
        _addItemStatus.value = OperationStatus.Error(message = message)
        delay(1500.milliseconds)
        _addItemStatus.value = OperationStatus.Idle
    }
}
