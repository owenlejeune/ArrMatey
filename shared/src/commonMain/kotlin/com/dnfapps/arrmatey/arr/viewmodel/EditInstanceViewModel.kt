package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.database.dao.InsertResult
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceHeader
import com.dnfapps.arrmatey.instances.model.SeerrAuthType
import com.dnfapps.arrmatey.instances.state.AddInstanceUiState
import com.dnfapps.arrmatey.instances.usecase.DeleteInstanceUseCase
import com.dnfapps.arrmatey.instances.usecase.GetInstanceByIdUseCase
import com.dnfapps.arrmatey.instances.usecase.PlexAuthUseCase
import com.dnfapps.arrmatey.instances.usecase.SeerrAuthUseCase
import com.dnfapps.arrmatey.instances.usecase.TestNewInstanceConnectionUseCase
import com.dnfapps.arrmatey.instances.usecase.UpdateInstanceUseCase
import com.dnfapps.arrmatey.notifications.NotificationManager
import com.dnfapps.arrmatey.utils.isValidUrl
import com.dnfapps.networking.NetworkResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

class EditInstanceViewModel(
    private val instanceId: Long,
    private val testNewInstanceConnectionUseCase: TestNewInstanceConnectionUseCase,
    private val updateInstanceUseCase: UpdateInstanceUseCase,
    private val getInstanceByIdUseCase: GetInstanceByIdUseCase,
    private val deleteInstanceUseCase: DeleteInstanceUseCase,
    private val notificationManager: NotificationManager,
    private val seerrAuthUseCase: SeerrAuthUseCase,
    private val plexAuthUseCase: PlexAuthUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddInstanceUiState())
    val uiState: StateFlow<AddInstanceUiState> = _uiState.asStateFlow()

    private var _instance = MutableStateFlow<Instance?>(null)
    val instance: StateFlow<Instance?> = _instance.asStateFlow()

    private var testJob: Job? = null
    private var localTestJob: Job? = null
    private var updateJob: Job? = null
    private var quickConnectJob: Job? = null
    private var plexAuthJob: Job? = null

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
                        seerrAuthType = instance.seerrAuthType,
                        sessionToken = instance.sessionToken.value,
                        authenticatedUserDisplayName = if (instance.sessionToken.value.isNotEmpty()) "Active Session" else null,
                    ).validate()
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
        quickConnectJob?.cancel()
        quickConnectJob = null
        _uiState.value = AddInstanceUiState()
    }

    fun setSeerrAuthType(type: SeerrAuthType) {
        cancelActiveTesting()
        cancelJellyfinQuickConnect()
        cancelPlexSignIn()
        _uiState.update {
            it.copy(
                seerrAuthType = type,
                authError = null,
                testResult = null,
                plexAuthUrl = null,
                isPlexWaiting = false,
            ).validate()
        }
    }

    fun loginLocal(email: String, pass: String) {
        val state = _uiState.value
        if (!state.apiEndpoint.isValidUrl()) {
            _uiState.update { it.copy(endpointError = true, authError = "Invalid server URL") }
            return
        }
        _uiState.update { it.copy(isAuthenticating = true, authError = null) }
        viewModelScope.launch {
            when (val res = seerrAuthUseCase.loginLocal(state.apiEndpoint, state.headers, email, pass)) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isAuthenticating = false,
                            sessionToken = res.data.sessionToken.orEmpty(),
                            authenticatedUserDisplayName = res.data.user.effectiveDisplayName,
                            authError = null,
                        ).validate()
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isAuthenticating = false,
                            authError = res.message ?: "Authentication failed",
                        )
                    }
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun setPlexToken(token: String) {
        _uiState.update { it.copy(plexToken = token) }
    }

    fun startPlexSignIn(onOpenUrl: ((String) -> Unit)? = null) {
        plexAuthJob?.cancel()
        _uiState.update { it.copy(isPlexWaiting = true, authError = null) }
        plexAuthJob = viewModelScope.launch {
            val clientId = plexAuthUseCase.generateClientIdentifier()
            when (val pinRes = plexAuthUseCase.createPin(clientId)) {
                is NetworkResult.Success -> {
                    val pin = pinRes.data
                    val authUrl = plexAuthUseCase.getAuthUrl(clientId, pin.code)
                    _uiState.update { it.copy(plexAuthUrl = authUrl, isPlexWaiting = true) }
                    onOpenUrl?.invoke(authUrl)
                    var authenticated = false
                    val startTime = Clock.System.now().toEpochMilliseconds()
                    while (!authenticated && (Clock.System.now().toEpochMilliseconds() - startTime < 1_800_000)) {
                        delay(2000)
                        when (val checkRes = plexAuthUseCase.checkPin(pin.id, pin.code, clientId)) {
                            is NetworkResult.Success -> {
                                val token = checkRes.data.authToken
                                if (!token.isNullOrBlank()) {
                                    authenticated = true
                                    _uiState.update {
                                        it.copy(
                                            plexToken = token,
                                            plexAuthUrl = null,
                                            isPlexWaiting = false,
                                        )
                                    }
                                    val currentState = _uiState.value
                                    if (currentState.apiEndpoint.isValidUrl()) {
                                        loginPlex(token)
                                    }
                                    break
                                }
                            }
                            is NetworkResult.Error -> break
                            is NetworkResult.Loading -> Unit
                        }
                    }
                    if (!authenticated) {
                        _uiState.update {
                            it.copy(
                                plexAuthUrl = null,
                                isPlexWaiting = false,
                                authError = "Plex sign in expired or cancelled",
                            )
                        }
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isPlexWaiting = false,
                            authError = pinRes.message ?: "Failed to start Plex sign in",
                        )
                    }
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun cancelPlexSignIn() {
        plexAuthJob?.cancel()
        plexAuthJob = null
        _uiState.update { it.copy(plexAuthUrl = null, isPlexWaiting = false) }
    }

    fun loginPlex(token: String) {
        val state = _uiState.value
        if (!state.apiEndpoint.isValidUrl()) {
            _uiState.update { it.copy(endpointError = true, authError = "Invalid server URL") }
            return
        }
        _uiState.update { it.copy(isAuthenticating = true, authError = null) }
        viewModelScope.launch {
            when (val res = seerrAuthUseCase.loginPlex(state.apiEndpoint, state.headers, token)) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isAuthenticating = false,
                            sessionToken = res.data.sessionToken.orEmpty(),
                            authenticatedUserDisplayName = res.data.user.effectiveDisplayName,
                            authError = null,
                        ).validate()
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isAuthenticating = false,
                            authError = res.message ?: "Plex authentication failed",
                        )
                    }
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun loginJellyfin(username: String, pass: String) {
        val state = _uiState.value
        if (!state.apiEndpoint.isValidUrl()) {
            _uiState.update { it.copy(endpointError = true, authError = "Invalid server URL") }
            return
        }
        _uiState.update { it.copy(isAuthenticating = true, authError = null) }
        viewModelScope.launch {
            when (val res = seerrAuthUseCase.loginJellyfin(state.apiEndpoint, state.headers, username, pass)) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isAuthenticating = false,
                            sessionToken = res.data.sessionToken.orEmpty(),
                            authenticatedUserDisplayName = res.data.user.effectiveDisplayName,
                            authError = null,
                        ).validate()
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isAuthenticating = false,
                            authError = res.message ?: "Jellyfin authentication failed",
                        )
                    }
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun startJellyfinQuickConnect() {
        val state = _uiState.value
        if (!state.apiEndpoint.isValidUrl()) {
            _uiState.update { it.copy(endpointError = true, authError = "Invalid server URL") }
            return
        }
        quickConnectJob?.cancel()
        _uiState.update {
            it.copy(
                isAuthenticating = true,
                authError = null,
                quickConnectCode = null,
                quickConnectSecret = null,
            )
        }
        quickConnectJob = viewModelScope.launch {
            when (val initRes = seerrAuthUseCase.initiateJellyfinQuickConnect(state.apiEndpoint, state.headers)) {
                is NetworkResult.Success -> {
                    val secret = initRes.data.secret
                    _uiState.update {
                        it.copy(
                            quickConnectCode = initRes.data.code,
                            quickConnectSecret = secret,
                        )
                    }
                    var authenticated = false
                    val startTime = Clock.System.now().toEpochMilliseconds()
                    while (!authenticated && (Clock.System.now().toEpochMilliseconds() - startTime < 300_000)) {
                        delay(3000)
                        when (val checkRes = seerrAuthUseCase.checkJellyfinQuickConnect(state.apiEndpoint, state.headers, secret)) {
                            is NetworkResult.Success -> {
                                if (checkRes.data.authenticated) {
                                    authenticated = true
                                    break
                                }
                            }
                            is NetworkResult.Error -> break
                            is NetworkResult.Loading -> Unit
                        }
                    }
                    if (authenticated) {
                        when (val authRes = seerrAuthUseCase.authenticateJellyfinQuickConnect(state.apiEndpoint, state.headers, secret)) {
                            is NetworkResult.Success -> {
                                _uiState.update {
                                    it.copy(
                                        isAuthenticating = false,
                                        sessionToken = authRes.data.sessionToken.orEmpty(),
                                        authenticatedUserDisplayName = authRes.data.user.effectiveDisplayName,
                                        quickConnectCode = null,
                                        quickConnectSecret = null,
                                    ).validate()
                                }
                            }
                            is NetworkResult.Error -> {
                                _uiState.update {
                                    it.copy(
                                        isAuthenticating = false,
                                        authError = "Quick Connect authentication failed",
                                        quickConnectCode = null,
                                        quickConnectSecret = null,
                                    )
                                }
                            }
                            is NetworkResult.Loading -> Unit
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isAuthenticating = false,
                                authError = "Quick Connect authorization expired or cancelled",
                                quickConnectCode = null,
                                quickConnectSecret = null,
                            )
                        }
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isAuthenticating = false,
                            authError = "Failed to initiate Quick Connect",
                        )
                    }
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun cancelJellyfinQuickConnect() {
        quickConnectJob?.cancel()
        quickConnectJob = null
        _uiState.update {
            it.copy(
                isAuthenticating = false,
                quickConnectCode = null,
                quickConnectSecret = null,
            )
        }
    }

    fun clearAuthSession() {
        cancelJellyfinQuickConnect()
        cancelPlexSignIn()
        _uiState.update {
            it.copy(
                sessionToken = "",
                authenticatedUserDisplayName = null,
                authError = null,
                plexToken = "",
            ).validate()
        }
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
                    state.sessionToken,
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
                    state.sessionToken,
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
                    s.sessionToken,
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
                    seerrAuthType = s.seerrAuthType,
                    sessionToken = EncryptedString(s.sessionToken),
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
}
