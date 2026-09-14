package com.dnfapps.arrmatey.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
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
import com.dnfapps.arrmatey.datastore.InstancePreferences
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
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
import com.dnfapps.arrmatey.seerr.api.model.IssueType
import com.dnfapps.arrmatey.seerr.api.model.MediaRequest
import com.dnfapps.arrmatey.seerr.api.model.MovieDetails
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.api.model.SeerrUser
import com.dnfapps.arrmatey.seerr.api.model.Service
import com.dnfapps.arrmatey.seerr.api.model.ServiceDetails
import com.dnfapps.arrmatey.seerr.api.model.TvDetails
import com.dnfapps.arrmatey.seerr.api.model.UserPermission
import com.dnfapps.arrmatey.seerr.state.MediaButtonState
import com.dnfapps.arrmatey.seerr.state.ReportIssueUiState
import com.dnfapps.arrmatey.seerr.state.toButtonState
import com.dnfapps.arrmatey.seerr.usecase.CancelRequestUseCase
import com.dnfapps.arrmatey.seerr.usecase.ClearSeerrMediaDataUseCase
import com.dnfapps.arrmatey.seerr.usecase.MarkSeerrMediaAsAvailableUseCase
import com.dnfapps.arrmatey.seerr.usecase.RemoveSeerrMediaFileUseCase
import com.dnfapps.arrmatey.seerr.usecase.SetRequestApprovalStatusUseCase
import com.dnfapps.arrmatey.seerr.usecase.SubmitIssueUseCase
import com.dnfapps.arrmatey.seerr.usecase.SubmitRequestUseCase
import com.dnfapps.arrmatey.viewmodel.details.UnifiedMediaDetailsArrActionsHandler
import com.dnfapps.arrmatey.viewmodel.details.UnifiedMediaDetailsIssueHandler
import com.dnfapps.arrmatey.viewmodel.details.UnifiedMediaDetailsSeerrHandler
import com.dnfapps.arrmatey.viewmodel.details.UnifiedMediaDetailsTracearrHandler
import com.dnfapps.networking.onSuccess
import dev.shivathapaa.logger.api.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class UnifiedMediaDetailsViewModel(
    private val arrId: Long?,
    private val tmdbId: Long?,
    private val tvdbId: Long?,
    private val instanceType: InstanceType?,
    private val requestType: RequestType?,
    initialForcedInstanceId: Long? = null,
    private val getUnifiedMediaDetailsUseCase: GetUnifiedMediaDetailsUseCase,
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
    private val updateInstancePreferencesUseCase: UpdateInstancePreferencesUseCase,
    observeScopedReposByTypeUseCase: ObserveScopedReposByTypeUseCase,
    private val getInstancePresencesUseCase: GetInstancePresencesUseCase,
    deleteQueueItemUseCase: DeleteQueueItemUseCase,
    private val activityQueueService: ActivityQueueService,
    removeSeerrMediaFileUseCase: RemoveSeerrMediaFileUseCase,
    clearSeerrMediaDataUseCase: ClearSeerrMediaDataUseCase,
    markSeerrMediaAsAvailableUseCase: MarkSeerrMediaAsAvailableUseCase,
    private val preferencesStore: PreferencesStore,
    private val logger: Logger,
) : ViewModel() {
    private var seerrMediaId: Long? = null
    private var initialInstanceId: Long? = null

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

    private val _isMonitored = MutableStateFlow(false)
    val isMonitored: StateFlow<Boolean> = _isMonitored.asStateFlow()

    private val _currentUser = MutableStateFlow<SeerrUser?>(null)
    val currentUser: StateFlow<SeerrUser?> = _currentUser.asStateFlow()

    private val _uiState = MutableStateFlow<UnifiedMediaDetailsUiState>(UnifiedMediaDetailsUiState.Initial)
    val uiState: StateFlow<UnifiedMediaDetailsUiState> = _uiState.asStateFlow()

    val pendingSeerrRequest: StateFlow<MediaRequest?> = arrActionsHandler.pendingSeerrRequest

    private val _qualityProfiles = MutableStateFlow<List<QualityProfile>>(emptyList())
    val qualityProfiles: StateFlow<List<QualityProfile>> = _qualityProfiles.asStateFlow()

    private val _rootFolders = MutableStateFlow<List<RootFolder>>(emptyList())
    val rootFolders: StateFlow<List<RootFolder>> = _rootFolders.asStateFlow()

    private val _tags = MutableStateFlow<List<Tag>>(emptyList())
    val tags: StateFlow<List<Tag>> = _tags.asStateFlow()

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

    private val _radarrServices = MutableStateFlow<List<Service>>(emptyList())
    val radarrServices: StateFlow<List<Service>> = _radarrServices.asStateFlow()

    private val _sonarrServices = MutableStateFlow<List<Service>>(emptyList())
    val sonarrServices: StateFlow<List<Service>> = _sonarrServices.asStateFlow()

    private val _users = MutableStateFlow<List<SeerrUser>>(emptyList())
    val users: StateFlow<List<SeerrUser>> = _users.asStateFlow()

    private val _serviceDetails = MutableStateFlow<ServiceDetails?>(null)
    val serviceDetails: StateFlow<ServiceDetails?> = _serviceDetails.asStateFlow()

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

    val resolvedInstanceType =
        when (requestType) {
            RequestType.Movie -> InstanceType.Radarr
            RequestType.Tv -> InstanceType.Sonarr
            else -> instanceType
        }

    val resolvedRequestType =
        when (resolvedInstanceType) {
            InstanceType.Radarr -> RequestType.Movie
            InstanceType.Sonarr -> RequestType.Tv
            else -> requestType
        }

    private val _selectedInstanceId = MutableStateFlow<Long?>(null)
    val selectedInstanceId: StateFlow<Long?> = _selectedInstanceId.asStateFlow()

    private val _instancePresencesMap = MutableStateFlow<Map<Long, ArrMedia?>>(emptyMap())

    private val allArrReposFlow: Flow<List<ArrInstanceRepository>> =
        if (resolvedInstanceType != null) {
            observeScopedReposByTypeUseCase(resolvedInstanceType)
                .map { it.filterIsInstance<ArrInstanceRepository>() }
        } else {
            flowOf(emptyList())
        }

    val availableInstances: StateFlow<List<Instance>> =
        allArrReposFlow
            .map { repos -> repos.map { it.instance } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    private val defaultSelectedArrRepoFlow: Flow<ArrInstanceRepository?> =
        if (resolvedInstanceType != null) {
            getArrInstanceRepositoryUseCase.observeSelected(resolvedInstanceType)
        } else {
            flowOf(null)
        }

    private val activeArrRepoFlow: Flow<ArrInstanceRepository?> =
        combine(
            allArrReposFlow,
            defaultSelectedArrRepoFlow,
            _selectedInstanceId,
        ) { allRepos, defaultRepo, selectedId ->
            if (selectedId != null) {
                allRepos.firstOrNull { it.instance.id == selectedId }
                    ?: getArrInstanceRepositoryUseCase(selectedId)
                    ?: defaultRepo
            } else {
                defaultRepo ?: allRepos.firstOrNull()
            }
        }

    private val seerrRepositoryFlow =
        combine(
            getSeerrInstanceRepositoryUseCase.observeSelected(),
            preferencesStore.combineSeerrArrMedia,
        ) { repo, combine ->
            if (!combine && arrId != null) null else repo
        }

    private val bazarrRepositoryFlow =
        combine(
            getBazarrInstanceRepositoryUseCase.observeSelected(),
            preferencesStore.bazarrDetailsIntegration,
        ) { repo, enabled ->
            if (enabled) repo else null
        }

    val activeInstance: StateFlow<Instance?> =
        activeArrRepoFlow
            .map { it?.instance }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null,
            )

    val activeSeerrInstance: StateFlow<Instance?> =
        seerrRepositoryFlow
            .map { it?.instance }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null,
            )

    val isSeerrConfigured: StateFlow<Boolean> =
        seerrRepositoryFlow
            .map { repo ->
                repo != null && (resolvedRequestType == RequestType.Movie || resolvedRequestType == RequestType.Tv)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

    val buttonState: StateFlow<MediaButtonState> =
        combine(
            uiState,
            _currentUser,
            isSeerrConfigured,
            _radarrServices,
            _sonarrServices,
        ) { state, user, isConfigured, radarr, sonarr ->
            when (state) {
                is UnifiedMediaDetailsUiState.Success -> {
                    if (!isConfigured) {
                        MediaButtonState()
                    } else {
                        val isAdmin = user?.hasPermission(UserPermission.ADMIN) == true
                        val totalSeasonCount = (state.seerrMedia as? TvDetails)?.numberOfSeasons ?: 0
                        val rawButtonState =
                            state.seerrMedia?.mediaInfo.toButtonState(
                                state.seerrMedia?.relatedVideos ?: emptyList(),
                                totalSeasonCount,
                                user?.id,
                                isAdmin,
                            )
                        val has4kServer =
                            when (resolvedRequestType) {
                                RequestType.Movie -> radarr.any { it.is4k }
                                RequestType.Tv -> sonarr.any { it.is4k }
                                else -> false
                            }
                        val existsInAnyArr = state.hasArrId || state.presentInstances.isNotEmpty()
                        if (existsInAnyArr) {
                            rawButtonState.copy(
                                showRequestButton = false,
                                showRequestMoreButton = false,
                                showRequest4kButton = false,
                            )
                        } else {
                            rawButtonState.copy(
                                showRequest4kButton = has4kServer && rawButtonState.showRequest4kButton,
                            )
                        }
                    }
                }

                else -> MediaButtonState()
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MediaButtonState(),
        )

    private val _addSheetUiState = MutableStateFlow(AddSheetUiState())
    val addSheetUiState: StateFlow<AddSheetUiState> = _addSheetUiState.asStateFlow()

    private val targetOrActiveRepoFlow: Flow<ArrInstanceRepository?> =
        combine(
            activeArrRepoFlow,
            _addSheetUiState,
        ) { activeRepo, addSheetState ->
            val targetId = addSheetState.targetInstance?.id
            if (targetId != null) {
                getArrInstanceRepositoryUseCase(targetId) ?: activeRepo
            } else {
                activeRepo
            }
        }

    val preferences: StateFlow<InstancePreferences> =
        targetOrActiveRepoFlow
            .filterNotNull()
            .flatMapLatest { repo ->
                observeInstancePreferencesUseCase(repo.instance.id)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = InstancePreferences(),
            )

    init {
        _selectedInstanceId.value = initialForcedInstanceId
        initialInstanceId = initialForcedInstanceId
        observeData()
        viewModelScope.launch {
            activityQueueService.manualRefresh()
        }
    }

    fun selectInstance(instanceId: Long) {
        if (_selectedInstanceId.value == instanceId) return
        _selectedInstanceId.value = instanceId

        val currentSuccess = _uiState.value as? UnifiedMediaDetailsUiState.Success
        if (currentSuccess != null) {
            val targetMedia =
                _instancePresencesMap.value[instanceId]
                    ?: currentSuccess.instancePresences.firstOrNull { it.instance.id == instanceId }?.arrMedia
            val hasTargetArrId = targetMedia?.let { it.id != null && it.id != 0L } ?: false
            _isMonitored.value = targetMedia?.monitored == true
            _uiState.value =
                currentSuccess.copy(
                    arrMedia = targetMedia,
                    selectedInstanceId = instanceId,
                    queueItems = if (targetMedia == null) emptyList() else currentSuccess.queueItems,
                    seerrMedia = if (!currentSuccess.combineSeerrArrMedia && hasTargetArrId) null else currentSuccess.seerrMedia,
                )
            val targetInst =
                availableInstances.value.firstOrNull { it.id == instanceId }
                    ?: currentSuccess.availableInstances.firstOrNull { it.id == instanceId }
                    ?: getArrInstanceRepositoryUseCase(instanceId)?.instance
            if (targetMedia == null && targetInst != null) {
                setAddSheetTargetInstance(targetInst)
            }
        }
        viewModelScope.launch {
            activityQueueService.manualRefresh()
        }
    }

    fun setAddSheetTargetInstance(instance: Instance?) {
        _addSheetUiState.update { it.copy(targetInstance = instance) }
        if (instance != null) {
            val repo = getArrInstanceRepositoryUseCase(instance.id)
            if (repo != null) {
                _addSheetUiState.update {
                    it.copy(
                        qualityProfiles = repo.qualityProfiles.value,
                        rootFolders = repo.rootFolders.value,
                        tags = repo.tags.value,
                    )
                }
                viewModelScope.launch {
                    repo.refreshQualityProfiles()
                    _addSheetUiState.update { it.copy(qualityProfiles = repo.qualityProfiles.value) }
                }
                viewModelScope.launch {
                    repo.refreshRootFolders()
                    _addSheetUiState.update { it.copy(rootFolders = repo.rootFolders.value) }
                }
                viewModelScope.launch {
                    repo.refreshTags()
                    _addSheetUiState.update { it.copy(tags = repo.tags.value) }
                }
            }
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            _instancePresencesMap.collect { map ->
                val current = _uiState.value as? UnifiedMediaDetailsUiState.Success ?: return@collect
                val updatedPresences =
                    getInstancePresencesUseCase.buildPresencesListFromInstances(
                        instances = current.availableInstances,
                        activeRepoId = current.selectedInstanceId,
                        activeArrMedia = current.arrMedia,
                        presencesMap = map,
                    )
                _uiState.value = current.copy(instancePresences = updatedPresences)

                val filteredInstances =
                    current.availableInstances.filter { instance ->
                        val arrMedia = map[instance.id]
                        val isPresent = arrMedia?.let { it.id != null && it.id != 0L } ?: false
                        !isPresent
                    }
                _addSheetUiState.update { it.copy(availableInstances = filteredInstances) }

                val currentTarget = _addSheetUiState.value.targetInstance
                val activeInst = current.availableInstances.find { it.id == current.selectedInstanceId }
                val newTarget =
                    if (currentTarget != null && filteredInstances.any { it.id == currentTarget.id }) {
                        currentTarget
                    } else if (activeInst != null && filteredInstances.any { it.id == activeInst.id }) {
                        activeInst
                    } else {
                        filteredInstances.firstOrNull()
                    }
                if (newTarget?.id != currentTarget?.id || _addSheetUiState.value.qualityProfiles.isEmpty()) {
                    setAddSheetTargetInstance(newTarget)
                }
            }
        }

        viewModelScope.launch {
            combine(
                activeArrRepoFlow,
                allArrReposFlow,
                seerrRepositoryFlow,
                bazarrRepositoryFlow,
            ) { activeRepo, allRepos, seerrRepo, bazarrRepo ->
                Quad(activeRepo, allRepos, seerrRepo, bazarrRepo)
            }.collectLatest { (activeRepo, allRepos, seerrRepo, bazarrRepo) ->
                val instances = allRepos.map { it.instance }
                val map = _instancePresencesMap.value
                val filteredInstances =
                    instances.filter { instance ->
                        val arrMedia = map[instance.id]
                        val isPresent = arrMedia?.let { it.id != null && it.id != 0L } ?: false
                        !isPresent
                    }
                _addSheetUiState.update { it.copy(availableInstances = filteredInstances) }

                val currentTarget = _addSheetUiState.value.targetInstance
                val activeInst = activeRepo?.instance
                val newTarget =
                    if (currentTarget != null && filteredInstances.any { it.id == currentTarget.id }) {
                        currentTarget
                    } else if (activeInst != null && filteredInstances.any { it.id == activeInst.id }) {
                        activeInst
                    } else {
                        filteredInstances.firstOrNull()
                    }
                if (newTarget?.id != currentTarget?.id || _addSheetUiState.value.qualityProfiles.isEmpty()) {
                    setAddSheetTargetInstance(newTarget)
                }
                if (activeRepo != null) {
                    if (_selectedInstanceId.value == null) {
                        _selectedInstanceId.value = activeRepo.instance.id
                        initialInstanceId = activeRepo.instance.id
                    }
                    launch {
                        activeRepo.qualityProfiles.collect { _qualityProfiles.value = it }
                    }
                    launch {
                        activeRepo.rootFolders.collect { _rootFolders.value = it }
                    }
                    launch {
                        activeRepo.tags.collect { _tags.value = it }
                    }
                    launch {
                        activeRepo.addItemStatus.collect { arrActionsHandler.updateAddItemStatus(it) }
                    }
                    launch {
                        activeRepo.editItemStatus.collect { arrActionsHandler.updateEditStatus(it) }
                    }
                }

                if (seerrRepo != null) {
                    launch {
                        seerrRepo.getLoggedInUser()
                    }
                    launch {
                        seerrRepo.loggedInUser.collect { _currentUser.value = it }
                    }
                    launch {
                        seerrRepo.getUsers()
                    }
                    launch {
                        seerrRepo.users.collect { _users.value = it }
                    }
                    launch {
                        seerrRepo.getRadarrServices()
                    }
                    launch {
                        seerrRepo.getSonarrServices()
                    }
                    launch {
                        seerrRepo.radarrServices.collect { _radarrServices.value = it }
                    }
                    launch {
                        seerrRepo.sonarrServices.collect { _sonarrServices.value = it }
                    }
                    launch {
                        combine(_uiState, _radarrServices, _sonarrServices) { state, radarr, sonarr ->
                            if (state is UnifiedMediaDetailsUiState.Success) {
                                val request =
                                    state.seerrMedia
                                        ?.mediaInfo
                                        ?.requests
                                        ?.firstOrNull { it.status == 1 }
                                val serverId =
                                    request?.serverId ?: when (resolvedRequestType) {
                                        RequestType.Movie -> radarr.find { it.isDefault }?.id
                                        RequestType.Tv -> sonarr.find { it.isDefault }?.id
                                        else -> null
                                    }
                                if (serverId != null) serverId to resolvedRequestType else null
                            } else {
                                null
                            }
                        }.filterNotNull()
                            .distinctUntilChanged()
                            .collectLatest { (serverId, type) ->
                                val result =
                                    when (type) {
                                        RequestType.Movie -> seerrRepo.getRadarrDetails(serverId)
                                        RequestType.Tv -> seerrRepo.getSonarrDetails(serverId)
                                        else -> return@collectLatest
                                    }
                                result.onSuccess { details ->
                                    _serviceDetails.value = details
                                }
                            }
                    }
                }

                val cachedArrMedia = activeRepo?.let { _instancePresencesMap.value[it.instance.id] }
                val targetArrId =
                    if (activeRepo?.instance?.id == initialInstanceId) {
                        cachedArrMedia?.id ?: arrId
                    } else {
                        cachedArrMedia?.id
                    }

                val combineMedia = preferencesStore.combineSeerrArrMedia.first()
                val showBazarr = preferencesStore.bazarrDetailsIntegration.first()

                val effectiveSeerrRepo = if (!combineMedia && targetArrId != null) null else seerrRepo
                val effectiveArrRepo = if (!combineMedia && targetArrId == null && seerrRepo != null) null else activeRepo

                getUnifiedMediaDetailsUseCase(
                    arrId = targetArrId,
                    tmdbId = tmdbId,
                    tvdbId = tvdbId,
                    instanceType = resolvedInstanceType,
                    requestType = resolvedRequestType,
                    arrRepository = effectiveArrRepo,
                    seerrRepository = effectiveSeerrRepo,
                    bazarrRepository = if (showBazarr) bazarrRepo else null,
                ).collect { rawState ->
                    if (rawState is UnifiedMediaDetailsUiState.Success) {
                        _isMonitored.value = rawState.arrMedia?.monitored ?: false

                        val resolvedTvdbLookupId =
                            tvdbId
                                ?: (rawState.arrMedia as? ArrSeries)?.tvdbId?.takeIf { it > 0 }
                                ?: (rawState.seerrMedia as? TvDetails)?.externalIds?.tvdbId?.takeIf { it > 0 }

                        val resolvedLookupId =
                            tmdbId
                                ?: (rawState.arrMedia as? ArrMovie)?.tmdbId?.takeIf { it > 0 }
                                ?: (rawState.arrMedia as? ArrSeries)?.tmdbId?.takeIf { it > 0 }
                                ?: (rawState.seerrMedia as? MovieDetails)?.id
                                ?: (rawState.seerrMedia as? TvDetails)?.id

                        val query =
                            if (resolvedInstanceType == InstanceType.Sonarr || resolvedRequestType == RequestType.Tv) {
                                resolvedTvdbLookupId?.let { "tvdb:$it" } ?: resolvedLookupId?.let { "tmdb:$it" }
                            } else {
                                resolvedLookupId?.let { "tmdb:$it" } ?: resolvedTvdbLookupId?.let { "tvdb:$it" }
                            }

                        if (activeRepo != null && rawState.hasArrId && rawState.arrMedia != null) {
                            if (_instancePresencesMap.value[activeRepo.instance.id] != rawState.arrMedia) {
                                val updated = _instancePresencesMap.value.toMutableMap()
                                updated[activeRepo.instance.id] = rawState.arrMedia
                                _instancePresencesMap.value = updated
                            }
                        }

                        if (allRepos.isNotEmpty() && query != null) {
                            val missingRepos =
                                allRepos.filter { repo ->
                                    repo.instance.id != activeRepo?.instance?.id &&
                                        !_instancePresencesMap.value.containsKey(
                                            repo.instance.id,
                                        )
                                }
                            if (missingRepos.isNotEmpty()) {
                                launch {
                                    _instancePresencesMap.value =
                                        getInstancePresencesUseCase.fetchMissingPresences(
                                            repositories = missingRepos,
                                            query = query,
                                            resolvedTvdbLookupId = resolvedTvdbLookupId,
                                            resolvedLookupId = resolvedLookupId,
                                            existingPresences = _instancePresencesMap.value,
                                        )
                                }
                            }
                        }

                        val presences =
                            getInstancePresencesUseCase.buildPresencesList(
                                repositories = allRepos,
                                activeRepoId = activeRepo?.instance?.id,
                                activeArrMedia = rawState.arrMedia,
                                presencesMap = _instancePresencesMap.value,
                            )

                        _uiState.value =
                            rawState.copy(
                                availableInstances = if (!combineMedia && !rawState.hasArrId) emptyList() else allRepos.map { it.instance },
                                selectedInstanceId = activeRepo?.instance?.id ?: _selectedInstanceId.value,
                                instancePresences = if (!combineMedia && !rawState.hasArrId) emptyList() else presences,
                                combineSeerrArrMedia = combineMedia,
                                bazarrDetailsIntegration = showBazarr,
                            )
                    } else {
                        if (_uiState.value !is UnifiedMediaDetailsUiState.Success) {
                            _uiState.value = rawState
                        }
                    }
                }
            }
        }

        tracearrHandler.observeTracearrData(
            scope = viewModelScope,
            uiStateFlow = uiState,
            getTracearrInstanceRepositoryUseCase = getTracearrInstanceRepositoryUseCase,
            preferencesStore = preferencesStore,
            initialTmdbId = tmdbId,
            initialRequestType = requestType,
        )
    }

    private suspend fun getActiveArrRepository(): ArrInstanceRepository? {
        val selectedId = _selectedInstanceId.value
        if (selectedId != null) {
            getArrInstanceRepositoryUseCase(selectedId)?.let { return it }
        }
        return activeArrRepoFlow.firstOrNull()
    }

    private suspend fun getSeerrRepository(): SeerrInstanceRepository? = seerrRepositoryFlow.firstOrNull()

    private fun getEffectiveArrMedia(): ArrMedia? {
        val selectedId = _selectedInstanceId.value
        if (selectedId != null) {
            _instancePresencesMap.value[selectedId]?.let { return it }
        }
        val stateMedia = (uiState.value as? UnifiedMediaDetailsUiState.Success)?.arrMedia
        if (stateMedia != null) return stateMedia
        val activeId = activeInstance.value?.id ?: initialInstanceId
        if (activeId != null) {
            _instancePresencesMap.value[activeId]?.let { return it }
        }
        return null
    }

    private fun getEffectiveArrId(): Long? {
        val media = getEffectiveArrMedia()
        val mediaId = media?.id?.takeIf { it != 0L }
        if (mediaId != null) return mediaId

        val activeId = _selectedInstanceId.value ?: activeInstance.value?.id ?: initialInstanceId
        return if (activeId == initialInstanceId) arrId else null
    }

    fun refresh() {
        viewModelScope.launch {
            val repository = getActiveArrRepository()
            if (repository != null) {
                launch { repository.refreshQualityProfiles() }
                launch { repository.refreshRootFolders() }
                launch { repository.refreshTags() }
            }
            launch { activityQueueService.manualRefresh() }
        }
        observeData()
    }

    fun performRefresh() {
        viewModelScope.launch {
            val repository = getActiveArrRepository() ?: return@launch
            val effectiveId = getEffectiveArrId() ?: return@launch
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
            activeRepoProvider = { getActiveArrRepository() },
            selectedInstanceIdProvider = { _selectedInstanceId.value },
            addSheetTargetInstanceIdProvider = { _addSheetUiState.value.targetInstance?.id },
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
            seerrRepoProvider = { getSeerrRepository() },
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
            repositoryProvider = { getSeerrRepository() },
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
            repositoryProvider = { getSeerrRepository() },
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
            repositoryProvider = { getSeerrRepository() },
            requestId = requestId,
            onSuccessRefresh = ::refresh,
        )
    }

    fun declineRequest(requestId: Long) {
        seerrHandler.declineRequest(
            scope = viewModelScope,
            repositoryProvider = { getSeerrRepository() },
            requestId = requestId,
            onSuccessRefresh = ::refresh,
        )
    }

    fun deleteSeerrMediaFile(is4k: Boolean = false) {
        seerrHandler.deleteSeerrMediaFile(
            scope = viewModelScope,
            repositoryProvider = { getSeerrRepository() },
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
            repositoryProvider = { getSeerrRepository() },
            currentMediaIdProvider = {
                (uiState.value as? UnifiedMediaDetailsUiState.Success)?.seerrMedia?.mediaInfo?.id ?: seerrMediaId
            },
            onSuccessRefresh = ::refresh,
        )
    }

    fun markSeerrMediaAsAvailable(is4k: Boolean = false) {
        seerrHandler.markSeerrMediaAsAvailable(
            scope = viewModelScope,
            repositoryProvider = { getSeerrRepository() },
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
            repositoryProvider = { getActiveArrRepository() },
            mediaProvider = { getEffectiveArrMedia() },
        )
    }

    fun performAutomaticLookup() {
        arrActionsHandler.performAutomaticLookup(
            scope = viewModelScope,
            repositoryProvider = { getActiveArrRepository() },
            effectiveIdProvider = { getEffectiveArrId() },
            resolvedInstanceType = resolvedInstanceType,
        )
    }

    fun performEpisodeAutomaticLookup(episodeId: Long) {
        arrActionsHandler.performEpisodeAutomaticLookup(
            scope = viewModelScope,
            repositoryProvider = { getActiveArrRepository() },
            effectiveIdProvider = { getEffectiveArrId() },
            resolvedInstanceType = resolvedInstanceType,
            episodeId = episodeId,
        )
    }

    fun performSeasonAutomaticLookup(seasonNumber: Int) {
        arrActionsHandler.performSeasonAutomaticLookup(
            scope = viewModelScope,
            repositoryProvider = { getActiveArrRepository() },
            effectiveIdProvider = { getEffectiveArrId() },
            resolvedInstanceType = resolvedInstanceType,
            seasonNumber = seasonNumber,
        )
    }

    fun performAlbumAutomaticLookup(albumId: Long) {
        arrActionsHandler.performAlbumAutomaticLookup(
            scope = viewModelScope,
            repositoryProvider = { getActiveArrRepository() },
            effectiveIdProvider = { getEffectiveArrId() },
            resolvedInstanceType = resolvedInstanceType,
            albumId = albumId,
        )
    }

    fun performBookAutomaticLookup(bookId: Long) {
        arrActionsHandler.performBookAutomaticLookup(
            scope = viewModelScope,
            repositoryProvider = { getActiveArrRepository() },
            effectiveIdProvider = { getEffectiveArrId() },
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
            repositoryProvider = { getActiveArrRepository() },
            effectiveIdProvider = { getEffectiveArrId() },
            item = item,
            moveFiles = moveFiles,
        )
    }

    fun updateAlbum(album: ArrAlbum) {
        arrActionsHandler.updateAlbum(
            scope = viewModelScope,
            repositoryProvider = { getActiveArrRepository() },
            album = album,
            onSuccessReobserve = ::observeData,
        )
    }

    fun deleteMedia(
        deleteFiles: Boolean,
        addImportExclusion: Boolean,
    ) {
        arrActionsHandler.deleteMedia(
            scope = viewModelScope,
            repositoryProvider = { getActiveArrRepository() },
            effectiveIdProvider = { getEffectiveArrId() },
            deleteFiles = deleteFiles,
            addImportExclusion = addImportExclusion,
        )
    }

    fun deleteSeasonFiles(seasonNumber: Int) {
        arrActionsHandler.deleteSeasonFiles(
            scope = viewModelScope,
            repositoryProvider = { getActiveArrRepository() },
            effectiveIdProvider = { getEffectiveArrId() },
            seasonNumber = seasonNumber,
            onSuccessRefresh = ::refresh,
        )
    }

    fun deleteAlbumFiles(albumId: Long) {
        arrActionsHandler.deleteAlbumFiles(
            scope = viewModelScope,
            repositoryProvider = { getActiveArrRepository() },
            effectiveIdProvider = { getEffectiveArrId() },
            albumId = albumId,
            onSuccessRefresh = ::refresh,
        )
    }

    fun deleteMovieFile() {
        arrActionsHandler.deleteMovieFile(
            scope = viewModelScope,
            repositoryProvider = { getActiveArrRepository() },
            mediaProvider = { getEffectiveArrMedia() },
            onSuccessRefresh = ::refresh,
        )
    }

    fun deleteAudiobookFile() {
        arrActionsHandler.deleteAudiobookFile(
            scope = viewModelScope,
            repositoryProvider = { getActiveArrRepository() },
            mediaProvider = { getEffectiveArrMedia() },
            onSuccessRefresh = ::refresh,
        )
    }

    fun deleteEpisodeFile(episodeId: Long) {
        arrActionsHandler.deleteEpisodeFile(
            scope = viewModelScope,
            repositoryProvider = { getActiveArrRepository() },
            effectiveIdProvider = { getEffectiveArrId() },
            episodeId = episodeId,
            onSuccessRefresh = ::refresh,
        )
    }

    fun toggleSeasonMonitored(seasonNumber: Int) {
        arrActionsHandler.toggleSeasonMonitored(
            scope = viewModelScope,
            repositoryProvider = { getActiveArrRepository() },
            effectiveIdProvider = { getEffectiveArrId() },
            seasonNumber = seasonNumber,
        )
    }

    fun toggleEpisodeMonitored(episode: Episode) {
        arrActionsHandler.toggleEpisodeMonitored(
            scope = viewModelScope,
            repositoryProvider = { getActiveArrRepository() },
            episode = episode,
        )
    }

    fun toggleAlbumMonitored(album: ArrAlbum) {
        arrActionsHandler.toggleAlbumMonitored(
            scope = viewModelScope,
            repositoryProvider = { getActiveArrRepository() },
            album = album,
        )
    }

    fun toggleBookMonitored(book: Book) {
        arrActionsHandler.toggleBookMonitored(
            scope = viewModelScope,
            repositoryProvider = { getActiveArrRepository() },
            book = book,
        )
    }

    fun toggleBookSeriesMonitored(books: List<Book>) {
        arrActionsHandler.toggleBookSeriesMonitored(
            scope = viewModelScope,
            repositoryProvider = { getActiveArrRepository() },
            books = books,
        )
    }

    val isArrConfigured: StateFlow<Boolean> =
        activeArrRepoFlow
            .map { it != null }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun updatePreferences(preferences: InstancePreferences) {
        viewModelScope.launch {
            val targetInstanceId =
                _addSheetUiState.value.targetInstance?.id
                    ?: activeInstance.value?.id
            if (targetInstanceId != null) {
                updateInstancePreferencesUseCase(targetInstanceId, preferences)
            }
        }
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

private data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
)
