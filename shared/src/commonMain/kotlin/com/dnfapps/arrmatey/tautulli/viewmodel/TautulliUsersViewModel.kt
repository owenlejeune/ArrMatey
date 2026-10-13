package com.dnfapps.arrmatey.tautulli.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.instances.repository.TautulliRepository
import com.dnfapps.arrmatey.instances.usecase.GetTautulliInstanceRepositoryUseCase
import com.dnfapps.arrmatey.tautulli.api.model.TautulliUserWatchTime
import com.dnfapps.arrmatey.tautulli.state.TautulliUserItem
import com.dnfapps.arrmatey.tautulli.state.TautulliUsersState
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
import kotlinx.coroutines.launch

class TautulliUsersViewModel(
    getTautulliInstanceRepositoryUseCase: GetTautulliInstanceRepositoryUseCase,
) : ViewModel() {
    val currentRepository: StateFlow<TautulliRepository?> =
        getTautulliInstanceRepositoryUseCase
            .observeSelected()
            .distinctUntilChanged { old, new -> old?.instance == new?.instance }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null,
            )

    private val _rawState = MutableStateFlow<TautulliUsersState>(TautulliUsersState.Initial)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val state: StateFlow<TautulliUsersState> =
        combine(_rawState, _searchQuery) { rawState, query ->
            val trimmedQuery = query.trim()
            if (rawState is TautulliUsersState.Success) {
                val filtered =
                    if (trimmedQuery.isBlank()) {
                        rawState.users
                    } else {
                        rawState.users.filter { item ->
                            item.user.displayName.contains(trimmedQuery, ignoreCase = true) ||
                                item.user.username?.contains(trimmedQuery, ignoreCase = true) == true ||
                                item.user.email?.contains(trimmedQuery, ignoreCase = true) == true ||
                                item.sharedLibraryNames.any { it.contains(trimmedQuery, ignoreCase = true) }
                        }
                    }
                rawState.copy(filteredUsers = filtered, searchQuery = trimmedQuery)
            } else {
                rawState
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TautulliUsersState.Initial,
        )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        viewModelScope.launch {
            currentRepository.collect { repo ->
                if (repo == null) {
                    _rawState.value = TautulliUsersState.NoInstance
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

    private suspend fun loadUsers(
        repo: TautulliRepository,
        isRefresh: Boolean,
    ) {
        if (isRefresh) {
            _rawState.value = TautulliUsersState.Loading
        }

        coroutineScope {
            var librariesMap = emptyMap<String, String>()
            repo.getLibraries().onSuccess { libs ->
                librariesMap = libs.associate { it.sectionId to it.sectionName }
            }

            repo.getUsers()
                .onSuccess { users ->
                    val statsMap = fetchWatchTimeForUsers(repo, users.map { it.userId })
                    val userItems = users.map { user ->
                        val libNames = user.sharedLibraries.map { sectionId ->
                            librariesMap[sectionId] ?: sectionId
                        }
                        TautulliUserItem(
                            user = user,
                            avatarUrl = repo.buildPmsImageUrl(user.avatarUrl),
                            stats = statsMap[user.userId],
                            sharedLibraryNames = libNames,
                            serverName = repo.serverName,
                        )
                    }
                    _rawState.value = TautulliUsersState.Success(users = userItems)
                }.onError { _, msg, _ ->
                    _rawState.value = TautulliUsersState.Error(msg ?: "Failed to load users")
                }
        }
    }

    private suspend fun fetchWatchTimeForUsers(
        repo: TautulliRepository,
        userIds: List<Long>,
    ): Map<Long, TautulliUserWatchTime> = coroutineScope {
        userIds
            .map { userId ->
                async {
                    var stats: TautulliUserWatchTime? = null
                    repo.getUserWatchTimeStats(userId).onSuccess { list ->
                        stats = list.firstOrNull()
                    }
                    stats?.let { userId to it }
                }
            }.awaitAll()
            .filterNotNull()
            .toMap()
    }
}
