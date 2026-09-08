package com.dnfapps.arrmatey.tracearr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.instances.repository.TracearrRepository
import com.dnfapps.arrmatey.instances.usecase.GetTracearrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.tracearr.api.model.TracearrTodayStats
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStreamSession
import com.dnfapps.arrmatey.tracearr.state.TracearrStreamsState
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class TracearrViewModel(
    private val getTracearrInstanceRepositoryUseCase: GetTracearrInstanceRepositoryUseCase,
    private val getTracearrStreamsUseCase: GetTracearrStreamsUseCase,
    private val getTracearrStatsUseCase: GetTracearrStatsTodayUseCase,
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

    private val _state = MutableStateFlow<TracearrStreamsState>(TracearrStreamsState.Initial)
    val state: StateFlow<TracearrStreamsState> = _state.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var pollingJob: Job? = null

    init {
        viewModelScope.launch {
            currentRepository.collect { repo ->
                if (repo == null) {
                    _state.value = TracearrStreamsState.NoInstance
                } else {
                    refreshData(repo, showLoading = _state.value !is TracearrStreamsState.Success)
                }
            }
        }

        startPolling()
    }

    fun loadStreams() {
        val repo = currentRepository.value ?: return
        viewModelScope.launch {
            refreshData(repo, showLoading = _state.value !is TracearrStreamsState.Success)
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

    private suspend fun refreshData(repo: TracearrRepository, showLoading: Boolean) {
        if (showLoading) {
            _state.value = TracearrStreamsState.Loading
        }

        var streams: List<TracearrStreamSession>? = null
        var stats: TracearrTodayStats? = null
        var errorMessage: String? = null

        getTracearrStreamsUseCase(repo).onSuccess { response ->
            streams = response.data
        }.onError { _, msg, _ ->
            errorMessage = msg ?: "Failed to fetch streams"
        }

        getTracearrStatsUseCase(repo).onSuccess {
            stats = it
        }

        if (streams != null) {
            _state.value = TracearrStreamsState.Success(
                streams = streams,
                stats = stats,
            )
        } else if (showLoading || _state.value !is TracearrStreamsState.Success) {
            _state.value = TracearrStreamsState.Error(errorMessage ?: "Failed to load Tracearr data")
        }
    }
}
