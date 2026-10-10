package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.database.dao.InsertResult
import com.dnfapps.arrmatey.instances.model.HeaderRestrictionType
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceHeader
import com.dnfapps.arrmatey.instances.state.AddInstanceUiState
import com.dnfapps.arrmatey.instances.usecase.DeleteInstanceUseCase
import com.dnfapps.arrmatey.instances.usecase.GetInstanceByIdUseCase
import com.dnfapps.arrmatey.instances.usecase.TestNewInstanceConnectionUseCase
import com.dnfapps.arrmatey.instances.usecase.UpdateInstanceUseCase
import com.dnfapps.arrmatey.notifications.NotificationManager
import com.dnfapps.arrmatey.utils.isValidUrl
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditInstanceViewModel(
    private val instanceId: Long,
    private val testNewInstanceConnectionUseCase: TestNewInstanceConnectionUseCase,
    private val updateInstanceUseCase: UpdateInstanceUseCase,
    private val getInstanceByIdUseCase: GetInstanceByIdUseCase,
    private val deleteInstanceUseCase: DeleteInstanceUseCase,
    private val notificationManager: NotificationManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddInstanceUiState())
    val uiState: StateFlow<AddInstanceUiState> = _uiState.asStateFlow()

    private var _instance = MutableStateFlow<Instance?>(null)
    val instance: StateFlow<Instance?> = _instance.asStateFlow()

    private var testJob: Job? = null
    private var localTestJob: Job? = null
    private var updateJob: Job? = null

    init {
        refreshInstance()
    }

    private fun refreshInstance() {
        viewModelScope.launch {
            getInstanceByIdUseCase(instanceId)?.let { instance ->
                _instance.value = instance
                _uiState.update {
                    it.copy(
                        apiEndpoint = instance.url,
                        apiKey = instance.apiKey.value,
                        noApiKeyRequired = instance.noApiKeyRequired,
                        isSlowInstance = instance.slowInstance,
                        customTimeout = instance.customTimeout,
                        instanceLabel = instance.label,
                        headers = instance.headers,
                        localNetworkEnabled = instance.localNetworkEnabled,
                        localNetworkUrl = instance.localNetworkEndpoint ?: "",
                        localNetworkSsids = instance.localNetworkSsids,
                        notificationsEnabled = instance.notificationsEnabled,
                    )
                }
            }
        }
    }

    private fun cancelActiveTesting() {
        if (_uiState.value.testing || testJob?.isActive == true || updateJob?.isActive == true) {
            testJob?.cancel()
            testJob = null
            updateJob?.cancel()
            updateJob = null
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

    fun setLocalNetworkSsids(ssids: List<String>) {
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
        _uiState.value = AddInstanceUiState()
    }

    fun testConnection() {
        val state = _uiState.value
        val type = instance.value?.type ?: return
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

    fun testLocalConnection() {
        val state = _uiState.value
        if (state.localTesting || state.localNetworkUrl.isBlank()) return
        val type = instance.value?.type ?: return

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

    fun updateInstance() {
        val s = _uiState.value
        if (s.testing) return
        val originalInstance =
            instance.value ?: run {
                _uiState.update {
                    it.copy(
                        editResult = InsertResult.Error("Instance doesn't exist"),
                    )
                }
                return
            }

        updateJob = viewModelScope.launch {
            if (!s.apiEndpoint.isValidUrl()) {
                _uiState.update { it.copy(endpointError = true, testing = false) }
                return@launch
            }

            _uiState.update { it.copy(testing = true, endpointError = false, testResult = null) }

            val success =
                testNewInstanceConnectionUseCase(
                    s.apiEndpoint,
                    s.apiKey,
                    originalInstance.type,
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

            val updated =
                originalInstance.copy(
                    label = s.instanceLabel,
                    url = s.apiEndpoint,
                    apiKey = EncryptedString(s.apiKey),
                    noApiKeyRequired = s.noApiKeyRequired,
                    slowInstance = s.isSlowInstance,
                    customTimeout = if (s.isSlowInstance) s.customTimeout else null,
                    headers = s.headers.filter { it.key.isNotEmpty() && it.value.isNotEmpty() },
                    localNetworkEnabled = s.localNetworkEnabled,
                    localNetworkEndpoint = s.localNetworkUrl.takeIf { s.localNetworkEnabled && it.isNotBlank() },
                    localNetworkSsids = s.localNetworkSsids.filter { it.isNotBlank() },
                    notificationsEnabled = s.notificationsEnabled,
                )

            if (originalInstance.notificationsEnabled && !updated.notificationsEnabled) {
                instance.value?.label?.let { instanceName ->
                    notificationManager.cancelNotificationsForInstance(instanceName)
                }
            }
            val result = updateInstanceUseCase(updated)
            _uiState.update {
                it.copy(
                    testing = false,
                    testResult = true,
                    editResult = result,
                ).validate()
            }
        }
    }

    fun deleteInstance(instance: Instance) {
        viewModelScope.launch {
            if (instance.notificationsEnabled) {
                notificationManager.cancelNotificationsForInstance(instance.label)
            }
            deleteInstanceUseCase(instance)
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
