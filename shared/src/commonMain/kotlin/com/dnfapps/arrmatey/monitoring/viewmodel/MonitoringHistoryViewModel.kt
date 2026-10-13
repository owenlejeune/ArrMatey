package com.dnfapps.arrmatey.monitoring.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.state.toHttpError
import com.dnfapps.arrmatey.instances.repository.PlaybackMonitoringRepository
import com.dnfapps.arrmatey.instances.usecase.GetPlaybackMonitoringRepositoryUseCase
import com.dnfapps.arrmatey.monitoring.model.PlaybackHistoryItem
import com.dnfapps.arrmatey.monitoring.model.PlaybackStreamSession
import com.dnfapps.arrmatey.monitoring.model.toStreamSession
import com.dnfapps.arrmatey.monitoring.state.MonitoringHistoryState
import com.dnfapps.networking.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MonitoringHistoryViewModel(
    private val getPlaybackMonitoringRepositoryUseCase: GetPlaybackMonitoringRepositoryUseCase,
) : ViewModel() {
    val currentRepository: StateFlow<PlaybackMonitoringRepository?> =
        getPlaybackMonitoringRepositoryUseCase
            .observeSelected()
            .distinctUntilChanged { old, new -> old?.instance == new?.instance }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null,
            )

    private val _state = MutableStateFlow<MonitoringHistoryState>(MonitoringHistoryState.Initial)
    val state: StateFlow<MonitoringHistoryState> = _state.asStateFlow()

    private val _selectedSession = MutableStateFlow<PlaybackStreamSession?>(null)
    val selectedSession: StateFlow<PlaybackStreamSession?> = _selectedSession.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var isLoadingPage = false

    init {
        viewModelScope.launch {
            currentRepository.collect { repo ->
                if (repo == null) {
                    _state.value = MonitoringHistoryState.NoInstance
                } else {
                    loadInitialHistory(repo)
                }
            }
        }
    }

    fun refresh() {
        val repo = currentRepository.value ?: return
        viewModelScope.launch {
            _isRefreshing.value = true
            loadInitialHistory(repo)
            _isRefreshing.value = false
        }
    }

    fun loadMore() {
        val repo = currentRepository.value ?: return
        val currentState = _state.value as? MonitoringHistoryState.Success ?: return
        if (!currentState.hasMore || isLoadingPage || currentState.isLoadingMore) return

        viewModelScope.launch {
            isLoadingPage = true
            _state.update {
                if (it is MonitoringHistoryState.Success) {
                    it.copy(isLoadingMore = true)
                } else {
                    it
                }
            }

            val offset = currentState.items.size
            when (val result = repo.getPlaybackHistory(pageOrStart = offset, pageSize = 25)) {
                is NetworkResult.Success -> {
                    val newItems = result.data
                    val hasMore = newItems.isNotEmpty() && newItems.size >= 25
                    _state.update { old ->
                        if (old is MonitoringHistoryState.Success) {
                            old.copy(
                                items = old.items + newItems,
                                isLoadingMore = false,
                                hasMore = hasMore,
                            )
                        } else {
                            old
                        }
                    }
                }
                is NetworkResult.Error -> {
                    _state.update { old ->
                        if (old is MonitoringHistoryState.Success) {
                            old.copy(isLoadingMore = false)
                        } else {
                            old
                        }
                    }
                }
                else -> {}
            }
            isLoadingPage = false
        }
    }

    private suspend fun loadInitialHistory(repo: PlaybackMonitoringRepository) {
        if (_state.value !is MonitoringHistoryState.Success) {
            _state.value = MonitoringHistoryState.Loading
        }

        when (val result = repo.getPlaybackHistory(pageOrStart = 0, pageSize = 25)) {
            is NetworkResult.Success -> {
                val hasMore = result.data.size >= 25
                _state.value =
                    MonitoringHistoryState.Success(
                        instance = repo.instance,
                        items = result.data,
                        hasMore = hasMore,
                        isLoadingMore = false,
                    )
            }
            is NetworkResult.Error -> {
                _state.value =
                    MonitoringHistoryState.Error(
                        message = result.message ?: "Failed to load history",
                        type = result.errorType.toHttpError(),
                    )
            }
            else -> {}
        }
    }

    fun setSelectedHistoryStream(historyItem: PlaybackHistoryItem) {
        _selectedSession.value = historyItem.toStreamSession()
    }

    fun clearSelected() {
        _selectedSession.value = null
    }
}
