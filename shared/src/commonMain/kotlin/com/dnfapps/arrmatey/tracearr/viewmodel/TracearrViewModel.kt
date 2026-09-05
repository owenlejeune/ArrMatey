package com.dnfapps.arrmatey.tracearr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStreamSession
import com.dnfapps.arrmatey.tracearr.state.TracearrStreamsState
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TracearrViewModel(
    private val instanceManager: InstanceManager,
) : ViewModel() {

    private val _state = MutableStateFlow<TracearrStreamsState>(TracearrStreamsState.Initial)
    val state: StateFlow<TracearrStreamsState> = _state.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadStreams()
    }

    fun loadStreams() {
        viewModelScope.launch {
            if (_state.value !is TracearrStreamsState.Success) {
                _state.value = TracearrStreamsState.Loading
            }
            fetchStreams()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            fetchStreams()
            _isRefreshing.value = false
        }
    }

    private suspend fun fetchStreams() {
        val repos = instanceManager.getAllTracearrRepositories()
        if (repos.isEmpty()) {
            _state.value = TracearrStreamsState.NoInstance
            return
        }

        val allStreams = mutableListOf<TracearrStreamSession>()
        var errorCount = 0

        for (repo in repos) {
            repo.getPublicStreams().onSuccess { response ->
                allStreams.addAll(response.data)
            }.onError { _, _, _ ->
                errorCount++
            }
        }

        if (allStreams.isNotEmpty() || errorCount < repos.size) {
            _state.value = TracearrStreamsState.Success(allStreams)
        } else {
            _state.value = TracearrStreamsState.Error("Failed to fetch streams")
        }
    }
}
