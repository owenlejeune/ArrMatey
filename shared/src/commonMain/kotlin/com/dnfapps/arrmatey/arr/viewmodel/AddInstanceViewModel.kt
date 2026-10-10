package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.instances.model.HeaderRestrictionType
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceHeader
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.state.AddInstanceUiState
import com.dnfapps.arrmatey.instances.usecase.CreateInstanceUseCase
import com.dnfapps.arrmatey.instances.usecase.DismissInfoCardUseCase
import com.dnfapps.arrmatey.instances.usecase.TestNewInstanceConnectionUseCase
import com.dnfapps.arrmatey.utils.isValidUrl
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddInstanceViewModel(
    private val testNewInstanceConnectionUseCase: TestNewInstanceConnectionUseCase,
    private val createInstanceUseCase: CreateInstanceUseCase,
    private val dismissInfoCardUseCase: DismissInfoCardUseCase,
    preferencesStore: PreferencesStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddInstanceUiState())
    val uiState: StateFlow<AddInstanceUiState> = _uiState.asStateFlow()

    private var testJob: Job? = null
    private var localTestJob: Job? = null
    private var createJob: Job? = null

    init {
        viewModelScope.launch {
            preferencesStore.showInfoCards.collect { map ->
                _uiState.update { it.copy(infoCardMaps = map) }
            }
        }
    }

    private fun cancelActiveTesting() {
        if (_uiState.value.testing || testJob?.isActive == true || createJob?.isActive == true) {
            testJob?.cancel()
            testJob = null
            createJob?.cancel()
            createJob = null
            _uiState.update { it.copy(testing = false, testResult = null) }
        }
        if (_uiState.value.localTesting || localTestJob?.isActive == true) {
            localTestJob?.cancel()
            localTestJob = null
            _uiState.update { it.copy(localTesting = false, localTestResult = null) }
        }
    }

    fun setApiEndpoint(endpoint: String) {
        cancelActiveTesting()
        _uiState.update {
            it
                .copy(
                    apiEndpoint = endpoint,
                    testResult = null,
                    endpointError = false,
                ).validate()
        }
    }

    fun setApiKey(value: String) {
        cancelActiveTesting()
        _uiState.update {
            it
                .copy(
                    apiKey = if (it.noApiKeyRequired) "" else value,
                    testResult = null,
                ).validate()
        }
    }

    fun setNoApiKeyRequired(enabled: Boolean) {
        cancelActiveTesting()
        _uiState.update {
            it
                .copy(
                    noApiKeyRequired = enabled,
                    apiKey = if (enabled) "" else it.apiKey,
                    testResult = null,
                ).validate()
        }
    }

    fun setIsSlowInstance(value: Boolean) {
        cancelActiveTesting()
        _uiState.update { it.copy(isSlowInstance = value, testResult = null).validate() }
    }

    fun setCustomTimeout(value: Long?) {
        cancelActiveTesting()
        _uiState.update { it.copy(customTimeout = value?.takeIf { v -> v > 0L }, testResult = null).validate() }
    }

    fun setInstanceLabel(value: String) {
        cancelActiveTesting()
        _uiState.update {
            it.copy(instanceLabel = value, testResult = null).validate()
        }
    }

    fun updateHeaders(headers: List<InstanceHeader>) {
        cancelActiveTesting()
        _uiState.update {
            it.copy(headers = headers, testResult = null).validate()
        }
    }

    fun setLocalNetworkEnabled(enabled: Boolean) {
        cancelActiveTesting()
        _uiState.update { it.copy(localNetworkEnabled = enabled, localTestResult = null).validate() }
    }

    fun setLocalNetworkUrl(url: String) {
        cancelActiveTesting()
        _uiState.update { it.copy(localNetworkUrl = url, localNetworkUrlError = false, localTestResult = null).validate() }
    }

    fun setLocalNetworkSsid(ssids: List<String>) {
        cancelActiveTesting()
        _uiState.update { it.copy(localNetworkSsids = ssids, localTestResult = null).validate() }
    }

    fun toggleNotificationsEnabled() {
        cancelActiveTesting()
        _uiState.update {
            it.copy(notificationsEnabled = !it.notificationsEnabled)
        }
    }

    fun reset() {
        cancelActiveTesting()
        _uiState.value =
            AddInstanceUiState(
                infoCardMaps = _uiState.value.infoCardMaps,
            )
    }

    fun dismissInfoCard(instanceType: InstanceType) {
        dismissInfoCardUseCase(instanceType)
    }

    fun testConnection(type: InstanceType) {
        val state = _uiState.value
        if (state.testing) return

        testJob = viewModelScope.launch {
            if (!state.apiEndpoint.isValidUrl()) {
                _uiState.update { it.copy(endpointError = true, testing = false) }
                return@launch
            }

            _uiState.update { it.copy(testing = true, endpointError = false) }

            val success =
                testNewInstanceConnectionUseCase(
                    state.apiEndpoint,
                    state.apiKey,
                    type,
                    state.headers,
                    state.noApiKeyRequired,
                )

            _uiState.update {
                it
                    .copy(
                        testing = false,
                        testResult = success,
                    ).validate()
            }
        }
    }

    fun testLocalConnection(type: InstanceType) {
        val state = _uiState.value
        if (state.localTesting || state.localNetworkUrl.isBlank()) return

        localTestJob = viewModelScope.launch {
            if (!state.localNetworkUrl.isValidUrl()) {
                _uiState.update { it.copy(localNetworkUrlError = true, localTesting = false) }
                return@launch
            }

            _uiState.update { it.copy(localTesting = true, localNetworkUrlError = false) }

            val success =
                testNewInstanceConnectionUseCase(
                    state.localNetworkUrl,
                    state.apiKey,
                    type,
                    state.headers,
                    state.noApiKeyRequired,
                )

            _uiState.update {
                it.copy(
                    localTesting = false,
                    localTestResult = success,
                )
            }
        }
    }

    fun createInstance(type: InstanceType) {
        val s = _uiState.value
        if (s.testing) return

        createJob = viewModelScope.launch {
            if (!s.apiEndpoint.isValidUrl()) {
                _uiState.update { it.copy(endpointError = true, testing = false) }
                return@launch
            }

            _uiState.update { it.copy(testing = true, endpointError = false, testResult = null) }

            val success =
                testNewInstanceConnectionUseCase(
                    s.apiEndpoint,
                    s.apiKey,
                    type,
                    s.headers,
                    s.noApiKeyRequired,
                )

            if (!success) {
                _uiState.update {
                    it.copy(
                        testing = false,
                        testResult = false,
                    ).validate()
                }
                return@launch
            }

            val instance =
                Instance(
                    type = type,
                    label = s.instanceLabel,
                    url = s.apiEndpoint.trimEnd('/'),
                    apiKey = EncryptedString(s.apiKey),
                    noApiKeyRequired = s.noApiKeyRequired,
                    slowInstance = s.isSlowInstance,
                    customTimeout = if (s.isSlowInstance) s.customTimeout else null,
                    headers = s.headers.filter { it.key.isNotEmpty() && it.value.isNotEmpty() },
                    localNetworkEnabled = s.localNetworkEnabled,
                    localNetworkEndpoint = s.localNetworkUrl.takeIf { s.localNetworkEnabled && it.isNotBlank() },
                    localNetworkSsids = s.localNetworkSsids.filter { it.isNotBlank() },
                )

            val result = createInstanceUseCase(instance)
            _uiState.update {
                it.copy(
                    testing = false,
                    testResult = true,
                    createResult = result,
                ).validate()
            }
        }
    }

    private fun AddInstanceUiState.validate(): AddInstanceUiState {
        val isValid =
            apiEndpoint.isNotEmpty() &&
                (noApiKeyRequired || apiKey.isNotEmpty()) &&
                instanceLabel.isNotEmpty() &&
                (!localNetworkEnabled || (localNetworkUrl.isValidUrl() && localNetworkSsids.isNotEmpty())) &&
                headers.all { it.restrictionType != HeaderRestrictionType.SpecificSsids || it.restrictedSsids.isNotEmpty() }
        return copy(saveButtonEnabled = isValid)
    }
}
