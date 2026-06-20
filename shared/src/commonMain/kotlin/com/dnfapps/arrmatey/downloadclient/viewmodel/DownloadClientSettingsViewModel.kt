package com.dnfapps.arrmatey.downloadclient.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.model.DownloadClientType
import com.dnfapps.arrmatey.downloadclient.repository.DownloadClientManager
import com.dnfapps.arrmatey.downloadclient.state.DownloadClientConfigurationUiState
import com.dnfapps.arrmatey.downloadclient.state.DownloadClientMutationState
import com.dnfapps.arrmatey.downloadclient.usecase.CreateDownloadClientUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.DeleteDownloadClientUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.GetDownloadClientByIdUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.TestDownloadClientConnectionUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.UpdateDownloadClientUseCase
import com.dnfapps.arrmatey.instances.model.InstanceHeader
import com.dnfapps.arrmatey.utils.isValidUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DownloadClientSettingsViewModel(
    private val downloadClientId: Long?,
    private val testDownloadClientConnectionUseCase: TestDownloadClientConnectionUseCase,
    private val deleteDownloadClientUseCase: DeleteDownloadClientUseCase,
    private val createDownloadClientUseCase: CreateDownloadClientUseCase,
    private val updateDownloadClientUseCase: UpdateDownloadClientUseCase,
    private val getDownloadClientByIdUseCase: GetDownloadClientByIdUseCase,
    private val downloadClientManager: DownloadClientManager
): ViewModel() {

    private val _uiState = MutableStateFlow(DownloadClientConfigurationUiState())
    val uiState: StateFlow<DownloadClientConfigurationUiState> = _uiState.asStateFlow()

    private var _downloadClient = MutableStateFlow<DownloadClient?>(null)
    val downloadClient: StateFlow<DownloadClient?> = _downloadClient.asStateFlow()

    private var originalClient: DownloadClient? = null
    private var pendingClientId: Long? = null

    init {
        refreshClient(downloadClientId)
    }

    private fun refreshClient(clientId: Long?) {
        viewModelScope.launch {
            clientId?.let { id ->
                getDownloadClientByIdUseCase(id)?.let { client ->
                    _downloadClient.value = client
                    originalClient = client
                    _uiState.update {
                        it.copy(
                            label = client.label,
                            selectedType = client.type,
                            url = client.url,
                            username = client.username.value,
                            password = client.password.value,
                            apiKey = client.apiKey.value,
                            noApiKeyRequired = client.noApiKeyRequired,
                            headers = client.headers,
                            isEditing = true,
                            localNetworkEnabled = client.localNetworkEnabled,
                            localNetworkSsids = client.localNetworkSsids,
                            localNetworkEndpoint = client.localNetworkEndpoint ?: ""
                        )
                    }
                }
            }
        }
    }

    fun updateLabel(label: String) {
        _uiState.update { it.copy(label = label) }
    }

    fun updateSelectedType(type: DownloadClientType) {
        _uiState.update { it.copy(selectedType = type) }
    }

    fun updateUrl(url: String) {
        _uiState.update {
            it.copy(
                url = url,
                saveButtonEnabled = url.isNotEmpty() &&
                        (it.noApiKeyRequired || it.apiKey.isNotEmpty()
                                || (it.username.isNotEmpty() && it.password.isNotEmpty()))
            )
        }
    }

    fun updateUsername(username: String) {
        _uiState.update {
            it.copy(
                username = username,
                saveButtonEnabled = it.url.isNotEmpty() &&
                        (it.noApiKeyRequired || it.apiKey.isNotEmpty()
                                || (username.isNotEmpty() && it.password.isNotEmpty()))
            )
        }
    }

    fun updatePassword(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                saveButtonEnabled = it.url.isNotEmpty() &&
                        (it.noApiKeyRequired || it.apiKey.isNotEmpty()
                                || (it.username.isNotEmpty() && password.isNotEmpty()))
            )
        }
    }

    fun updateApiKey(apiKey: String) {
        _uiState.update {
            val newApiKey = if (it.noApiKeyRequired) "" else apiKey
            it.copy(
                apiKey = newApiKey,
                saveButtonEnabled = it.url.isNotEmpty() &&
                        (it.noApiKeyRequired || newApiKey.isNotEmpty()
                                || (!it.username.isNotEmpty() && !it.password.isNotEmpty()))
            )
        }
    }

    fun updateNoApiKeyRequired(enabled: Boolean) {
        _uiState.update {
            val newApiKey = if (enabled) "" else it.apiKey
            val newUsername = if (enabled) "" else it.username
            val newPassword = if (enabled) "" else it.password
            it.copy(
                noApiKeyRequired = enabled,
                apiKey = newApiKey,
                username = newUsername,
                password = newPassword,
                saveButtonEnabled = it.url.isNotEmpty() &&
                        (enabled || newApiKey.isNotEmpty()
                                || (!newUsername.isNotEmpty() && !newPassword.isNotEmpty()))
            )
        }
    }

    fun updateHeaders(headers: List<InstanceHeader>) {
        _uiState.update { it.copy(headers = headers) }
    }

    fun updateLocalNetworkEnabled(enabled: Boolean) {
        _uiState.update { it.copy(localNetworkEnabled = enabled) }
    }

    fun updateLocalNetworkUrl(url: String) {
        _uiState.update { it.copy(localNetworkEndpoint = url, localNetworkEndpointError = false) }
    }

    fun updateLocalNetworkSsid(ssids: List<String>) {
        _uiState.update { it.copy(localNetworkSsids = ssids) }
    }

    fun testConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isTesting = true, testResult = null) }
            val client = buildDownloadClient()
            val resultFlow = testDownloadClientConnectionUseCase(client)
            resultFlow.collect { status ->
                if (status !is OperationStatus.InProgress) {
                    _uiState.update {
                        it.copy(
                            isTesting = false,
                            testResult = status is OperationStatus.Success
                        )
                    }
                }
            }
        }
    }

    fun testLocalConnection() {
        viewModelScope.launch {
            if (!uiState.value.localNetworkEndpoint.isValidUrl()) {
                _uiState.update { it.copy(localNetworkEndpointError = true) }
                return@launch
            }
            _uiState.update { it.copy(localTesting = true, localTestResult = null) }
            val client = buildDownloadClient().copy(
                url = uiState.value.localNetworkEndpoint,
                localNetworkEnabled = false // Force use current URL
            )
            val resultFlow = testDownloadClientConnectionUseCase(client)
            resultFlow.collect { status ->
                if (status !is OperationStatus.InProgress) {
                    _uiState.update {
                        it.copy(
                            localTesting = false,
                            localTestResult = status is OperationStatus.Success
                        )
                    }
                }
            }
        }
    }

    private fun buildDownloadClient(): DownloadClient {
        return DownloadClient(
            id = downloadClient.value?.id ?: 0,
            type = uiState.value.selectedType,
            label = uiState.value.label.takeUnless { it.isEmpty() }
                ?: uiState.value.selectedType.displayName,
            url = uiState.value.url,
            username = EncryptedString(uiState.value.username),
            password = EncryptedString(uiState.value.password),
            apiKey = EncryptedString(uiState.value.apiKey),
            noApiKeyRequired = uiState.value.noApiKeyRequired,
            headers = uiState.value.headers.filter { it.key.isNotEmpty() && it.value.isNotEmpty() },
            selected = downloadClient.value?.selected ?: false,
            localNetworkEnabled = uiState.value.localNetworkEnabled,
            localNetworkSsids = uiState.value.localNetworkSsids,
            localNetworkEndpoint = uiState.value.localNetworkEndpoint
        )
    }

    fun deleteClient() {
        downloadClientId?.let { clientId ->
            viewModelScope.launch {
                runCatching {
                    deleteDownloadClientUseCase(clientId)
                    downloadClientManager.removeApi(clientId)
                }.onSuccess {
                    _uiState.update {
                        it.copy(mutationState = DownloadClientMutationState.Success(clientId))
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            mutationState = DownloadClientMutationState.Error(
                                error.message ?: ""
                            )
                        )
                    }
                }
            }
        }
    }

    fun submit() {
        val newClient = buildDownloadClient()

        if (uiState.value.isEditing) {
            updateClient(newClient)
        } else {
            createClient(newClient)
        }
    }

    private fun createClient(downloadClient: DownloadClient) {
        viewModelScope.launch {
            if (!_uiState.value.url.isValidUrl()) {
                _uiState.update { it.copy(endpointError = true, isTesting = false) }
                return@launch
            }
            _uiState.update { it.copy(isTesting = true) }

            when (val createResult = createDownloadClientUseCase(downloadClient)) {
                is DownloadClientMutationState.Success -> {
                    val createdId = createResult.id
                    pendingClientId = createdId

                    _uiState.update { it.copy(mutationState = DownloadClientMutationState.Testing) }

                    testDownloadClientConnectionUseCase(createdId, forceRefresh = true)
                        .collect { testStatus ->
                            when (testStatus) {
                                is OperationStatus.Success -> {
                                    pendingClientId = null
                                    _uiState.update {
                                        it.copy(
                                            mutationState = DownloadClientMutationState.Success(
                                                createdId
                                            ),
                                            isTesting = false
                                        )
                                    }
                                }

                                is OperationStatus.Error -> {
                                    deleteDownloadClientUseCase(createdId)
                                    downloadClientManager.removeApi(createdId)
                                    pendingClientId = null

                                    _uiState.update {
                                        it.copy(
                                            mutationState = DownloadClientMutationState.ConnectionFailed(
                                                testStatus.message ?: "Connection test failed"
                                            ),
                                            isTesting = false,
                                            isEditing = false
                                        )
                                    }
                                }

                                else -> {}
                            }
                        }

                }
                is DownloadClientMutationState.Conflict -> {
                    _uiState.update {
                        it.copy(mutationState = createResult, isTesting = false)
                    }
                }
                is DownloadClientMutationState.Error -> {
                    _uiState.update {
                        it.copy(mutationState = createResult, isTesting = false)
                    }
                }
                else -> {
                    _uiState.update {
                        it.copy(
                            mutationState = DownloadClientMutationState.Error("Unexpected error"),
                            isTesting = false
                        )
                    }
                }
            }
        }
    }

    private fun updateClient(downloadClient: DownloadClient) {
        viewModelScope.launch {
            _uiState.update { it.copy(isTesting = true) }

            when (val updateResult = updateDownloadClientUseCase(downloadClient)) {
                is DownloadClientMutationState.Success -> {
                    val updatedId = updateResult.id

                    _uiState.update { it.copy(mutationState = DownloadClientMutationState.Testing) }
                    testDownloadClientConnectionUseCase(updatedId, forceRefresh = true)
                        .collect { testStatus ->
                            when (testStatus) {
                                is OperationStatus.Success -> {
                                    _uiState.update {
                                        it.copy(
                                            mutationState = DownloadClientMutationState.Success(updatedId),
                                            isTesting = false
                                        )
                                    }
                                }
                                is OperationStatus.Error -> {
                                    originalClient?.let { original ->
                                        updateDownloadClientUseCase(original)
                                        downloadClientManager.refreshApi(updatedId)
                                    }
                                    _uiState.update {
                                        it.copy(
                                            mutationState = DownloadClientMutationState.ConnectionFailed(
                                                testStatus.message ?: "Connection test failed"
                                            ),
                                            isTesting = false
                                        )
                                    }
                                }
                                else -> {}
                            }
                    }
                }
                is DownloadClientMutationState.Conflict -> {
                    _uiState.update {
                        it.copy(mutationState = updateResult, isTesting = false)
                    }
                }
                is DownloadClientMutationState.Error -> {
                    _uiState.update {
                        it.copy(mutationState = updateResult, isTesting = false)
                    }
                }
                else -> {
                    _uiState.update {
                        it.copy(
                            mutationState = DownloadClientMutationState.Error("Unexpected error"),
                            isTesting = false
                        )
                    }
                }
            }
        }
    }

    fun resetMutationState() {
        _uiState.update {
            it.copy(mutationState = DownloadClientMutationState.Initial)
        }
    }

    override fun onCleared() {
        super.onCleared()
        pendingClientId?.let { id ->
            viewModelScope.launch {
                deleteDownloadClientUseCase(id)
                downloadClientManager.removeApi(id)
            }
        }
    }
}