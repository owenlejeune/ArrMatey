package com.dnfapps.arrmatey.seerr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.instances.usecase.GetSeerrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.seerr.api.model.ApprovalStatus
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.api.model.SeerrUser
import com.dnfapps.arrmatey.seerr.api.model.TvDetails
import com.dnfapps.arrmatey.seerr.api.model.UserPermission
import com.dnfapps.arrmatey.seerr.state.MediaButtonState
import com.dnfapps.arrmatey.seerr.state.SeerrDetailsState
import com.dnfapps.arrmatey.seerr.state.toButtonState
import com.dnfapps.arrmatey.seerr.usecase.CancelRequestUseCase
import com.dnfapps.arrmatey.seerr.usecase.GetCurrentSeerrUserUseCase
import com.dnfapps.arrmatey.seerr.usecase.GetSeerrMediaDetailsRatingsUseCase
import com.dnfapps.arrmatey.seerr.usecase.GetSeerrMediaDetailsUseCase
import com.dnfapps.arrmatey.seerr.usecase.SetRequestApprovalStatusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SeerrMediaDetailsViewModel(
    private val tmdbId: Long,
    private val mediaType: RequestType,
    private val getSeerrInstanceRepositoryUseCase: GetSeerrInstanceRepositoryUseCase,
    private val getSeerrMediaDetailsUseCase: GetSeerrMediaDetailsUseCase,
    private val getCurrentSeerrUserUseCase: GetCurrentSeerrUserUseCase,
    private val setRequestApprovalStatusUseCase: SetRequestApprovalStatusUseCase,
    private val cancelRequestUseCase: CancelRequestUseCase,
    private val getSeerrMediaDetailsRatingsUseCase: GetSeerrMediaDetailsRatingsUseCase
): ViewModel() {

    private val _uiState = MutableStateFlow<SeerrDetailsState>(SeerrDetailsState.Initial)
    val uiState: MutableStateFlow<SeerrDetailsState> = _uiState

    private val _currentUser = MutableStateFlow<SeerrUser?>(null)
    val currentUser: StateFlow<SeerrUser?> = _currentUser.asStateFlow()

    val buttonState: StateFlow<MediaButtonState> = combine(
        _uiState,
        _currentUser,
        getSeerrMediaDetailsRatingsUseCase(tmdbId)
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
            getCurrentSeerrUserUseCase(repository)
                .collect { state ->
                    _currentUser.value = state
                }
        }
    }

    fun refreshDetails() {
        currentRepository?.let {
            loadData(it)
        }
    }

    fun approveRequest(requestId: Long) {
        val repository = currentRepository ?: return
        viewModelScope.launch {
            setRequestApprovalStatusUseCase(requestId, ApprovalStatus.Approve, repository)
                .onSuccess { refreshDetails() }
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

}