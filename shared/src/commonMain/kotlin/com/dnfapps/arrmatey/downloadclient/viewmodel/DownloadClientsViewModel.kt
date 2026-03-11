package com.dnfapps.arrmatey.downloadclient.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.service.DownloadClientsState
import com.dnfapps.arrmatey.downloadclient.state.DownloadClientConfigurationUiState
import com.dnfapps.arrmatey.downloadclient.state.DownloadClientConnectionState
import com.dnfapps.arrmatey.downloadclient.state.DownloadClientMutationState
import com.dnfapps.arrmatey.downloadclient.usecase.CreateDownloadClientUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.DeleteDownloadClientUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.GetDownloadClientByIdUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.ObserveDownloadClientsUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.ObserveSelectedDownloadClientsUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.SetDownloadClientActiveUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.TestDownloadClientConnectionUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.UpdateDownloadClientUseCase
import io.ktor.util.reflect.instanceOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DownloadClientsViewModel(
    observeDownloadClientsUseCase: ObserveDownloadClientsUseCase,
    observeSelectedDownloadClientsUseCase: ObserveSelectedDownloadClientsUseCase,
    private val testDownloadClientConnectionUseCase: TestDownloadClientConnectionUseCase,
    private val deleteDownloadClientUseCase: DeleteDownloadClientUseCase,
    private val setDownloadClientActiveUseCase: SetDownloadClientActiveUseCase
): ViewModel() {

    val downloadClientsState: StateFlow<DownloadClientsState> = combine(
        observeDownloadClientsUseCase(),
        observeSelectedDownloadClientsUseCase()
    ) { clients, selected ->
        DownloadClientsState(clients, selected)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DownloadClientsState()
        )

    private val _connectionStates = MutableStateFlow<Map<Long, OperationStatus>>(emptyMap())
    val connectionStates: StateFlow<Map<Long, OperationStatus>> = _connectionStates.asStateFlow()

    private val _mutationState = MutableStateFlow<DownloadClientMutationState>(DownloadClientMutationState.Initial)
    val mutationState: StateFlow<DownloadClientMutationState> = _mutationState.asStateFlow()

    fun testConnection(id: Long) {
        viewModelScope.launch {
            testDownloadClientConnectionUseCase(id).collect { state ->
                _connectionStates.value = _connectionStates.value.toMutableMap().apply {
                    put(id, state)
                }
            }
        }
    }

    fun deleteClient(downloadClient: DownloadClient) {
        viewModelScope.launch {
            runCatching {
                deleteDownloadClientUseCase(downloadClient.id)
            }.onSuccess {
                _mutationState.value = DownloadClientMutationState.Success(downloadClient.id)
            }.onFailure { error ->
                _mutationState.value = DownloadClientMutationState.Error(
                    error.message ?: ""
                )
            }
        }
    }

    fun resetMutationState() {
        _mutationState.value = DownloadClientMutationState.Initial
    }

    fun setClientActive(downloadClient: DownloadClient) {
        viewModelScope.launch {
            setDownloadClientActiveUseCase(downloadClient)
        }
    }
}
