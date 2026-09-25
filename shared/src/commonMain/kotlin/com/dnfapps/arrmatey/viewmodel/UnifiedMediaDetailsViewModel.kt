package com.dnfapps.arrmatey.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.arr.service.ActivityQueueService
import com.dnfapps.arrmatey.arr.usecase.DeleteAlbumFilesUseCase
import com.dnfapps.arrmatey.arr.usecase.DeleteAudiobookFileUseCase
import com.dnfapps.arrmatey.arr.usecase.DeleteEpisodeFileUseCase
import com.dnfapps.arrmatey.arr.usecase.DeleteMediaUseCase
import com.dnfapps.arrmatey.arr.usecase.DeleteMovieFileUseCase
import com.dnfapps.arrmatey.arr.usecase.DeleteQueueItemUseCase
import com.dnfapps.arrmatey.arr.usecase.DeleteSeasonFilesUseCase
import com.dnfapps.arrmatey.arr.usecase.GetInstancePresencesUseCase
import com.dnfapps.arrmatey.arr.usecase.GetUnifiedMediaDetailsUseCase
import com.dnfapps.arrmatey.arr.usecase.PerformAutomaticSearchUseCase
import com.dnfapps.arrmatey.arr.usecase.PerformRefreshUseCase
import com.dnfapps.arrmatey.arr.usecase.SmartAddMediaUseCase
import com.dnfapps.arrmatey.arr.usecase.ToggleMonitorUseCase
import com.dnfapps.arrmatey.arr.usecase.UpdateMediaUseCase
import com.dnfapps.arrmatey.client.paging.PagedData
import com.dnfapps.arrmatey.datastore.InstancePreferences
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.usecase.GetArrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.instances.usecase.GetBazarrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.instances.usecase.GetSeerrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.instances.usecase.GetTracearrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.instances.usecase.ObserveInstancePreferencesUseCase
import com.dnfapps.arrmatey.instances.usecase.ObserveScopedReposByTypeUseCase
import com.dnfapps.arrmatey.instances.usecase.UpdateInstancePreferencesUseCase
import com.dnfapps.arrmatey.model.AddSheetUiState
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.arrmatey.model.SmartAddSeerrAction
import com.dnfapps.arrmatey.model.TracearrMediaUiState
import com.dnfapps.arrmatey.model.TracearrStatsWindowType
import com.dnfapps.arrmatey.model.UnifiedMediaDetailsUiState
import com.dnfapps.arrmatey.seerr.api.model.DiscoverResult
import com.dnfapps.arrmatey.seerr.api.model.IssueType
import com.dnfapps.arrmatey.seerr.api.model.MediaRequest
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.api.model.SeerrUser
import com.dnfapps.arrmatey.seerr.api.model.Service
import com.dnfapps.arrmatey.seerr.api.model.ServiceDetails
import com.dnfapps.arrmatey.seerr.state.MediaButtonState
import com.dnfapps.arrmatey.seerr.state.ReportIssueUiState
import com.dnfapps.arrmatey.seerr.usecase.CancelRequestUseCase
import com.dnfapps.arrmatey.seerr.usecase.ClearSeerrMediaDataUseCase
import com.dnfapps.arrmatey.seerr.usecase.GetRecommendationsUseCase
import com.dnfapps.arrmatey.seerr.usecase.GetSimilarUseCase
import com.dnfapps.arrmatey.seerr.usecase.MarkSeerrMediaAsAvailableUseCase
import com.dnfapps.arrmatey.seerr.usecase.RemoveSeerrMediaFileUseCase
import com.dnfapps.arrmatey.seerr.usecase.SetRequestApprovalStatusUseCase
import com.dnfapps.arrmatey.seerr.usecase.SubmitIssueUseCase
import com.dnfapps.arrmatey.seerr.usecase.SubmitRequestUseCase
import com.dnfapps.arrmatey.viewmodel.details.UnifiedMediaDetailsArrActionsHandler
import com.dnfapps.arrmatey.viewmodel.details.UnifiedMediaDetailsDataObserver
import com.dnfapps.arrmatey.viewmodel.details.UnifiedMediaDetailsInstanceHandler
import com.dnfapps.arrmatey.viewmodel.details.UnifiedMediaDetailsIssueHandler
import com.dnfapps.arrmatey.viewmodel.details.UnifiedMediaDetailsRecommendationsHandler
import com.dnfapps.arrmatey.viewmodel.details.UnifiedMediaDetailsSeerrHandler
import com.dnfapps.arrmatey.viewmodel.details.UnifiedMediaDetailsSeerrServiceHandler
import com.dnfapps.arrmatey.viewmodel.details.UnifiedMediaDetailsTracearrHandler
import dev.shivathapaa.logger.api.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class UnifiedMediaDetailsViewModel(
    private val arrId: Long?,
    private val tmdbId: Long?,
    private val tvdbId: Long?,
    private val instanceType: InstanceType?,
    private val requestType: RequestType?,
    initialForcedInstanceId: Long? = null,
    getUnifiedMediaDetailsUseCase: GetUnifiedMediaDetailsUseCase,
    smartAddMediaUseCase: SmartAddMediaUseCase,
    private val getArrInstanceRepositoryUseCase: GetArrInstanceRepositoryUseCase,
    getSeerrInstanceRepositoryUseCase: GetSeerrInstanceRepositoryUseCase,
    getBazarrInstanceRepositoryUseCase: GetBazarrInstanceRepositoryUseCase,
    private val getTracearrInstanceRepositoryUseCase: GetTracearrInstanceRepositoryUseCase,
    toggleMonitorUseCase: ToggleMonitorUseCase,
    updateMediaUseCase: UpdateMediaUseCase,
    deleteMediaUseCase: DeleteMediaUseCase,
    private val performRefreshUseCase: PerformRefreshUseCase,
    performAutomaticSearchUseCase: PerformAutomaticSearchUseCase,
    submitRequestUseCase: SubmitRequestUseCase,
    cancelRequestUseCase: CancelRequestUseCase,
    private val setRequestApprovalStatusUseCase: SetRequestApprovalStatusUseCase,
    deleteSeasonFilesUseCase: DeleteSeasonFilesUseCase,
    deleteAlbumFilesUseCase: DeleteAlbumFilesUseCase,
    deleteMovieFileUseCase: DeleteMovieFileUseCase,
    deleteAudiobookFileUseCase: DeleteAudiobookFileUseCase,
    deleteEpisodeFileUseCase: DeleteEpisodeFileUseCase,
    submitIssueUseCase: SubmitIssueUseCase,
    observeInstancePreferencesUseCase: ObserveInstancePreferencesUseCase,
    updateInstancePreferencesUseCase: UpdateInstancePreferencesUseCase,
    observeScopedReposByTypeUseCase: ObserveScopedReposByTypeUseCase,
    getInstancePresencesUseCase: GetInstancePresencesUseCase,
    deleteQueueItemUseCase: DeleteQueueItemUseCase,
    private val activityQueueService: ActivityQueueService,
    removeSeerrMediaFileUseCase: RemoveSeerrMediaFileUseCase,
    clearSeerrMediaDataUseCase: ClearSeerrMediaDataUseCase,
    markSeerrMediaAsAvailableUseCase: MarkSeerrMediaAsAvailableUseCase,
    getRecommendationsUseCase: GetRecommendationsUseCase,
    getSimilarUseCase: GetSimilarUseCase,
    private val preferencesStore: PreferencesStore,
    private val logger: Logger,
) : ViewModel() {
    private var seerrMediaId: Long? = null

    val resolvedInstanceType: InstanceType? =
        when (requestType) {
            RequestType.Movie -> InstanceType.Radarr
            RequestType.Tv -> InstanceType.Sonarr
            else -> instanceType
        }

    val resolvedRequestType: RequestType? =
        when (resolvedInstanceType) {
            InstanceType.Radarr -> RequestType.Movie
            InstanceType.Sonarr -> RequestType.Tv
            else -> requestType
        }

    private val _isMonitored = MutableStateFlow(false)
    val isMonitored: StateFlow<Boolean> = _isMonitored.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _uiState = MutableStateFlow<UnifiedMediaDetailsUiState>(UnifiedMediaDetailsUiState.Initial)
    val uiState: StateFlow<UnifiedMediaDetailsUiState> = _uiState.asStateFlow()

    private val instanceHandler =
        UnifiedMediaDetailsInstanceHandler(
            initialForcedInstanceId = initialForcedInstanceId,
            arrId = arrId,
            resolvedInstanceType = resolvedInstanceType,
            resolvedRequestType = resolvedRequestType,
            scope = viewModelScope,
            getArrInstanceRepositoryUseCase = getArrInstanceRepositoryUseCase,
            getSeerrInstanceRepositoryUseCase = getSeerrInstanceRepositoryUseCase,
            getBazarrInstanceRepositoryUseCase = getBazarrInstanceRepositoryUseCase,
            observeInstancePreferencesUseCase = observeInstancePreferencesUseCase,
            updateInstancePreferencesUseCase = updateInstancePreferencesUseCase,
            observeScopedReposByTypeUseCase = observeScopedReposByTypeUseCase,
            preferencesStore = preferencesStore,
            activityQueueService = activityQueueService,
        )

    private val seerrServiceHandler =
        UnifiedMediaDetailsSeerrServiceHandler(
            scope = viewModelScope,
            resolvedRequestType = resolvedRequestType,
            uiStateFlow = _uiState,
            isSeerrConfiguredFlow = instanceHandler.isSeerrConfigured,
        )

    private val seerrHandler =
        UnifiedMediaDetailsSeerrHandler(
            submitRequestUseCase = submitRequestUseCase,
            cancelRequestUseCase = cancelRequestUseCase,
            setRequestApprovalStatusUseCase = setRequestApprovalStatusUseCase,
            removeSeerrMediaFileUseCase = removeSeerrMediaFileUseCase,
            clearSeerrMediaDataUseCase = clearSeerrMediaDataUseCase,
            markSeerrMediaAsAvailableUseCase = markSeerrMediaAsAvailableUseCase,
        )

    private val issueHandler =
        UnifiedMediaDetailsIssueHandler(
            submitIssueUseCase = submitIssueUseCase,
        )

    private val tracearrHandler = UnifiedMediaDetailsTracearrHandler()

    private val recommendationsHandler =
        UnifiedMediaDetailsRecommendationsHandler(
            getRecommendationsUseCase = getRecommendationsUseCase,
            getSimilarUseCase = getSimilarUseCase,
        )

    private val arrActionsHandler =
        UnifiedMediaDetailsArrActionsHandler(
            toggleMonitorUseCase = toggleMonitorUseCase,
            performAutomaticSearchUseCase = performAutomaticSearchUseCase,
            updateMediaUseCase = updateMediaUseCase,
            deleteMediaUseCase = deleteMediaUseCase,
            deleteSeasonFilesUseCase = deleteSeasonFilesUseCase,
            deleteAlbumFilesUseCase = deleteAlbumFilesUseCase,
            deleteMovieFileUseCase = deleteMovieFileUseCase,
            deleteAudiobookFileUseCase = deleteAudiobookFileUseCase,
            deleteEpisodeFileUseCase = deleteEpisodeFileUseCase,
            deleteQueueItemUseCase = deleteQueueItemUseCase,
            smartAddMediaUseCase = smartAddMediaUseCase,
        )

    private val dataObserver =
        UnifiedMediaDetailsDataObserver(
            scope = viewModelScope,
            arrId = arrId,
            tmdbId = tmdbId,
            tvdbId = tvdbId,
            resolvedInstanceType = resolvedInstanceType,
            resolvedRequestType = resolvedRequestType,
            getUnifiedMediaDetailsUseCase = getUnifiedMediaDetailsUseCase,
            getInstancePresencesUseCase = getInstancePresencesUseCase,
            preferencesStore = preferencesStore,
            instanceHandler = instanceHandler,
            arrActionsHandler = arrActionsHandler,
            seerrServiceHandler = seerrServiceHandler,
            tracearrHandler = tracearrHandler,
            getTracearrInstanceRepositoryUseCase = getTracearrInstanceRepositoryUseCase,
            onIsMonitoredUpdated = { _isMonitored.value = it },
        )

    val currentUser: StateFlow<SeerrUser?> = seerrServiceHandler.currentUser
    val pendingSeerrRequest: StateFlow<MediaRequest?> = arrActionsHandler.pendingSeerrRequest
    val qualityProfiles: StateFlow<List<QualityProfile>> = instanceHandler.qualityProfiles
    val rootFolders: StateFlow<List<RootFolder>> = instanceHandler.rootFolders
    val tags: StateFlow<List<Tag>> = instanceHandler.tags

    val addItemStatus: StateFlow<OperationStatus> = arrActionsHandler.addItemStatus
    val editStatus: StateFlow<OperationStatus> = arrActionsHandler.editStatus
    val deleteStatus: StateFlow<OperationStatus> = arrActionsHandler.deleteStatus
    val deleteSeasonStatus: StateFlow<OperationStatus> = arrActionsHandler.deleteSeasonStatus
    val deleteAlbumStatus: StateFlow<OperationStatus> = arrActionsHandler.deleteAlbumStatus
    val deleteMovieFileStatus: StateFlow<OperationStatus> = arrActionsHandler.deleteMovieFileStatus
    val deleteAudiobookFileStatus: StateFlow<OperationStatus> = arrActionsHandler.deleteAudiobookFileStatus
    val deleteEpisodeStatus: StateFlow<OperationStatus> = arrActionsHandler.deleteEpisodeStatus
    val removeQueueItemStatus: StateFlow<OperationStatus> = arrActionsHandler.removeQueueItemStatus
    val requestStatus: StateFlow<OperationStatus> = seerrHandler.requestStatus
    val lastSearchResult: StateFlow<Boolean?> = arrActionsHandler.lastSearchResult

    val isRequestSheetVisible: StateFlow<Boolean> = seerrHandler.isRequestSheetVisible
    val isReportIssueSheetVisible: StateFlow<Boolean> = issueHandler.isReportIssueSheetVisible

    val radarrServices: StateFlow<List<Service>> = seerrServiceHandler.radarrServices
    val sonarrServices: StateFlow<List<Service>> = seerrServiceHandler.sonarrServices
    val users: StateFlow<List<SeerrUser>> = seerrServiceHandler.users
    val serviceDetails: StateFlow<ServiceDetails?> = seerrServiceHandler.serviceDetails

    val reportIssueState: StateFlow<ReportIssueUiState> =
        issueHandler.createCombinedReportIssueState(
            scope = viewModelScope,
            uiStateFlow = _uiState,
            onSeerrMediaIdExtracted = { id -> seerrMediaId = id },
        )

    val isViewRequestSheetVisible: StateFlow<Boolean> = seerrHandler.isViewRequestSheetVisible
    val tracearrState: StateFlow<TracearrMediaUiState> = tracearrHandler.tracearrState
    val isRequest4k: StateFlow<Boolean> = seerrHandler.isRequest4k
    val automaticSearchIds: StateFlow<Set<Long>> = arrActionsHandler.automaticSearchIds

    val recommendationsState: StateFlow<PagedData<DiscoverResult>> = recommendationsHandler.recommendationsState
    val similarState: StateFlow<PagedData<DiscoverResult>> = recommendationsHandler.similarState

    val selectedInstanceId: StateFlow<Long?> = instanceHandler.selectedInstanceId
    val availableInstances: StateFlow<List<Instance>> = instanceHandler.availableInstances
    val activeInstance: StateFlow<Instance?> = instanceHandler.activeInstance
    val activeSeerrInstance: StateFlow<Instance?> = instanceHandler.activeSeerrInstance
    val isSeerrConfigured: StateFlow<Boolean> = instanceHandler.isSeerrConfigured
    val isArrConfigured: StateFlow<Boolean> = instanceHandler.isArrConfigured
    val buttonState: StateFlow<MediaButtonState> = seerrServiceHandler.buttonState
    val addSheetUiState: StateFlow<AddSheetUiState> = instanceHandler.addSheetUiState
    val preferences: StateFlow<InstancePreferences> = instanceHandler.preferences

    init {
        dataObserver.observeData(_uiState)
        recommendationsHandler.observeRecommendations(
            scope = viewModelScope,
            seerrRepoFlow = instanceHandler.seerrRepositoryFlow,
            uiStateFlow = _uiState,
            initialTmdbId = tmdbId,
            initialRequestType = resolvedRequestType,
        )
        viewModelScope.launch {
            activityQueueService.manualRefresh()
        }
    }

    fun loadNextRecommendationsPage() {
        recommendationsHandler.loadNextRecommendationsPage()
    }

    fun loadNextSimilarPage() {
        recommendationsHandler.loadNextSimilarPage()
    }

    fun selectInstance(instanceId: Long) {
        instanceHandler.selectInstance(
            instanceId = instanceId,
            currentUiState = _uiState.value,
            onUiStateUpdated = { _uiState.value = it },
            onIsMonitoredUpdated = { _isMonitored.value = it },
        )
    }

    fun setAddSheetTargetInstance(instance: Instance?) {
        instanceHandler.setAddSheetTargetInstance(instance)
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val repository = instanceHandler.getActiveArrRepository()
                val effectiveId = instanceHandler.getEffectiveArrId(_uiState.value)
                val seerrRepository = instanceHandler.getSeerrRepository()
                coroutineScope {
                    if (repository != null) {
                        launch { repository.refreshQualityProfiles() }
                        launch { repository.refreshRootFolders() }
                        launch { repository.refreshTags() }
                        if (effectiveId != null && effectiveId != 0L) {
                            launch { repository.getMediaDetails(effectiveId) }
                        }
                    }
                    if (seerrRepository != null && tmdbId != null && resolvedRequestType != null) {
                        launch { seerrRepository.refreshMediaDetails(tmdbId, resolvedRequestType) }
                    }
                    launch { activityQueueService.manualRefresh() }
                    launch { recommendationsHandler.refresh() }
                }
                dataObserver.observeData(_uiState)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun performRefresh() {
        viewModelScope.launch {
            val repository = instanceHandler.getActiveArrRepository() ?: return@launch
            val effectiveId = instanceHandler.getEffectiveArrId(_uiState.value) ?: return@launch
            launch {
                performRefreshUseCase(effectiveId, resolvedInstanceType ?: return@launch, repository)
            }
            launch {
                activityQueueService.manualRefresh()
            }
        }
    }

    // Sheet Visibility Actions
    fun showRequestSheet(is4k: Boolean = false) {
        seerrHandler.showRequestSheet(is4k)
    }

    fun hideRequestSheet() {
        seerrHandler.hideRequestSheet()
    }

    fun showReportIssueSheet() {
        issueHandler.showReportIssueSheet()
    }

    fun hideReportIssueSheet() {
        issueHandler.hideReportIssueSheet()
    }

    fun showViewRequestSheet() {
        seerrHandler.showViewRequestSheet()
    }

    fun hideViewRequestSheet() {
        seerrHandler.hideViewRequestSheet()
    }

    fun removeQueueItem(
        queueItem: QueueItem,
        removeFromClient: Boolean,
        addToBlocklist: Boolean,
        skipRedownload: Boolean,
    ) {
        arrActionsHandler.removeQueueItem(
            scope = viewModelScope,
            queueItem = queueItem,
            removeFromClient = removeFromClient,
            addToBlocklist = addToBlocklist,
            skipRedownload = skipRedownload,
        )
    }

    // Smart Actions
    fun smartAdd(
        item: ArrMedia,
        searchOnAdd: Boolean = false,
        targetInstanceId: Long? = null,
    ) {
        arrActionsHandler.smartAdd(
            scope = viewModelScope,
            resolvedInstanceType = resolvedInstanceType,
            requestType = requestType,
            instanceType = instanceType,
            item = item,
            searchOnAdd = searchOnAdd,
            targetInstanceId = targetInstanceId,
            uiState = uiState.value,
            preferencesStore = preferencesStore,
            targetRepoProvider = { getArrInstanceRepositoryUseCase(it) },
            activeRepoProvider = { instanceHandler.getActiveArrRepository() },
            selectedInstanceIdProvider = { instanceHandler.selectedInstanceId.value },
            addSheetTargetInstanceIdProvider = {
                instanceHandler.addSheetUiState.value.targetInstance
                    ?.id
            },
            selectInstance = ::selectInstance,
            refresh = ::refresh,
            logger = logger,
        )
    }

    fun handlePendingRequestAction(
        requestId: Long,
        action: SmartAddSeerrAction,
        rememberChoice: Boolean,
    ) {
        arrActionsHandler.handlePendingRequestAction(
            scope = viewModelScope,
            preferencesStore = preferencesStore,
            seerrRepoProvider = { instanceHandler.getSeerrRepository() },
            setRequestApprovalStatusUseCase = setRequestApprovalStatusUseCase,
            requestId = requestId,
            action = action,
            rememberChoice = rememberChoice,
            refresh = ::refresh,
        )
    }

    fun dismissPendingRequestDialog() {
        arrActionsHandler.dismissPendingRequestDialog()
    }

    fun submitRequest(
        profileId: Long? = null,
        rootFolder: String? = null,
        languageProfileId: Long? = null,
        seasons: List<Int>? = null,
        is4k: Boolean = false,
        userId: Long? = null,
    ) {
        seerrHandler.submitRequest(
            scope = viewModelScope,
            repositoryProvider = { instanceHandler.getSeerrRepository() },
            resolvedRequestType = resolvedRequestType,
            tmdbId = tmdbId,
            profileId = profileId,
            rootFolder = rootFolder,
            languageProfileId = languageProfileId,
            seasons = seasons,
            is4k = is4k,
            userId = userId,
            onSuccessRefresh = ::refresh,
        )
    }

    fun approveRequest(
        requestId: Long,
        profileId: Long? = null,
        rootFolder: String? = null,
        languageProfileId: Long? = null,
        seasons: List<Int>? = null,
    ) {
        seerrHandler.approveRequest(
            scope = viewModelScope,
            repositoryProvider = { instanceHandler.getSeerrRepository() },
            requestId = requestId,
            profileId = profileId,
            rootFolder = rootFolder,
            languageProfileId = languageProfileId,
            seasons = seasons,
            onSuccessRefresh = ::refresh,
        )
    }

    fun cancelRequest(requestId: Long) {
        seerrHandler.cancelRequest(
            scope = viewModelScope,
            repositoryProvider = { instanceHandler.getSeerrRepository() },
            requestId = requestId,
            onSuccessRefresh = ::refresh,
        )
    }

    fun declineRequest(requestId: Long) {
        seerrHandler.declineRequest(
            scope = viewModelScope,
            repositoryProvider = { instanceHandler.getSeerrRepository() },
            requestId = requestId,
            onSuccessRefresh = ::refresh,
        )
    }

    fun deleteSeerrMediaFile(is4k: Boolean = false) {
        seerrHandler.deleteSeerrMediaFile(
            scope = viewModelScope,
            repositoryProvider = { instanceHandler.getSeerrRepository() },
            currentMediaIdProvider = {
                (uiState.value as? UnifiedMediaDetailsUiState.Success)?.seerrMedia?.mediaInfo?.id ?: seerrMediaId
            },
            is4k = is4k,
            onSuccessRefresh = ::refresh,
        )
    }

    fun clearSeerrMediaData() {
        seerrHandler.clearSeerrMediaData(
            scope = viewModelScope,
            repositoryProvider = { instanceHandler.getSeerrRepository() },
            currentMediaIdProvider = {
                (uiState.value as? UnifiedMediaDetailsUiState.Success)?.seerrMedia?.mediaInfo?.id ?: seerrMediaId
            },
            onSuccessRefresh = ::refresh,
        )
    }

    fun markSeerrMediaAsAvailable(is4k: Boolean = false) {
        seerrHandler.markSeerrMediaAsAvailable(
            scope = viewModelScope,
            repositoryProvider = { instanceHandler.getSeerrRepository() },
            currentMediaIdProvider = {
                (uiState.value as? UnifiedMediaDetailsUiState.Success)?.seerrMedia?.mediaInfo?.id ?: seerrMediaId
            },
            is4k = is4k,
            onSuccessRefresh = ::refresh,
        )
    }

    // Arr Actions
    fun toggleMonitored() {
        arrActionsHandler.toggleMonitored(
            scope = viewModelScope,
            repositoryProvider = { instanceHandler.getActiveArrRepository() },
            mediaProvider = { instanceHandler.getEffectiveArrMedia(_uiState.value) },
        )
    }

    fun performAutomaticLookup() {
        arrActionsHandler.performAutomaticLookup(
            scope = viewModelScope,
            repositoryProvider = { instanceHandler.getActiveArrRepository() },
            effectiveIdProvider = { instanceHandler.getEffectiveArrId(_uiState.value) },
            resolvedInstanceType = resolvedInstanceType,
        )
    }

    fun performEpisodeAutomaticLookup(episodeId: Long) {
        arrActionsHandler.performEpisodeAutomaticLookup(
            scope = viewModelScope,
            repositoryProvider = { instanceHandler.getActiveArrRepository() },
            effectiveIdProvider = { instanceHandler.getEffectiveArrId(_uiState.value) },
            resolvedInstanceType = resolvedInstanceType,
            episodeId = episodeId,
        )
    }

    fun performSeasonAutomaticLookup(seasonNumber: Int) {
        arrActionsHandler.performSeasonAutomaticLookup(
            scope = viewModelScope,
            repositoryProvider = { instanceHandler.getActiveArrRepository() },
            effectiveIdProvider = { instanceHandler.getEffectiveArrId(_uiState.value) },
            resolvedInstanceType = resolvedInstanceType,
            seasonNumber = seasonNumber,
        )
    }

    fun performAlbumAutomaticLookup(albumId: Long) {
        arrActionsHandler.performAlbumAutomaticLookup(
            scope = viewModelScope,
            repositoryProvider = { instanceHandler.getActiveArrRepository() },
            effectiveIdProvider = { instanceHandler.getEffectiveArrId(_uiState.value) },
            resolvedInstanceType = resolvedInstanceType,
            albumId = albumId,
        )
    }

    fun performBookAutomaticLookup(bookId: Long) {
        arrActionsHandler.performBookAutomaticLookup(
            scope = viewModelScope,
            repositoryProvider = { instanceHandler.getActiveArrRepository() },
            effectiveIdProvider = { instanceHandler.getEffectiveArrId(_uiState.value) },
            resolvedInstanceType = resolvedInstanceType,
            bookId = bookId,
        )
    }

    fun editItem(
        item: ArrMedia,
        moveFiles: Boolean = false,
    ) {
        arrActionsHandler.editItem(
            scope = viewModelScope,
            repositoryProvider = { instanceHandler.getActiveArrRepository() },
            effectiveIdProvider = { instanceHandler.getEffectiveArrId(_uiState.value) },
            item = item,
            moveFiles = moveFiles,
            onSuccessRefresh = ::refresh,
        )
    }

    fun updateAlbum(album: ArrAlbum) {
        arrActionsHandler.updateAlbum(
            scope = viewModelScope,
            repositoryProvider = { instanceHandler.getActiveArrRepository() },
            album = album,
            onSuccessReobserve = { dataObserver.observeData(_uiState) },
        )
    }

    fun resetEditStatus() {
        arrActionsHandler.resetEditStatus()
        viewModelScope.launch {
            instanceHandler.getActiveArrRepository()?.resetEditItemStatus()
        }
    }

    fun resetAddItemStatus() {
        arrActionsHandler.resetAddItemStatus()
    }

    fun deleteMedia(
        deleteFiles: Boolean,
        addImportExclusion: Boolean,
    ) {
        arrActionsHandler.deleteMedia(
            scope = viewModelScope,
            repositoryProvider = { instanceHandler.getActiveArrRepository() },
            effectiveIdProvider = { instanceHandler.getEffectiveArrId(_uiState.value) },
            deleteFiles = deleteFiles,
            addImportExclusion = addImportExclusion,
        )
    }

    fun deleteSeasonFiles(seasonNumber: Int) {
        arrActionsHandler.deleteSeasonFiles(
            scope = viewModelScope,
            repositoryProvider = { instanceHandler.getActiveArrRepository() },
            effectiveIdProvider = { instanceHandler.getEffectiveArrId(_uiState.value) },
            seasonNumber = seasonNumber,
            onSuccessRefresh = ::refresh,
        )
    }

    fun deleteAlbumFiles(albumId: Long) {
        arrActionsHandler.deleteAlbumFiles(
            scope = viewModelScope,
            repositoryProvider = { instanceHandler.getActiveArrRepository() },
            effectiveIdProvider = { instanceHandler.getEffectiveArrId(_uiState.value) },
            albumId = albumId,
            onSuccessRefresh = ::refresh,
        )
    }

    fun deleteMovieFile() {
        arrActionsHandler.deleteMovieFile(
            scope = viewModelScope,
            repositoryProvider = { instanceHandler.getActiveArrRepository() },
            mediaProvider = { instanceHandler.getEffectiveArrMedia(_uiState.value) },
            onSuccessRefresh = ::refresh,
        )
    }

    fun deleteAudiobookFile() {
        arrActionsHandler.deleteAudiobookFile(
            scope = viewModelScope,
            repositoryProvider = { instanceHandler.getActiveArrRepository() },
            mediaProvider = { instanceHandler.getEffectiveArrMedia(_uiState.value) },
            onSuccessRefresh = ::refresh,
        )
    }

    fun deleteEpisodeFile(episodeId: Long) {
        arrActionsHandler.deleteEpisodeFile(
            scope = viewModelScope,
            repositoryProvider = { instanceHandler.getActiveArrRepository() },
            effectiveIdProvider = { instanceHandler.getEffectiveArrId(_uiState.value) },
            episodeId = episodeId,
            onSuccessRefresh = ::refresh,
        )
    }

    fun toggleSeasonMonitored(seasonNumber: Int) {
        arrActionsHandler.toggleSeasonMonitored(
            scope = viewModelScope,
            repositoryProvider = { instanceHandler.getActiveArrRepository() },
            effectiveIdProvider = { instanceHandler.getEffectiveArrId(_uiState.value) },
            seasonNumber = seasonNumber,
        )
    }

    fun toggleEpisodeMonitored(episode: Episode) {
        arrActionsHandler.toggleEpisodeMonitored(
            scope = viewModelScope,
            repositoryProvider = { instanceHandler.getActiveArrRepository() },
            episode = episode,
        )
    }

    fun toggleAlbumMonitored(album: ArrAlbum) {
        arrActionsHandler.toggleAlbumMonitored(
            scope = viewModelScope,
            repositoryProvider = { instanceHandler.getActiveArrRepository() },
            album = album,
        )
    }

    fun toggleBookMonitored(book: Book) {
        arrActionsHandler.toggleBookMonitored(
            scope = viewModelScope,
            repositoryProvider = { instanceHandler.getActiveArrRepository() },
            book = book,
        )
    }

    fun toggleBookSeriesMonitored(books: List<Book>) {
        arrActionsHandler.toggleBookSeriesMonitored(
            scope = viewModelScope,
            repositoryProvider = { instanceHandler.getActiveArrRepository() },
            books = books,
        )
    }

    fun updatePreferences(preferences: InstancePreferences) {
        instanceHandler.updatePreferences(preferences)
    }

    fun setIssueType(issueType: IssueType) {
        issueHandler.setIssueType(issueType)
    }

    fun setIssueMessage(message: String) {
        issueHandler.setIssueMessage(message)
    }

    fun setProblemSeason(season: Int?) {
        issueHandler.setProblemSeason(season)
    }

    fun setProblemEpisode(episode: Int?) {
        issueHandler.setProblemEpisode(episode)
    }

    fun resetIssueState() {
        issueHandler.resetIssueState()
    }

    fun submitIssue() {
        issueHandler.submitIssue(
            scope = viewModelScope,
            seerrMediaIdProvider = { seerrMediaId },
        )
    }

    // Tracearr Actions
    fun selectTracearrStatsWindow(window: TracearrStatsWindowType) {
        tracearrHandler.selectTracearrStatsWindow(window)
    }

    fun loadMoreTracearrHistory() {
        tracearrHandler.loadMoreTracearrHistory(
            scope = viewModelScope,
            getTracearrInstanceRepositoryUseCase = getTracearrInstanceRepositoryUseCase,
            preferencesStore = preferencesStore,
        )
    }
}
