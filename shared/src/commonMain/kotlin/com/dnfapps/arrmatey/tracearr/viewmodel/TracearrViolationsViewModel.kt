package com.dnfapps.arrmatey.tracearr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.instances.repository.TracearrRepository
import com.dnfapps.arrmatey.instances.usecase.GetTracearrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.tracearr.state.TracearrViolationsState
import com.dnfapps.arrmatey.tracearr.usecase.GetTracearrViolationsUseCase
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TracearrViolationsViewModel(
    getTracearrInstanceRepositoryUseCase: GetTracearrInstanceRepositoryUseCase,
    private val getTracearrViolationsUseCase: GetTracearrViolationsUseCase,
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

    private val _rawState = MutableStateFlow<TracearrViolationsState>(TracearrViolationsState.Initial)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val state: StateFlow<TracearrViolationsState> =
        combine(_rawState, _searchQuery) { rawState, query ->
            val trimmedQuery = query.trim()
            if (rawState is TracearrViolationsState.Success) {
                val filtered = if (trimmedQuery.isBlank()) {
                    rawState.violations
                } else {
                    rawState.violations.filter { v ->
                        v.rule?.name?.contains(trimmedQuery, ignoreCase = true) == true ||
                        v.serverName?.contains(trimmedQuery, ignoreCase = true) == true ||
                        v.user?.username?.contains(trimmedQuery, ignoreCase = true) == true ||
                        v.severity.name.contains(trimmedQuery, ignoreCase = true)
                    }
                }
                rawState.copy(filteredViolations = filtered, searchQuery = trimmedQuery)
            } else {
                rawState
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TracearrViolationsState.Initial,
        )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var currentPage = 1
    private var isLoadingPage = false

    init {
        viewModelScope.launch {
            currentRepository.collect { repo ->
                if (repo == null) {
                    _rawState.value = TracearrViolationsState.NoInstance
                } else {
                    loadViolations(repo, isRefresh = true)
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
            loadViolations(repo, isRefresh = true)
            _isRefreshing.value = false
        }
    }

    fun loadMore() {
        val repo = currentRepository.value ?: return
        val currentState = _rawState.value as? TracearrViolationsState.Success ?: return
        if (!currentState.hasMore || isLoadingPage) return

        viewModelScope.launch {
            isLoadingPage = true
            _rawState.update {
                if (it is TracearrViolationsState.Success) {
                    it.copy(isLoadingMore = true)
                } else it
            }

            val nextPage = currentPage + 1
            getTracearrViolationsUseCase(repo, page = nextPage, pageSize = 25)
                .onSuccess { response ->
                    val newItems = response.data
                    val total = response.meta?.total ?: (currentState.violations.size + newItems.size)
                    val allItems = currentState.violations + newItems
                    val hasMore = allItems.size < total && newItems.isNotEmpty()
                    currentPage = nextPage

                    _rawState.update { old ->
                        if (old is TracearrViolationsState.Success) {
                            old.copy(
                                violations = allItems,
                                isLoadingMore = false,
                                hasMore = hasMore,
                                page = currentPage,
                                total = total,
                            )
                        } else old
                    }
                }
                .onError { _, _, _ ->
                    _rawState.update { old ->
                        if (old is TracearrViolationsState.Success) {
                            old.copy(isLoadingMore = false)
                        } else old
                    }
                }

            isLoadingPage = false
        }
    }

    private suspend fun loadViolations(repo: TracearrRepository, isRefresh: Boolean) {
        if (isRefresh) {
            _rawState.value = TracearrViolationsState.Loading
            currentPage = 1
        }

        isLoadingPage = true

        getTracearrViolationsUseCase(repo, page = 1, pageSize = 25)
            .onSuccess { response ->
                val total = response.meta?.total ?: response.data.size
                val hasMore = response.data.size < total && response.data.isNotEmpty()
                currentPage = 1
                _rawState.value = TracearrViolationsState.Success(
                    violations = response.data,
                    isLoadingMore = false,
                    hasMore = hasMore,
                    page = 1,
                    total = total,
                )
            }
            .onError { _, msg, _ ->
                _rawState.value = TracearrViolationsState.Error(msg ?: "Failed to load violations")
            }

        isLoadingPage = false
    }
}
