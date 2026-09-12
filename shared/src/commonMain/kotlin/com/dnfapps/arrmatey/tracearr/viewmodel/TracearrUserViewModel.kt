package com.dnfapps.arrmatey.tracearr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.instances.repository.TracearrRepository
import com.dnfapps.arrmatey.instances.usecase.GetTracearrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.tracearr.api.model.TracearrHistoryItem
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStreamSession
import com.dnfapps.arrmatey.tracearr.api.model.TracearrUserDetail
import com.dnfapps.arrmatey.tracearr.api.model.TracearrUserStats
import com.dnfapps.arrmatey.tracearr.state.TracearrUserState
import com.dnfapps.arrmatey.tracearr.usecase.GetUserDetailUseCase
import com.dnfapps.arrmatey.tracearr.usecase.GetUserHistoryUseCase
import com.dnfapps.arrmatey.tracearr.usecase.GetUserStatsUseCase
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TracearrUserViewModel(
    val userRef: String,
    getTracearrInstanceRepositoryUseCase: GetTracearrInstanceRepositoryUseCase,
    private val getUserDetailUseCase: GetUserDetailUseCase,
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val getUserHistoryUseCase: GetUserHistoryUseCase,
) : ViewModel() {
    val currentRepository: StateFlow<TracearrRepository?> =
        getTracearrInstanceRepositoryUseCase
            .observeSelected()
            .distinctUntilChanged { old, new -> old?.instance?.id == new?.instance?.id }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null,
            )

    private val _state = MutableStateFlow<TracearrUserState>(TracearrUserState.Initial)
    val state: StateFlow<TracearrUserState> = _state.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _selectedSession = MutableStateFlow<TracearrStreamSession?>(null)
    val selectedSession: StateFlow<TracearrStreamSession?> = _selectedSession.asStateFlow()

    private var nextCursor: String? = null
    private var isLoadingPage = false

    init {
        viewModelScope.launch {
            currentRepository.collect { repo ->
                if (repo == null) {
                    _state.value = TracearrUserState.NoInstance
                } else {
                    loadUserData(repo, isRefresh = true)
                }
            }
        }
    }

    fun refresh() {
        val repo = currentRepository.value ?: return
        viewModelScope.launch {
            _isRefreshing.value = true
            loadUserData(repo, isRefresh = true)
            _isRefreshing.value = false
        }
    }

    fun loadMoreHistory() {
        val repo = currentRepository.value ?: return
        val currentState = _state.value as? TracearrUserState.Success ?: return
        if (!currentState.hasMoreHistory || isLoadingPage || nextCursor == null) return

        viewModelScope.launch {
            isLoadingPage = true
            _state.update {
                if (it is TracearrUserState.Success) {
                    it.copy(isLoadingMoreHistory = true)
                } else {
                    it
                }
            }

            val actualUserId = currentState.userDetail?.id ?: userRef
            getUserHistoryUseCase(repo, ref = actualUserId, cursor = nextCursor, pageSize = 20)
                .onSuccess { response ->
                    val newItems = response.data
                    nextCursor = response.meta?.nextCursor
                    val hasMore = !nextCursor.isNullOrBlank()

                    _state.update { old ->
                        if (old is TracearrUserState.Success) {
                            old.copy(
                                history = old.history + newItems,
                                isLoadingMoreHistory = false,
                                hasMoreHistory = hasMore,
                                nextCursor = nextCursor,
                            )
                        } else {
                            old
                        }
                    }
                }.onError { _, _, _ ->
                    _state.update { old ->
                        if (old is TracearrUserState.Success) {
                            old.copy(isLoadingMoreHistory = false)
                        } else {
                            old
                        }
                    }
                }

            isLoadingPage = false
        }
    }

    private suspend fun loadUserData(
        repo: TracearrRepository,
        isRefresh: Boolean,
    ) {
        if (isRefresh) {
            _state.value = TracearrUserState.Loading
            nextCursor = null
        }

        isLoadingPage = true

        var detail: TracearrUserDetail? = null
        var stats: TracearrUserStats? = null
        var history: List<TracearrHistoryItem> = emptyList()
        var hasMoreHistory = false
        var errorMessage: String? = null

        getUserDetailUseCase(repo, ref = userRef)
            .onSuccess { resolvedDetail ->
                detail = resolvedDetail
                val actualUserId = resolvedDetail.id

                getUserStatsUseCase(repo, ref = actualUserId).onSuccess {
                    stats = it
                }

                getUserHistoryUseCase(repo, ref = actualUserId, cursor = null, pageSize = 20).onSuccess { response ->
                    history = response.data
                    nextCursor = response.meta?.nextCursor
                    hasMoreHistory = !nextCursor.isNullOrBlank()
                }
            }.onError { _, msg, _ ->
                errorMessage = msg ?: "Failed to load user"
            }

        if (detail != null) {
            _state.value =
                TracearrUserState.Success(
                    userDetail = detail,
                    userStats = stats,
                    history = history,
                    isLoadingMoreHistory = false,
                    hasMoreHistory = hasMoreHistory,
                    nextCursor = nextCursor,
                )
        } else {
            _state.value = TracearrUserState.Error(errorMessage ?: "User not found")
        }

        isLoadingPage = false
    }

    fun setSelectedHistoryStream(historyItem: TracearrHistoryItem) {
        _selectedSession.value = historyItem.toStreamSession()
    }

    fun clearSelected() {
        _selectedSession.value = null
    }
}
