package com.dnfapps.arrmatey.tracearr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.instances.repository.TracearrRepository
import com.dnfapps.arrmatey.instances.usecase.GetTracearrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.tracearr.api.model.TracearrPeriod
import com.dnfapps.arrmatey.tracearr.state.TracearrActivityState
import com.dnfapps.arrmatey.tracearr.usecase.GetTracearrActivityUseCase
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TracearrActivityViewModel(
    getTracearrInstanceRepositoryUseCase: GetTracearrInstanceRepositoryUseCase,
    private val getTracearrActivityUseCase: GetTracearrActivityUseCase,
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

    private val _state = MutableStateFlow<TracearrActivityState>(TracearrActivityState.Initial)
    val state: StateFlow<TracearrActivityState> = _state.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(TracearrPeriod.Month)
    val selectedPeriod: StateFlow<TracearrPeriod> = _selectedPeriod.asStateFlow()

    init {
        viewModelScope.launch {
            currentRepository.collect { repo ->
                if (repo == null) {
                    _state.value = TracearrActivityState.NoInstance
                } else {
                    loadActivity(repo, _selectedPeriod.value, isRefresh = true)
                }
            }
        }
    }

    fun setPeriod(period: TracearrPeriod) {
        if (_selectedPeriod.value == period) return
        _selectedPeriod.value = period
        val repo = currentRepository.value ?: return
        viewModelScope.launch {
            loadActivity(repo, period, isRefresh = false)
        }
    }

    fun refresh() {
        val repo = currentRepository.value ?: return
        viewModelScope.launch {
            _isRefreshing.value = true
            loadActivity(repo, _selectedPeriod.value, isRefresh = false)
            _isRefreshing.value = false
        }
    }

    private suspend fun loadActivity(
        repo: TracearrRepository,
        period: TracearrPeriod,
        isRefresh: Boolean,
    ) {
        if (isRefresh) {
            _state.value = TracearrActivityState.Loading
        }

        getTracearrActivityUseCase(repo, period = period)
            .onSuccess { response ->
                _state.value =
                    TracearrActivityState.Success(
                        response = response,
                        period = period,
                    )
            }.onError { _, msg, _ ->
                _state.value = TracearrActivityState.Error(msg ?: "Failed to load activity")
            }
    }
}
