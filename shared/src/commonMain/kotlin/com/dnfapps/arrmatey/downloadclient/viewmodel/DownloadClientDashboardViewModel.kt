package com.dnfapps.arrmatey.downloadclient.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.state.HttpErrorType
import com.dnfapps.arrmatey.arr.state.toHttpError
import com.dnfapps.arrmatey.downloadclient.api.DownloadClientApi
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.repository.DownloadClientManager
import com.dnfapps.arrmatey.downloadclient.state.DownloadClientDashboardUiState
import com.dnfapps.networking.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DownloadClientDashboardViewModel(
    private val clientId: Long,
    private val downloadClientManager: DownloadClientManager,
) : ViewModel() {
    private val _state = MutableStateFlow<DownloadClientDashboardUiState>(DownloadClientDashboardUiState.Initial)
    val state: StateFlow<DownloadClientDashboardUiState> = _state.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _client = MutableStateFlow<DownloadClient?>(null)
    val client: StateFlow<DownloadClient?> = _client.asStateFlow()

    private var api: DownloadClientApi? = null

    init {
        loadClientAndObserve()
    }

    private fun loadClientAndObserve() {
        viewModelScope.launch {
            _state.value = DownloadClientDashboardUiState.Loading
            val client = downloadClientManager.getDownloadClientById(clientId)
            if (client == null) {
                _state.value = DownloadClientDashboardUiState.Error(
                    type = HttpErrorType.Unexpected,
                    message = "Download client not found",
                )
                return@launch
            }
            _client.value = client
            api = downloadClientManager.getOrCreateApi(clientId)
            refresh()
        }
    }

    fun refresh() {
        if (_isRefreshing.value) return

        viewModelScope.launch {
            _isRefreshing.value = true
            val currentApi = api ?: downloadClientManager.getOrCreateApi(clientId)
            if (currentApi == null) {
                _state.value = DownloadClientDashboardUiState.Error(
                    type = HttpErrorType.Unexpected,
                    message = "Could not initialize download client API",
                )
                _isRefreshing.value = false
                return@launch
            }
            api = currentApi

            when (val overviewResult = currentApi.getSystemOverview()) {
                is NetworkResult.Success -> {
                    _state.value = DownloadClientDashboardUiState.Success(
                        overview = overviewResult.data,
                        isRefreshing = false,
                    )
                }
                is NetworkResult.Error -> {
                    if (_state.value !is DownloadClientDashboardUiState.Success) {
                        _state.value = DownloadClientDashboardUiState.Error(
                            type = overviewResult.errorType.toHttpError(),
                            message = overviewResult.message,
                        )
                    }
                }
                is NetworkResult.Loading -> Unit
            }
            _isRefreshing.value = false
        }
    }

    fun deleteClient() {
        viewModelScope.launch {
            _client.value?.let { client ->
                downloadClientManager.deleteClient(client)
            }
        }
    }
}
