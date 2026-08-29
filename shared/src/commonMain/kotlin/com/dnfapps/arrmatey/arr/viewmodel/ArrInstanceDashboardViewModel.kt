package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.state.ArrDashboardState
import com.dnfapps.arrmatey.arr.state.HttpErrorType
import com.dnfapps.arrmatey.arr.usecase.ExecuteArrCommandUseCase
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.instances.usecase.GetArrInstanceRepositoryUseCase
import com.dnfapps.networking.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ArrInstanceDashboardViewModel(
    private val instanceId: Long,
    private val getArrInstanceRepositoryUseCase: GetArrInstanceRepositoryUseCase,
    private val executeArrCommandUseCase: ExecuteArrCommandUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow<ArrDashboardState>(ArrDashboardState.Initial)
    val state: StateFlow<ArrDashboardState> = _state.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _instance = MutableStateFlow<Instance?>(null)
    val instance: StateFlow<Instance?> = _instance.asStateFlow()

    private val _lastCommandResult = MutableStateFlow<Boolean?>(null)
    val lastCommandResult: StateFlow<Boolean?> = _lastCommandResult.asStateFlow()

    private var repository: ArrInstanceRepository? = null

    init {
        loadInstanceAndObserve()
    }

    private fun loadInstanceAndObserve() {
        viewModelScope.launch {
            _state.value = ArrDashboardState.Loading

            repository = getArrInstanceRepositoryUseCase(instanceId)
            val currentRepo = repository
            if (currentRepo == null) {
                _state.value =
                    ArrDashboardState.Error(
                        type = HttpErrorType.Unexpected,
                        message = "Could not connect to instance repository",
                    )
                return@launch
            }
            _instance.value = currentRepo.instance

            refresh()

            combine(
                currentRepo.softwareStatus,
                currentRepo.diskSpace,
                currentRepo.health,
                _isRefreshing,
            ) { software, disks, health, refreshing ->
                ArrDashboardState.Success(
                    softwareStatus = software,
                    disks = disks,
                    healthItems = health,
                    isRefreshing = refreshing,
                )
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    fun refresh() {
        if (_isRefreshing.value) return

        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository?.refreshInstanceStatuses()
            } catch (e: Exception) {
                if (_state.value is ArrDashboardState.Initial) {
                    _state.value = ArrDashboardState.Error(HttpErrorType.Unexpected, e.message)
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun runRssSync() {
        viewModelScope.launch {
            val result = executeArrCommandUseCase.runRssSync(instanceId)
            _lastCommandResult.value = result is NetworkResult.Success
        }
    }

    fun searchAllMissing() {
        viewModelScope.launch {
            val result = executeArrCommandUseCase.searchAllMissing(instanceId)
            _lastCommandResult.value = result is NetworkResult.Success
        }
    }

    fun updateLibrary() {
        viewModelScope.launch {
            val result = executeArrCommandUseCase.updateLibrary(instanceId)
            _lastCommandResult.value = result is NetworkResult.Success
        }
    }

    fun backupDatabase() {
        viewModelScope.launch {
            val result = executeArrCommandUseCase.backupDatabase(instanceId)
            _lastCommandResult.value = result is NetworkResult.Success
        }
    }
}
