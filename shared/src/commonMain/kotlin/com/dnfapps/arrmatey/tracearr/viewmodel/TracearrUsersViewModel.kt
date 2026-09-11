package com.dnfapps.arrmatey.tracearr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.instances.repository.TracearrRepository
import com.dnfapps.arrmatey.instances.usecase.GetTracearrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.tracearr.api.model.TracearrUserDetail
import com.dnfapps.arrmatey.tracearr.api.model.TracearrUserStats
import com.dnfapps.arrmatey.tracearr.state.TracearrUsersState
import com.dnfapps.arrmatey.tracearr.usecase.GetTracearrUsersUseCase
import com.dnfapps.arrmatey.tracearr.usecase.GetUserStatsUseCase
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TracearrUsersViewModel(
    getTracearrInstanceRepositoryUseCase: GetTracearrInstanceRepositoryUseCase,
    private val getTracearrUsersUseCase: GetTracearrUsersUseCase,
    private val getUserStatsUseCase: GetUserStatsUseCase,
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

    private val _rawState = MutableStateFlow<TracearrUsersState>(TracearrUsersState.Initial)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val state: StateFlow<TracearrUsersState> =
        combine(_rawState, _searchQuery) { rawState, query ->
            val trimmedQuery = query.trim()
            if (rawState is TracearrUsersState.Success) {
                val filtered =
                    if (trimmedQuery.isBlank()) {
                        rawState.users
                    } else {
                        rawState.users.filter { user ->
                            user.username?.contains(trimmedQuery, ignoreCase = true) == true ||
                                user.email?.contains(trimmedQuery, ignoreCase = true) == true ||
                                user.accounts.any { acc ->
                                    acc.username?.contains(trimmedQuery, ignoreCase = true) == true
                                }
                        }
                    }
                rawState.copy(filteredUsers = filtered, searchQuery = trimmedQuery)
            } else {
                rawState
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TracearrUsersState.Initial,
        )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var nextCursor: String? = null
    private var isLoadingPage = false

    init {
        viewModelScope.launch {
            currentRepository.collect { repo ->
                if (repo == null) {
                    _rawState.value = TracearrUsersState.NoInstance
                } else {
                    loadUsers(repo, isRefresh = true)
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun refresh() {
        val repo = currentRepository.value ?: return
        viewModelScope.launch {
            _isRefreshing.value = true
            loadUsers(repo, isRefresh = true)
            _isRefreshing.value = false
        }
    }

    fun loadMore() {
        val repo = currentRepository.value ?: return
        val currentState = _rawState.value as? TracearrUsersState.Success ?: return
        if (!currentState.hasMore || isLoadingPage || nextCursor.isNullOrBlank()) return

        viewModelScope.launch {
            isLoadingPage = true
            _rawState.update {
                if (it is TracearrUsersState.Success) {
                    it.copy(isLoadingMore = true)
                } else {
                    it
                }
            }

            getTracearrUsersUseCase(repo, cursor = nextCursor, pageSize = 25)
                .onSuccess { response ->
                    val newItems = response.data
                    nextCursor = response.meta?.nextCursor
                    val hasMore = !nextCursor.isNullOrBlank()
                    val newStatsMap = fetchStatsForUsers(repo, newItems)

                    _rawState.update { old ->
                        if (old is TracearrUsersState.Success) {
                            val allUsers = old.users + newItems
                            val allStatsMap = old.userStatsMap + newStatsMap
                            old.copy(
                                users = allUsers,
                                userStatsMap = allStatsMap,
                                isLoadingMore = false,
                                hasMore = hasMore,
                                nextCursor = nextCursor,
                            )
                        } else {
                            old
                        }
                    }
                }.onError { _, _, _ ->
                    _rawState.update { old ->
                        if (old is TracearrUsersState.Success) {
                            old.copy(isLoadingMore = false)
                        } else {
                            old
                        }
                    }
                }

            isLoadingPage = false
        }
    }

    private suspend fun loadUsers(
        repo: TracearrRepository,
        isRefresh: Boolean,
    ) {
        if (isRefresh) {
            _rawState.value = TracearrUsersState.Loading
            nextCursor = null
        }

        isLoadingPage = true

        getTracearrUsersUseCase(repo, cursor = null, pageSize = 25)
            .onSuccess { response ->
                nextCursor = response.meta?.nextCursor
                val hasMore = !nextCursor.isNullOrBlank()
                val statsMap = fetchStatsForUsers(repo, response.data)
                _rawState.value =
                    TracearrUsersState.Success(
                        users = response.data,
                        userStatsMap = statsMap,
                        isLoadingMore = false,
                        hasMore = hasMore,
                        nextCursor = nextCursor,
                    )
            }.onError { _, msg, _ ->
                _rawState.value = TracearrUsersState.Error(msg ?: "Failed to load users")
            }

        isLoadingPage = false
    }

    private suspend fun fetchStatsForUsers(
        repo: TracearrRepository,
        users: List<TracearrUserDetail>,
    ): Map<String, TracearrUserStats> =
        coroutineScope {
            users
                .map { user ->
                    async {
                        var stats: TracearrUserStats? = null
                        getUserStatsUseCase(repo, ref = user.id).onSuccess {
                            stats = it
                        }
                        stats?.let { user.id to it }
                    }
                }.awaitAll()
                .filterNotNull()
                .toMap()
        }
}
