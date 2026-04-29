package com.dnfapps.arrmatey.seerr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.instances.usecase.GetSeerrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.seerr.api.model.ApprovalStatus
import com.dnfapps.arrmatey.seerr.api.model.CombinedRatings
import com.dnfapps.arrmatey.seerr.api.model.IssueBody
import com.dnfapps.arrmatey.seerr.api.model.IssueType
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.api.model.RottenTomatoesRating
import com.dnfapps.arrmatey.seerr.api.model.Service
import com.dnfapps.arrmatey.seerr.api.model.ServiceDetails
import com.dnfapps.arrmatey.seerr.api.model.SeerrUser
import com.dnfapps.arrmatey.seerr.api.model.TvDetails
import com.dnfapps.arrmatey.seerr.api.model.UserPermission
import com.dnfapps.arrmatey.seerr.state.MediaButtonState
import com.dnfapps.arrmatey.seerr.state.ReportIssueUiState
import com.dnfapps.arrmatey.seerr.state.SeerrDetailsState
import com.dnfapps.arrmatey.seerr.state.toButtonState
import com.dnfapps.arrmatey.seerr.usecase.CancelRequestUseCase
import com.dnfapps.arrmatey.seerr.usecase.GetSeerrTvRatingsUseCase
import com.dnfapps.arrmatey.seerr.usecase.GetSeerrMediaDetailsUseCase
import com.dnfapps.arrmatey.seerr.usecase.GetSeerrMovieRatingsUseCase
import com.dnfapps.arrmatey.seerr.usecase.SetRequestApprovalStatusUseCase
import com.dnfapps.arrmatey.seerr.usecase.SubmitIssueUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SeerrMediaDetailsViewModel(
    private val tmdbId: Long,
    private val mediaType: RequestType,
    private val getSeerrInstanceRepositoryUseCase: GetSeerrInstanceRepositoryUseCase,
    private val getSeerrMediaDetailsUseCase: GetSeerrMediaDetailsUseCase,
    private val setRequestApprovalStatusUseCase: SetRequestApprovalStatusUseCase,
    private val cancelRequestUseCase: CancelRequestUseCase,
    private val getSeerrTvRatingsUseCase: GetSeerrTvRatingsUseCase,
    private val getSeerrMovieRatingsUseCase: GetSeerrMovieRatingsUseCase,
    private val submitIssueUseCase: SubmitIssueUseCase
): ViewModel() {

    private val _combinedRatings = MutableStateFlow<CombinedRatings?>(null)
    private val _rtRatings = MutableStateFlow<RottenTomatoesRating?>(null)
    private val _uiState = MutableStateFlow<SeerrDetailsState>(SeerrDetailsState.Initial)
    val uiState: StateFlow<SeerrDetailsState> = combine(
        _uiState,
        _rtRatings,
        _combinedRatings
    ) { state, rtRatings, combinedRatings ->
        when (state) {
            is SeerrDetailsState.Success -> state.copy(
                rtRatings = combinedRatings?.rt ?: rtRatings,
                imdbRatings = combinedRatings?.imdb
            )
            else -> state
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SeerrDetailsState.Initial
        )

    private val _isReportIssueSheetVisible = MutableStateFlow(false)
    val isReportIssueSheetVisible: StateFlow<Boolean> = _isReportIssueSheetVisible.asStateFlow()

    private val _isViewRequestSheetVisible = MutableStateFlow(false)
    val isViewRequestSheetVisible: StateFlow<Boolean> = _isViewRequestSheetVisible.asStateFlow()

    private var seerrMediaId: Long? = null

    private val _reportIssueState = MutableStateFlow(ReportIssueUiState())
    val reportIssueState: StateFlow<ReportIssueUiState> = _reportIssueState
        .combine(_uiState) { issueState, uiState ->
            if (uiState is SeerrDetailsState.Success) {
                seerrMediaId = uiState.item.mediaInfo?.id
                if (issueState.saveSuccess) {
                    _isReportIssueSheetVisible.value = false
                }
                issueState.copy(
                    includeSeriesOptions = uiState.item.requestType == RequestType.Tv,
                    mediaTitle = uiState.item.displayTitle,
                    availableSeasons = (uiState.item as? TvDetails)?.seasons ?: emptyList(),
                    saveButtonEnabled = issueState.message.isNotEmpty() && !issueState.saveInProgress
                )
            } else {
                issueState
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ReportIssueUiState()
        )

    private val _currentUser = MutableStateFlow<SeerrUser?>(null)
    val currentUser: StateFlow<SeerrUser?> = _currentUser.asStateFlow()

    private val _radarrServices = MutableStateFlow<List<Service>>(emptyList())
    val radarrServices: StateFlow<List<Service>> = _radarrServices.asStateFlow()

    private val _sonarrServices = MutableStateFlow<List<Service>>(emptyList())
    val sonarrServices: StateFlow<List<Service>> = _sonarrServices.asStateFlow()

    private val _serviceDetails = MutableStateFlow<ServiceDetails?>(null)
    val serviceDetails: StateFlow<ServiceDetails?> = _serviceDetails.asStateFlow()

    val buttonState: StateFlow<MediaButtonState> = combine(
        _uiState,
        _currentUser,
    ) { state, user ->
        when (state) {
            is SeerrDetailsState.Success -> {
                val isAdmin = user?.hasPermission(UserPermission.ADMIN) == true
                val totalSeasonCount = (state.item as? TvDetails)?.numberOfSeasons ?: 0
                state.item.mediaInfo.toButtonState(state.item.relatedVideos, totalSeasonCount, user?.id, isAdmin)
            }
            else -> MediaButtonState()
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MediaButtonState()
        )

    private val _selectedInstance = MutableStateFlow<Instance?>(null)
    val selectedInstance: StateFlow<Instance?> = _selectedInstance.asStateFlow()
    private var currentRepository: SeerrInstanceRepository? = null

    init {
        observeSelectedInstance()
    }

    private fun observeSelectedInstance() {
        viewModelScope.launch {
            getSeerrInstanceRepositoryUseCase.observeSelected()
                .filterNotNull()
                .collectLatest { repository ->
                    currentRepository = repository
                    loadData(repository)
                }
        }
    }

    private fun loadData(repository: SeerrInstanceRepository) {
        viewModelScope.launch {
            getSeerrMediaDetailsUseCase(tmdbId, mediaType, repository)
                .collect { state ->
                    _uiState.value = state
                }
        }
        viewModelScope.launch {
            repository.getRadarrServices()
        }
        viewModelScope.launch {
            repository.getSonarrServices()
        }
        viewModelScope.launch {
            repository.radarrServices.collect { _radarrServices.value = it }
        }
        viewModelScope.launch {
            repository.sonarrServices.collect { _sonarrServices.value = it }
        }
        viewModelScope.launch {
            combine(_uiState, _radarrServices, _sonarrServices) { state, radarr, sonarr ->
                if (state is SeerrDetailsState.Success) {
                    val request = state.item.mediaInfo?.requests?.firstOrNull { it.status == 1 }
                    val serverId = request?.serverId ?: when (state.item.requestType) {
                        RequestType.Movie -> radarr.find { it.isDefault }?.id
                        RequestType.Tv -> sonarr.find { it.isDefault }?.id
                    }
                    if (serverId != null) serverId to state.item.requestType else null
                } else null
            }
                .filterNotNull()
                .distinctUntilChanged()
                .collectLatest { (serverId, type) ->
                    val result = when (type) {
                        RequestType.Movie -> repository.getRadarrDetails(serverId)
                        RequestType.Tv -> repository.getSonarrDetails(serverId)
                    }
                    result.onSuccess { details ->
                        _serviceDetails.value = details
                    }
                }
        }
        viewModelScope.launch {
            getSeerrMovieRatingsUseCase(tmdbId)
                .collect { rt -> _combinedRatings.value = rt }
        }
        viewModelScope.launch {
            getSeerrTvRatingsUseCase(tmdbId)
                .collect { rt -> _rtRatings.value = rt }
        }
    }

    fun refreshDetails() {
        currentRepository?.let {
            loadData(it)
        }
    }

    fun approveRequest(
        requestId: Long,
        profileId: Long? = null,
        rootFolder: String? = null,
        languageProfileId: Long? = null,
        seasons: List<Int>? = null
    ) {
        val repository = currentRepository ?: return
        viewModelScope.launch {
            setRequestApprovalStatusUseCase(
                requestId = requestId,
                approvalStatus = ApprovalStatus.Approve,
                repository = repository,
                profileId = profileId,
                rootFolder = rootFolder,
                languageProfileId = languageProfileId,
                seasons = seasons
            ).onSuccess { refreshDetails() }
        }
    }

    fun declineRequest(requestId: Long) {
        val repository = currentRepository ?: return
        viewModelScope.launch {
            setRequestApprovalStatusUseCase(requestId, ApprovalStatus.Decline, repository)
                .onSuccess { refreshDetails() }
        }
    }

    fun cancelRequest(requestId: Long) {
        val repository = currentRepository ?: return
        viewModelScope.launch {
            cancelRequestUseCase(requestId, repository)
                .onSuccess { refreshDetails() }
        }
    }

    fun showViewRequestSheet() {
        _isViewRequestSheetVisible.value = true
    }

    fun hideViewRequestSheet() {
        _isViewRequestSheetVisible.value = false
    }

    fun showReportIssueSheet() {
        _isReportIssueSheetVisible.value = true
    }

    fun hideReportIssueSheet() {
        _isReportIssueSheetVisible.value = false
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
        val issue = IssueBody(
            issueType = state.issueType.value,
            message = state.message,
            mediaId = seerrId,
            problemSeason = state.problemSeason ?: 0,
            problemEpisode = state.problemSeason?.let { state.problemEpisode } ?: 0
        )
        viewModelScope.launch {
            submitIssueUseCase(issue)
                .collect { issueStatus ->
                    _reportIssueState.update {
                        it.copy(
                            saveInProgress = issueStatus == OperationStatus.InProgress,
                            saveError = (issueStatus as? OperationStatus.Error)?.message,
                            saveSuccess = issueStatus is OperationStatus.Success
                        )
                    }
                }
        }
    }

}