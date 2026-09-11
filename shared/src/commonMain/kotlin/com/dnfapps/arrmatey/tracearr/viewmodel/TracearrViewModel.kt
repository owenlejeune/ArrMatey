package com.dnfapps.arrmatey.tracearr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.instances.repository.TracearrRepository
import com.dnfapps.arrmatey.instances.usecase.GetTracearrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.tracearr.api.model.TracearrHistoryItem
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStreamSession
import com.dnfapps.arrmatey.tracearr.api.model.TracearrTodayStats
import com.dnfapps.arrmatey.tracearr.state.TracearrState
import com.dnfapps.arrmatey.tracearr.usecase.GetTracearrHistoryUseCase
import com.dnfapps.arrmatey.tracearr.usecase.GetTracearrStatsTodayUseCase
import com.dnfapps.arrmatey.tracearr.usecase.GetTracearrStreamsUseCase
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class TracearrViewModel(
    private val getTracearrInstanceRepositoryUseCase: GetTracearrInstanceRepositoryUseCase,
    private val getTracearrStreamsUseCase: GetTracearrStreamsUseCase,
    private val getTracearrStatsUseCase: GetTracearrStatsTodayUseCase,
    private val getTracearrHistoryUseCase: GetTracearrHistoryUseCase,
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

    private val _state = MutableStateFlow<TracearrState>(TracearrState.Initial)
    val state: StateFlow<TracearrState> = _state.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _selectedSession = MutableStateFlow<TracearrStreamSession?>(null)
    val selectedSession: StateFlow<TracearrStreamSession?> = _selectedSession.asStateFlow()

    private var pollingJob: Job? = null

    init {
        viewModelScope.launch {
            currentRepository.collect { repo ->
                if (repo == null) {
                    _state.value = TracearrState.NoInstance
                } else {
                    refreshData(repo, showLoading = _state.value !is TracearrState.Success)
                }
            }
        }

        startPolling()
    }

    fun loadStreams() {
        val repo = currentRepository.value ?: return
        viewModelScope.launch {
            refreshData(repo, showLoading = _state.value !is TracearrState.Success)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val repo = currentRepository.value ?: return@launch
            _isRefreshing.value = true
            refreshData(repo, showLoading = false)
            _isRefreshing.value = false
        }
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob =
            viewModelScope.launch {
                while (isActive) {
                    delay(30.seconds)
                    val repo = currentRepository.value ?: continue
                    refreshData(repo, showLoading = false)
                }
            }
    }

    private suspend fun refreshData(
        repo: TracearrRepository,
        showLoading: Boolean,
    ) {
        if (showLoading) {
            _state.value = TracearrState.Loading
        }

        var streams: List<TracearrStreamSession>? = null
        var stats: TracearrTodayStats? = null
        var history: List<TracearrHistoryItem> = emptyList()
        var errorMessage: String? = null

        getTracearrStreamsUseCase(repo)
            .onSuccess { response ->
                streams = response.data
            }.onError { _, msg, _ ->
                errorMessage = msg ?: "Failed to fetch streams"
            }

        getTracearrStatsUseCase(repo).onSuccess {
            stats = it
        }

        getTracearrHistoryUseCase(repo, pageSize = 5).onSuccess { response ->
            history = response.data
        }

        if (streams != null) {
            _state.value =
                TracearrState.Success(
                    streams = streams,
                    stats = stats,
                    history = history,
                )
        } else if (showLoading || _state.value !is TracearrState.Success) {
            _state.value = TracearrState.Error(errorMessage ?: "Failed to load Tracearr data")
        }
    }

    fun setSelectedStream(streamSession: TracearrStreamSession) {
        _selectedSession.update {
            (_state.value as? TracearrState.Success)?.streams?.firstOrNull { s ->
                s.id == streamSession.id
            }
        }
    }

    fun setSelectedHistoryStream(historyItem: TracearrHistoryItem) {
        _selectedSession.update {
            (_state.value as? TracearrState.Success)
                ?.history
                ?.firstOrNull { h ->
                    h.id == historyItem.id
                }?.toStreamSession()
        }
    }

    fun clearSelected() {
        _selectedSession.value = null
    }
}
