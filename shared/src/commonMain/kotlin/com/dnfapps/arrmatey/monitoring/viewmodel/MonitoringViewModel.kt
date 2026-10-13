package com.dnfapps.arrmatey.monitoring.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.state.HttpErrorType
import com.dnfapps.arrmatey.arr.state.toHttpError
import com.dnfapps.arrmatey.instances.repository.PlaybackMonitoringRepository
import com.dnfapps.arrmatey.instances.repository.TracearrRepository
import com.dnfapps.arrmatey.instances.usecase.GetPlaybackMonitoringRepositoryUseCase
import com.dnfapps.arrmatey.monitoring.model.PlaybackHistoryItem
import com.dnfapps.arrmatey.monitoring.model.PlaybackStreamSession
import com.dnfapps.arrmatey.monitoring.model.toStreamSession
import com.dnfapps.arrmatey.monitoring.state.MonitoringState
import com.dnfapps.arrmatey.monitoring.usecase.TerminatePlaybackStreamUseCase
import com.dnfapps.networking.NetworkResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class MonitoringViewModel(
    private val getPlaybackMonitoringRepositoryUseCase: GetPlaybackMonitoringRepositoryUseCase,
    private val terminatePlaybackStreamUseCase: TerminatePlaybackStreamUseCase,
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

    private val _state = MutableStateFlow<MonitoringState>(MonitoringState.Initial)
    val state: StateFlow<MonitoringState> = _state.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _selectedSession = MutableStateFlow<PlaybackStreamSession?>(null)
    val selectedSession: StateFlow<PlaybackStreamSession?> = _selectedSession.asStateFlow()

    private var pollingJob: Job? = null

    init {
        viewModelScope.launch {
            currentRepository.collect { repo ->
                if (repo == null) {
                    _state.value = MonitoringState.NoInstance
                } else {
                    refreshData(repo, showLoading = _state.value !is MonitoringState.Success)
                }
            }
        }

        startPolling()
    }

    fun loadStreams() {
        val repo = currentRepository.value ?: return
        viewModelScope.launch {
            refreshData(repo, showLoading = _state.value !is MonitoringState.Success)
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
        repo: PlaybackMonitoringRepository,
        showLoading: Boolean,
    ) {
        if (showLoading) {
            _state.value = MonitoringState.Loading
        }

        var streams: List<PlaybackStreamSession>? = null
        var history: List<PlaybackHistoryItem> = emptyList()
        var errorMessage: String? = null
        var errorType: HttpErrorType = HttpErrorType.Unexpected

        when (val res = repo.refreshPlaybackStreams()) {
            is NetworkResult.Success -> {
                streams = res.data
            }
            is NetworkResult.Error -> {
                errorMessage = res.message ?: "Failed to fetch streams"
                errorType = res.errorType.toHttpError()
            }
            else -> {}
        }

        repo.refreshPlaybackStats()

        when (val histRes = repo.getPlaybackHistory(pageOrStart = 0, pageSize = 5)) {
            is NetworkResult.Success -> {
                history = histRes.data
            }
            else -> {}
        }

        if (streams != null) {
            _state.value =
                MonitoringState.Success(
                    instance = repo.instance,
                    streams = streams,
                    stats = repo.summaryStats.value,
                    tracearrStats = (repo as? TracearrRepository)?.todayStats?.value,
                    history = history,
                )
        } else if (showLoading || _state.value !is MonitoringState.Success) {
            _state.value =
                MonitoringState.Error(
                    message = errorMessage ?: "Failed to load monitoring data",
                    type = errorType,
                )
        }
    }

    fun terminateStream(session: PlaybackStreamSession, message: String? = null) {
        viewModelScope.launch {
            terminatePlaybackStreamUseCase(session, message)
            val repo = currentRepository.value ?: return@launch
            refreshData(repo, showLoading = false)
            if (_selectedSession.value?.sessionId == session.sessionId) {
                _selectedSession.value = null
            }
        }
    }

    fun setSelectedStream(streamSession: PlaybackStreamSession) {
        _selectedSession.value = streamSession
    }

    fun setSelectedHistoryStream(historyItem: PlaybackHistoryItem) {
        _selectedSession.value = historyItem.toStreamSession()
    }

    fun clearSelected() {
        _selectedSession.value = null
    }
}
