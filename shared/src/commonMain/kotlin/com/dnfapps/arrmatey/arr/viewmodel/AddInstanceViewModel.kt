package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.instances.state.AddInstanceUiState
import com.dnfapps.arrmatey.instances.usecase.CreateInstanceUseCase
import com.dnfapps.arrmatey.instances.usecase.DismissInfoCardUseCase
import com.dnfapps.arrmatey.instances.usecase.TestNewInstanceConnectionUseCase
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.instances.model.HeaderRestrictionType
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceHeader
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.utils.isValidUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddInstanceViewModel(
    private val testNewInstanceConnectionUseCase: TestNewInstanceConnectionUseCase,
    private val createInstanceUseCase: CreateInstanceUseCase,
    private val dismissInfoCardUseCase: DismissInfoCardUseCase,
    preferencesStore: PreferencesStore
): ViewModel() {

    private val _uiState = MutableStateFlow(AddInstanceUiState())
    val uiState: StateFlow<AddInstanceUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesStore.showInfoCards.collect { map ->
                _uiState.update { it.copy(infoCardMaps = map) }
            }
        }
    }

    fun setApiEndpoint(endpoint: String) {
        _uiState.update {
            it.copy(
                apiEndpoint = endpoint,
                testResult = null
            ).validate()
        }
    }

    fun setApiKey(value: String) {
        _uiState.update {
            it.copy(
                apiKey = value,
                testing = false,
                testResult = null
            ).validate()
        }
    }

    fun setIsSlowInstance(value: Boolean) {
        _uiState.update { it.copy(isSlowInstance = value).validate() }
    }

    fun setCustomTimeout(value: Long?) {
        _uiState.update { it.copy(customTimeout = value?.takeIf { v -> v > 0L } ).validate() }
    }

    fun setInstanceLabel(value: String) {
        _uiState.update {
            it.copy(instanceLabel = value).validate()
        }
    }

    fun updateHeaders(headers: List<InstanceHeader>) {
        _uiState.update {
            it.copy(headers = headers).validate()
        }
    }

    fun setLocalNetworkEnabled(enabled: Boolean) {
        _uiState.update { it.copy(localNetworkEnabled = enabled).validate() }
    }

    fun setLocalNetworkUrl(url: String) {
        _uiState.update { it.copy(localNetworkUrl = url).validate() }
    }

    fun setLocalNetworkSsid(ssids: List<String>) {
        _uiState.update { it.copy(localNetworkSsids = ssids).validate() }
    }

    fun toggleNotificationsEnabled() {
        _uiState.update {
            it.copy(notificationsEnabled = !it.notificationsEnabled)
        }
    }

    fun reset() {
        _uiState.value = AddInstanceUiState(
            infoCardMaps = _uiState.value.infoCardMaps
        )
    }

    fun dismissInfoCard(instanceType: InstanceType) {
        dismissInfoCardUseCase(instanceType)
    }


    fun testConnection(type: InstanceType) {
        val state = _uiState.value
        if (state.testing) return

        viewModelScope.launch {
            if (!state.apiEndpoint.isValidUrl()) {
                _uiState.update { it.copy(endpointError = true, testing = false) }
                return@launch
            }

            _uiState.update { it.copy(testing = true, endpointError = false) }

            val success = testNewInstanceConnectionUseCase(state.apiEndpoint, state.apiKey, type, state.headers)

            _uiState.update {
                it.copy(
                    testing = false,
                    testResult = success
                ).validate()
            }
        }
    }

    fun testLocalConnection(type: InstanceType) {
        val state = _uiState.value
        if (state.localTesting || state.localNetworkUrl.isBlank()) return

        viewModelScope.launch {
            if (!state.localNetworkUrl.isValidUrl()) {
                _uiState.update { it.copy(localNetworkUrlError = true, localTesting = false) }
                return@launch
            }

            _uiState.update { it.copy(localTesting = true, localNetworkUrlError = false) }

            val success = testNewInstanceConnectionUseCase(state.localNetworkUrl, state.apiKey, type, state.headers)

            _uiState.update {
                it.copy(
                    localTesting = false,
                    localTestResult = success
                )
            }
        }
    }

    fun createInstance(type: InstanceType) {
        val s = _uiState.value
        val instance = Instance(
            type = type,
            label = s.instanceLabel,
            url = s.apiEndpoint,
            apiKey = s.apiKey,
            slowInstance = s.isSlowInstance,
            customTimeout = if (s.isSlowInstance) s.customTimeout else null,
            headers = s.headers.filter { it.key.isNotEmpty() && it.value.isNotEmpty() },
            localNetworkEnabled = s.localNetworkEnabled,
            localNetworkEndpoint = s.localNetworkUrl.takeIf { s.localNetworkEnabled && it.isNotBlank() },
            localNetworkSsids = s.localNetworkSsids.filter { it.isNotBlank() }
        )

        viewModelScope.launch {
            val result = createInstanceUseCase(instance)
            _uiState.update { it.copy(createResult = result) }
        }
    }

    private fun AddInstanceUiState.validate(): AddInstanceUiState {
        val isValid = testResult == true &&
                apiEndpoint.isNotEmpty() &&
                apiKey.isNotEmpty() &&
                instanceLabel.isNotEmpty() &&
                (!localNetworkEnabled || (localNetworkUrl.isValidUrl() && localNetworkSsids.isNotEmpty())) &&
                headers.all { it.restrictionType != HeaderRestrictionType.SpecificSsids || it.restrictedSsids.isNotEmpty() }
        return copy(saveButtonEnabled = isValid)
    }
}