package com.dnfapps.arrmatey.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Arrtist
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.arr.service.ActivityQueueService
import com.dnfapps.arrmatey.arr.usecase.DeleteAlbumFilesUseCase
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
import com.dnfapps.arrmatey.instances.usecase.ObserveInstancePreferencesUseCase
import com.dnfapps.arrmatey.instances.usecase.ObserveScopedReposByTypeUseCase
import com.dnfapps.arrmatey.instances.usecase.UpdateInstancePreferencesUseCase
import com.dnfapps.arrmatey.model.AddSheetUiState
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.arrmatey.model.SmartAddSeerrAction
import com.dnfapps.arrmatey.model.UnifiedMediaDetailsUiState
import com.dnfapps.arrmatey.seerr.api.model.ApprovalStatus
import com.dnfapps.arrmatey.seerr.api.model.IssueBody
import com.dnfapps.arrmatey.seerr.api.model.IssueType
import com.dnfapps.arrmatey.seerr.api.model.MediaRequest
import com.dnfapps.arrmatey.seerr.api.model.MovieDetails
import com.dnfapps.arrmatey.seerr.api.model.RequestMediaBody
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
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
import dev.shivathapaa.logger.api.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
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
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class UnifiedMediaDetailsViewModel(
    private val arrId: Long?,
    private val tmdbId: Long?,
    private val tvdbId: Long?,
    private val instanceType: InstanceType?,
    private val requestType: RequestType?,
    initialForcedInstanceId: Long? = null,
    private val getUnifiedMediaDetailsUseCase: GetUnifiedMediaDetailsUseCase,
    private val smartAddMediaUseCase: SmartAddMediaUseCase,
    private val getArrInstanceRepositoryUseCase: GetArrInstanceRepositoryUseCase,
    getSeerrInstanceRepositoryUseCase: GetSeerrInstanceRepositoryUseCase,
    getBazarrInstanceRepositoryUseCase: GetBazarrInstanceRepositoryUseCase,
    private val toggleMonitorUseCase: ToggleMonitorUseCase,
    private val updateMediaUseCase: UpdateMediaUseCase,
    private val deleteMediaUseCase: DeleteMediaUseCase,
    private val performRefreshUseCase: PerformRefreshUseCase,
    private val performAutomaticSearchUseCase: PerformAutomaticSearchUseCase,
    private val submitRequestUseCase: SubmitRequestUseCase,
    private val cancelRequestUseCase: CancelRequestUseCase,
    private val setRequestApprovalStatusUseCase: SetRequestApprovalStatusUseCase,
    private val deleteSeasonFilesUseCase: DeleteSeasonFilesUseCase,
    private val deleteAlbumFilesUseCase: DeleteAlbumFilesUseCase,
    private val deleteMovieFileUseCase: DeleteMovieFileUseCase,
    private val deleteEpisodeFileUseCase: DeleteEpisodeFileUseCase,
    private val submitIssueUseCase: SubmitIssueUseCase,
    observeInstancePreferencesUseCase: ObserveInstancePreferencesUseCase,
    private val updateInstancePreferencesUseCase: UpdateInstancePreferencesUseCase,
    observeScopedReposByTypeUseCase: ObserveScopedReposByTypeUseCase,
    private val getInstancePresencesUseCase: GetInstancePresencesUseCase,
    private val deleteQueueItemUseCase: DeleteQueueItemUseCase,
    private val activityQueueService: ActivityQueueService,
    private val removeSeerrMediaFileUseCase: RemoveSeerrMediaFileUseCase,
    private val clearSeerrMediaDataUseCase: ClearSeerrMediaDataUseCase,
    private val markSeerrMediaAsAvailableUseCase: MarkSeerrMediaAsAvailableUseCase,
    private val preferencesStore: PreferencesStore,
    private val logger: Logger,
) : ViewModel() {
    private var seerrMediaId: Long? = null
    private var initialInstanceId: Long? = null

    private val _isMonitored = MutableStateFlow(false)
    val isMonitored: StateFlow<Boolean> = _isMonitored.asStateFlow()

    private val _currentUser = MutableStateFlow<SeerrUser?>(null)
    val currentUser: StateFlow<SeerrUser?> = _currentUser.asStateFlow()

    private val _uiState = MutableStateFlow<UnifiedMediaDetailsUiState>(UnifiedMediaDetailsUiState.Initial)
    val uiState: StateFlow<UnifiedMediaDetailsUiState> = _uiState.asStateFlow()

    private val _pendingSeerrRequest = MutableStateFlow<MediaRequest?>(null)
    val pendingSeerrRequest: StateFlow<MediaRequest?> = _pendingSeerrRequest.asStateFlow()

    private val _qualityProfiles = MutableStateFlow<List<QualityProfile>>(emptyList())
    val qualityProfiles: StateFlow<List<QualityProfile>> = _qualityProfiles.asStateFlow()

    private val _rootFolders = MutableStateFlow<List<RootFolder>>(emptyList())
    val rootFolders: StateFlow<List<RootFolder>> = _rootFolders.asStateFlow()

    private val _tags = MutableStateFlow<List<Tag>>(emptyList())
    val tags: StateFlow<List<Tag>> = _tags.asStateFlow()

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

    private val _deleteEpisodeStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val deleteEpisodeStatus: StateFlow<OperationStatus> = _deleteEpisodeStatus.asStateFlow()

    private val _removeQueueItemStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val removeQueueItemStatus: StateFlow<OperationStatus> = _removeQueueItemStatus.asStateFlow()

    private val _requestStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val requestStatus: StateFlow<OperationStatus> = _requestStatus.asStateFlow()

    private val _lastSearchResult = MutableStateFlow<Boolean?>(null)
    val lastSearchResult: StateFlow<Boolean?> = _lastSearchResult.asStateFlow()

    private val _isRequestSheetVisible = MutableStateFlow(false)
    val isRequestSheetVisible: StateFlow<Boolean> = _isRequestSheetVisible.asStateFlow()

    private val _isReportIssueSheetVisible = MutableStateFlow(false)
    val isReportIssueSheetVisible: StateFlow<Boolean> = _isReportIssueSheetVisible.asStateFlow()

    private val _radarrServices = MutableStateFlow<List<Service>>(emptyList())
    val radarrServices: StateFlow<List<Service>> = _radarrServices.asStateFlow()

    private val _sonarrServices = MutableStateFlow<List<Service>>(emptyList())
    val sonarrServices: StateFlow<List<Service>> = _sonarrServices.asStateFlow()

    private val _users = MutableStateFlow<List<SeerrUser>>(emptyList())
    val users: StateFlow<List<SeerrUser>> = _users.asStateFlow()

    private val _serviceDetails = MutableStateFlow<ServiceDetails?>(null)
    val serviceDetails: StateFlow<ServiceDetails?> = _serviceDetails.asStateFlow()

    private val _reportIssueState = MutableStateFlow(ReportIssueUiState())
    val reportIssueState: StateFlow<ReportIssueUiState> =
        _reportIssueState
            .combine(_uiState) { issueState, uiState ->
                if (uiState is UnifiedMediaDetailsUiState.Success && uiState.seerrMedia != null) {
                    seerrMediaId = uiState.seerrMedia.mediaInfo?.id
                    if (issueState.saveSuccess) {
                        _isReportIssueSheetVisible.value = false
                    }
                    issueState.copy(
                        includeSeriesOptions = uiState.seerrMedia.requestType == RequestType.Tv,
                        mediaTitle = uiState.seerrMedia.displayTitle,
                        availableSeasons = (uiState.seerrMedia as? TvDetails)?.seasons ?: emptyList(),
                        saveButtonEnabled = issueState.message.isNotEmpty() && !issueState.saveInProgress,
                    )
                } else {
                    issueState
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ReportIssueUiState(),
            )

    private val _isViewRequestSheetVisible = MutableStateFlow(false)
    val isViewRequestSheetVisible: StateFlow<Boolean> = _isViewRequestSheetVisible.asStateFlow()

    private val _isRequest4k = MutableStateFlow(false)
    val isRequest4k: StateFlow<Boolean> = _isRequest4k.asStateFlow()

    private val _automaticSearchIds = MutableStateFlow<Set<Long>>(emptySet())
    val automaticSearchIds: StateFlow<Set<Long>> = _automaticSearchIds.asStateFlow()

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

    private val seerrRepositoryFlow = getSeerrInstanceRepositoryUseCase.observeSelected()
    private val bazarrRepositoryFlow = getBazarrInstanceRepositoryUseCase.observeSelected()

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

    // Add Sheet Dynamic State
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
            _isMonitored.value = targetMedia?.monitored == true
            _uiState.value =
                currentSuccess.copy(
                    arrMedia = targetMedia,
                    selectedInstanceId = instanceId,
                    queueItems = if (targetMedia == null) emptyList() else currentSuccess.queueItems,
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
                        activeRepo.addItemStatus.collect { _addItemStatus.value = it }
                    }
                    launch {
                        activeRepo.editItemStatus.collect { _editStatus.value = it }
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

                getUnifiedMediaDetailsUseCase(
                    arrId = targetArrId,
                    tmdbId = tmdbId,
                    tvdbId = tvdbId,
                    instanceType = resolvedInstanceType,
                    requestType = resolvedRequestType,
                    arrRepository = activeRepo,
                    seerrRepository = seerrRepo,
                    bazarrRepository = bazarrRepo,
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
                                availableInstances = allRepos.map { it.instance },
                                selectedInstanceId = activeRepo?.instance?.id ?: _selectedInstanceId.value,
                                instancePresences = presences,
                            )
                    } else {
                        if (_uiState.value !is UnifiedMediaDetailsUiState.Success) {
                            _uiState.value = rawState
                        }
                    }
                }
            }
        }
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
        _requestStatus.value = OperationStatus.Idle
        _isRequest4k.value = is4k
        _isRequestSheetVisible.value = true
    }

    fun hideRequestSheet() {
        _isRequestSheetVisible.value = false
    }

    fun showReportIssueSheet() {
        _isReportIssueSheetVisible.value = true
    }

    fun hideReportIssueSheet() {
        _isReportIssueSheetVisible.value = false
    }

    fun showViewRequestSheet() {
        _isViewRequestSheetVisible.value = true
    }

    fun hideViewRequestSheet() {
        _isViewRequestSheetVisible.value = false
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

    // Smart Actions
    fun smartAdd(
        item: ArrMedia,
        searchOnAdd: Boolean = false,
        targetInstanceId: Long? = null,
    ) {
        viewModelScope.launch {
            val type = resolvedInstanceType
            if (type == null) {
                logger.error {
                    "UnifiedMediaDetailsViewModel.smartAdd: resolvedInstanceType is null (requestType=$requestType, instanceType=$instanceType); cannot add '${item.title}'"
                }
                emitFallbackAddError("Unsupported media type")
                return@launch
            }

            val successState = uiState.value as? UnifiedMediaDetailsUiState.Success
            val seerrMediaDetails = successState?.seerrMedia
            val pendingRequest = seerrMediaDetails?.mediaInfo?.requests?.firstOrNull { it.status == 1 }

            if (pendingRequest != null) {
                val action = preferencesStore.smartAddSeerrAction.first()
                if (action == SmartAddSeerrAction.AlwaysAsk) {
                    _pendingSeerrRequest.value = pendingRequest
                } else if (action == SmartAddSeerrAction.Approve) {
                    handlePendingRequestAction(pendingRequest.id, SmartAddSeerrAction.Approve, false)
                } else if (action == SmartAddSeerrAction.Decline) {
                    handlePendingRequestAction(pendingRequest.id, SmartAddSeerrAction.Decline, false)
                }
            }

            val effectiveInstanceId =
                targetInstanceId ?: _addSheetUiState.value.targetInstance?.id ?: _selectedInstanceId.value

            val targetRepo =
                if (effectiveInstanceId != null) {
                    getArrInstanceRepositoryUseCase(effectiveInstanceId)
                } else {
                    getActiveArrRepository()
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
        requestId: Long,
        action: SmartAddSeerrAction,
        rememberChoice: Boolean,
    ) {
        viewModelScope.launch {
            if (rememberChoice) {
                preferencesStore.setSmartAddSeerrAction(action)
            }

            val seerrRepo = getSeerrRepository() ?: return@launch
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

    fun dismissPendingRequestDialog() {
        _pendingSeerrRequest.value = null
    }

    private suspend fun emitFallbackAddError(message: String) {
        _addItemStatus.value = OperationStatus.Error(message = message)
        delay(1500.milliseconds)
        _addItemStatus.value = OperationStatus.Idle
    }

    fun submitRequest(
        profileId: Long? = null,
        rootFolder: String? = null,
        languageProfileId: Long? = null,
        seasons: List<Int>? = null,
        is4k: Boolean = false,
        userId: Long? = null,
    ) {
        viewModelScope.launch {
            val repository = getSeerrRepository() ?: return@launch
            val body =
                RequestMediaBody(
                    mediaType = resolvedRequestType ?: return@launch,
                    mediaId = tmdbId ?: return@launch,
                    is4k = is4k,
                    serverId = null,
                    profileId = profileId,
                    rootFolder = rootFolder,
                    languageProfileId = languageProfileId,
                    seasons = seasons,
                    userId = userId,
                )
            _requestStatus.value = OperationStatus.InProgress
            submitRequestUseCase(body, repository)
                .onSuccess {
                    _requestStatus.value = OperationStatus.Success()
                    hideRequestSheet()
                    refresh()
                }.onError { code, message, cause ->
                    _requestStatus.value = OperationStatus.Error(code, message, cause)
                }
        }
    }

    fun approveRequest(
        requestId: Long,
        profileId: Long? = null,
        rootFolder: String? = null,
        languageProfileId: Long? = null,
        seasons: List<Int>? = null,
    ) {
        viewModelScope.launch {
            val repository = getSeerrRepository() ?: return@launch
            _requestStatus.value = OperationStatus.InProgress
            setRequestApprovalStatusUseCase(
                requestId = requestId,
                approvalStatus = ApprovalStatus.Approve,
                repository = repository,
                profileId = profileId,
                rootFolder = rootFolder,
                languageProfileId = languageProfileId,
                seasons = seasons,
            ).onSuccess {
                _requestStatus.value = OperationStatus.Success()
                hideViewRequestSheet()
                refresh()
            }.onError { code, message, cause ->
                _requestStatus.value = OperationStatus.Error(code, message, cause)
            }
        }
    }

    fun cancelRequest(requestId: Long) {
        viewModelScope.launch {
            val repository = getSeerrRepository() ?: return@launch
            _requestStatus.value = OperationStatus.InProgress
            cancelRequestUseCase(requestId, repository)
                .onSuccess {
                    _requestStatus.value = OperationStatus.Success()
                    refresh()
                }.onError { code, message, cause ->
                    _requestStatus.value = OperationStatus.Error(code, message, cause)
                }
        }
    }

    fun declineRequest(requestId: Long) {
        viewModelScope.launch {
            val repository = getSeerrRepository() ?: return@launch
            _requestStatus.value = OperationStatus.InProgress
            setRequestApprovalStatusUseCase(requestId, ApprovalStatus.Decline, repository)
                .onSuccess {
                    _requestStatus.value = OperationStatus.Success()
                    hideViewRequestSheet()
                    refresh()
                }.onError { code, message, cause ->
                    _requestStatus.value = OperationStatus.Error(code, message, cause)
                }
        }
    }

    fun deleteSeerrMediaFile(is4k: Boolean = false) {
        viewModelScope.launch {
            val repository = getSeerrRepository() ?: return@launch
            val currentMediaId =
                (uiState.value as? UnifiedMediaDetailsUiState.Success)?.seerrMedia?.mediaInfo?.id
                    ?: seerrMediaId ?: return@launch
            removeSeerrMediaFileUseCase(currentMediaId, is4k, repository)
                .onSuccess { refresh() }
        }
    }

    fun clearSeerrMediaData() {
        viewModelScope.launch {
            val repository = getSeerrRepository() ?: return@launch
            val currentMediaId =
                (uiState.value as? UnifiedMediaDetailsUiState.Success)?.seerrMedia?.mediaInfo?.id
                    ?: seerrMediaId ?: return@launch
            clearSeerrMediaDataUseCase(currentMediaId, repository)
                .onSuccess { refresh() }
        }
    }

    fun markSeerrMediaAsAvailable(is4k: Boolean = false) {
        viewModelScope.launch {
            val repository = getSeerrRepository() ?: return@launch
            val currentMediaId =
                (uiState.value as? UnifiedMediaDetailsUiState.Success)?.seerrMedia?.mediaInfo?.id
                    ?: seerrMediaId ?: return@launch
            markSeerrMediaAsAvailableUseCase(currentMediaId, is4k, repository)
                .onSuccess { refresh() }
        }
    }

    // Arr Actions
    fun toggleMonitored() {
        viewModelScope.launch {
            val repository = getActiveArrRepository() ?: return@launch
            val item = getEffectiveArrMedia() ?: return@launch
            toggleMonitorUseCase.toggleMedia(item, repository)
        }
    }

    fun performAutomaticLookup() {
        val effectiveId = getEffectiveArrId() ?: return
        runSearch(effectiveId)
    }

    fun performEpisodeAutomaticLookup(episodeId: Long) {
        runSearch(episodeId, episodeId = episodeId)
    }

    fun performSeasonAutomaticLookup(seasonNumber: Int) {
        val effectiveId = getEffectiveArrId() ?: return
        runSearch(effectiveId, seasonNumber = seasonNumber)
    }

    fun performAlbumAutomaticLookup(albumId: Long) {
        runSearch(albumId, albumId = albumId)
    }

    fun performBookAutomaticLookup(bookId: Long) {
        runSearch(bookId, albumId = bookId)
    }

    private fun runSearch(
        trackingId: Long,
        episodeId: Long? = null,
        seasonNumber: Int? = null,
        albumId: Long? = null,
    ) {
        viewModelScope.launch {
            val repository = getActiveArrRepository() ?: return@launch
            val effectiveId = getEffectiveArrId() ?: return@launch
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
        item: ArrMedia,
        moveFiles: Boolean = false,
    ) {
        viewModelScope.launch {
            val repository = getActiveArrRepository() ?: return@launch
            val effectiveId = getEffectiveArrId()
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
            updateMediaUseCase.edit(contextualItem, moveFiles, repository)
        }
    }

    fun updateAlbum(album: ArrAlbum) {
        viewModelScope.launch {
            val repository = getActiveArrRepository() ?: return@launch
            updateMediaUseCase
                .updateAlbum(album, repository)
                .onSuccess {
                    delay(1.seconds)
                    repository.resetEditItemStatus()
                    observeData()
                }.onError { _, _, _ ->
                    delay(3.seconds)
                    repository.resetEditItemStatus()
                }
        }
    }

    fun deleteMedia(
        deleteFiles: Boolean,
        addImportExclusion: Boolean,
    ) {
        viewModelScope.launch {
            val repository = getActiveArrRepository() ?: return@launch
            val effectiveId = getEffectiveArrId() ?: return@launch
            deleteMediaUseCase(effectiveId, deleteFiles, addImportExclusion, repository)
                .collect { status ->
                    _deleteStatus.value = status
                }
        }
    }

    fun deleteSeasonFiles(seasonNumber: Int) {
        viewModelScope.launch {
            val repository = getActiveArrRepository() ?: return@launch
            val effectiveId = getEffectiveArrId() ?: return@launch
            deleteSeasonFilesUseCase(effectiveId, seasonNumber, repository)
                .collect { status ->
                    _deleteSeasonStatus.value = status
                    if (status is OperationStatus.Success) {
                        delay(500.milliseconds)
                        _deleteSeasonStatus.value = OperationStatus.Idle
                        refresh()
                    } else if (status is OperationStatus.Error) {
                        delay(2000.milliseconds)
                        _deleteSeasonStatus.value = OperationStatus.Idle
                    }
                }
        }
    }

    fun deleteAlbumFiles(albumId: Long) {
        viewModelScope.launch {
            val repository = getActiveArrRepository() ?: return@launch
            val effectiveId = getEffectiveArrId() ?: return@launch
            deleteAlbumFilesUseCase(effectiveId, albumId, repository)
                .collect { status ->
                    _deleteAlbumStatus.value = status
                    if (status is OperationStatus.Success) {
                        delay(500.milliseconds)
                        _deleteAlbumStatus.value = OperationStatus.Idle
                        refresh()
                    } else if (status is OperationStatus.Error) {
                        delay(2000.milliseconds)
                        _deleteAlbumStatus.value = OperationStatus.Idle
                    }
                }
        }
    }

    fun deleteMovieFile() {
        viewModelScope.launch {
            val repository = getActiveArrRepository() ?: return@launch
            val effectiveId = getEffectiveArrId() ?: return@launch
            deleteMovieFileUseCase(effectiveId, repository)
                .collect { status ->
                    _deleteMovieFileStatus.value = status
                    if (status is OperationStatus.Success) {
                        delay(500.milliseconds)
                        _deleteMovieFileStatus.value = OperationStatus.Idle
                        refresh()
                    } else if (status is OperationStatus.Error) {
                        delay(2000.milliseconds)
                        _deleteMovieFileStatus.value = OperationStatus.Idle
                    }
                }
        }
    }

    fun deleteEpisodeFile(episodeId: Long) {
        viewModelScope.launch {
            val repository = getActiveArrRepository() ?: return@launch
            val effectiveId = getEffectiveArrId() ?: return@launch
            deleteEpisodeFileUseCase(effectiveId, episodeId, repository)
                .collect { status ->
                    _deleteEpisodeStatus.value = status
                    if (status is OperationStatus.Success) {
                        delay(500.milliseconds)
                        _deleteEpisodeStatus.value = OperationStatus.Idle
                        refresh()
                    } else if (status is OperationStatus.Error) {
                        delay(2000.milliseconds)
                        _deleteEpisodeStatus.value = OperationStatus.Idle
                    }
                }
        }
    }

    fun toggleSeasonMonitored(seasonNumber: Int) {
        viewModelScope.launch {
            val repository = getActiveArrRepository() ?: return@launch
            val effectiveId = getEffectiveArrId() ?: return@launch
            toggleMonitorUseCase.toggleSeason(effectiveId, seasonNumber, repository)
        }
    }

    fun toggleEpisodeMonitored(episode: Episode) {
        viewModelScope.launch {
            val repository = getActiveArrRepository() ?: return@launch
            toggleMonitorUseCase.toggleEpisode(episode, repository)
        }
    }

    fun toggleAlbumMonitored(album: ArrAlbum) {
        viewModelScope.launch {
            val repository = getActiveArrRepository() ?: return@launch
            toggleMonitorUseCase.toggleAlbum(album, repository)
        }
    }

    fun toggleBookMonitored(book: Book) {
        viewModelScope.launch {
            val repository = getActiveArrRepository() ?: return@launch
            toggleMonitorUseCase.toggleBook(book, repository)
        }
    }

    fun toggleBookSeriesMonitored(books: List<Book>) {
        viewModelScope.launch {
            val repository = getActiveArrRepository() ?: return@launch
            books.forEach { book ->
                toggleMonitorUseCase.toggleBook(book, repository)
            }
        }
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
        _reportIssueState.update {
            it.copy(issueType = issueType)
        }
    }

    fun setIssueMessage(message: String) {
        _reportIssueState.update {
            it.copy(message = message)
        }
    }

    fun setProblemSeason(season: Int?) {
        _reportIssueState.update {
            it.copy(problemSeason = season)
        }
    }

    fun setProblemEpisode(episode: Int?) {
        _reportIssueState.update {
            it.copy(problemEpisode = episode)
        }
    }

    fun resetIssueState() {
        _reportIssueState.value = ReportIssueUiState()
    }

    fun submitIssue() {
        val seerrId = seerrMediaId ?: return
        val state = _reportIssueState.value
        val issue =
            IssueBody(
                issueType = state.issueType.value,
                message = state.message,
                mediaId = seerrId,
                problemSeason = state.problemSeason ?: 0,
                problemEpisode = state.problemSeason?.let { state.problemEpisode } ?: 0,
            )
        viewModelScope.launch {
            submitIssueUseCase(issue)
                .collect { issueStatus ->
                    _reportIssueState.update {
                        it.copy(
                            saveInProgress = issueStatus == OperationStatus.InProgress,
                            saveError = (issueStatus as? OperationStatus.Error)?.message,
                            saveSuccess = issueStatus is OperationStatus.Success,
                        )
                    }
                }
        }
    }
}

private data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
)
