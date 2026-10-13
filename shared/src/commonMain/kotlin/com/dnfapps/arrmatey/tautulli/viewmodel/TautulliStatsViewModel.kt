package com.dnfapps.arrmatey.tautulli.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.state.toHttpError
import com.dnfapps.arrmatey.instances.repository.TautulliRepository
import com.dnfapps.arrmatey.instances.usecase.GetTautulliInstanceRepositoryUseCase
import com.dnfapps.arrmatey.tautulli.api.model.TautulliStatsType
import com.dnfapps.arrmatey.tautulli.state.TautulliStatsState
import com.dnfapps.networking.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TautulliStatsViewModel(
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

    private val _state = MutableStateFlow<TautulliStatsState>(TautulliStatsState.Initial)
    val state: StateFlow<TautulliStatsState> = _state.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _timeRange = MutableStateFlow(30)
    val timeRange: StateFlow<Int> = _timeRange.asStateFlow()

    private val _statsType = MutableStateFlow(TautulliStatsType.Plays)
    val statsType: StateFlow<TautulliStatsType> = _statsType.asStateFlow()

    init {
        viewModelScope.launch {
            currentRepository.collect { repo ->
                if (repo != null) {
                    loadStats(repo, showLoading = true)
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val repo = currentRepository.value ?: return@launch
            _isRefreshing.value = true
            loadStats(repo, showLoading = false)
            _isRefreshing.value = false
        }
    }

    fun setTimeRange(days: Int) {
        _timeRange.value = days
        val repo = currentRepository.value ?: return
        viewModelScope.launch {
            loadStats(repo, showLoading = true)
        }
    }

    fun setStatsType(type: TautulliStatsType) {
        _statsType.value = type
        val repo = currentRepository.value ?: return
        viewModelScope.launch {
            loadStats(repo, showLoading = true)
        }
    }

    private suspend fun loadStats(repo: TautulliRepository, showLoading: Boolean) {
        if (showLoading) {
            _state.value = TautulliStatsState.Loading
        }
        when (val result = repo.refreshHomeStats(timeRange = _timeRange.value, statsType = _statsType.value)) {
            is NetworkResult.Success -> {
                _state.value =
                    TautulliStatsState.Success(
                        instance = repo.instance,
                        sections = result.data,
                    )
            }
            is NetworkResult.Error -> {
                _state.value =
                    TautulliStatsState.Error(
                        message = result.message ?: "Failed to load stats",
                        type = result.errorType.toHttpError(),
                    )
            }
            else -> {}
        }
    }
}
