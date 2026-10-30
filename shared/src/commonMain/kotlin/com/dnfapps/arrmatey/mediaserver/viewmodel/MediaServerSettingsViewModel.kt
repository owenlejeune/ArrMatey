package com.dnfapps.arrmatey.mediaserver.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.instances.model.InstanceHeader
import com.dnfapps.arrmatey.instances.usecase.PlexAuthUseCase
import com.dnfapps.arrmatey.mediaserver.model.MediaServerIntegration
import com.dnfapps.arrmatey.mediaserver.model.MediaServerType
import com.dnfapps.arrmatey.mediaserver.model.PlexServerResource
import com.dnfapps.arrmatey.mediaserver.repository.MediaServerManager
import com.dnfapps.arrmatey.mediaserver.usecase.JellyfinQuickConnectUseCase
import com.dnfapps.networking.NetworkResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Clock

enum class MediaServerAuthMode {
    OAuthOrQuickConnect,
    UsernamePassword,
    ManualToken,
}

data class MediaServerConfigState(
    val serverType: MediaServerType,
    val existingId: Long = 0,
    val label: String = "",
    val url: String = "",
    val token: String = "",
    val userId: String = "",
    val username: String = "",
    val password: String = "",
    val clientIdentifier: String = "",
    val serverId: String = "",
    val enabled: Boolean = true,
    val localNetworkEnabled: Boolean = false,
    val localNetworkSsids: List<String> = emptyList(),
    val localNetworkEndpoint: String = "",
    val headers: List<InstanceHeader> = emptyList(),
    val authMode: MediaServerAuthMode = MediaServerAuthMode.OAuthOrQuickConnect,
    val isPlexWaiting: Boolean = false,
    val plexAuthUrl: String? = null,
    val discoveredPlexServers: List<PlexServerResource> = emptyList(),
    val quickConnectCode: String? = null,
    val quickConnectSecret: String? = null,
    val isQuickConnectWaiting: Boolean = false,
    val isTesting: Boolean = false,
    val isSaving: Boolean = false,
    val testSuccess: Boolean? = null,
    val errorMessage: String? = null,
    val saveCompleted: Boolean = false,
)

class MediaServerSettingsViewModel(
    private val mediaServerManager: MediaServerManager,
    private val plexAuthUseCase: PlexAuthUseCase,
    private val jellyfinQuickConnectUseCase: JellyfinQuickConnectUseCase,
) : ViewModel() {

    val configuredServers: StateFlow<Map<MediaServerType, MediaServerIntegration>> =
        mediaServerManager.observeIntegrations()
            .map { list -> list.associateBy { it.serverType } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _configState = MutableStateFlow<MediaServerConfigState?>(null)
    val configState: StateFlow<MediaServerConfigState?> = _configState.asStateFlow()

    private var plexJob: Job? = null
    private var quickConnectJob: Job? = null

    fun openConfiguration(serverType: MediaServerType) {
        viewModelScope.launch {
            val existing = mediaServerManager.getIntegration(serverType)
            if (existing != null) {
                _configState.value = MediaServerConfigState(
                    serverType = serverType,
                    existingId = existing.id,
                    label = existing.label,
                    url = existing.url,
                    token = existing.token.value,
                    userId = existing.userId ?: "",
                    username = existing.username ?: "",
                    clientIdentifier = existing.clientIdentifier,
                    serverId = existing.serverId ?: "",
                    enabled = existing.enabled,
                    localNetworkEnabled = existing.localNetworkEnabled,
                    localNetworkSsids = existing.localNetworkSsids,
                    localNetworkEndpoint = existing.localNetworkEndpoint ?: "",
                    headers = existing.headers,
                    authMode = when (serverType) {
                        MediaServerType.Plex -> MediaServerAuthMode.OAuthOrQuickConnect
                        MediaServerType.Jellyfin -> if (existing.token.value.isNotBlank() && existing.username.isNullOrBlank()) {
                            MediaServerAuthMode.ManualToken
                        } else {
                            MediaServerAuthMode.OAuthOrQuickConnect
                        }
                        MediaServerType.Emby -> if (existing.username.isNullOrBlank() && existing.token.value.isNotBlank()) {
                            MediaServerAuthMode.ManualToken
                        } else {
                            MediaServerAuthMode.UsernamePassword
                        }
                    },
                )
            } else {
                _configState.value = MediaServerConfigState(
                    serverType = serverType,
                    existingId = 0,
                    label = serverType.name,
                    url = when (serverType) {
                        MediaServerType.Plex -> "http://localhost:32400"
                        MediaServerType.Jellyfin -> "http://localhost:8096"
                        MediaServerType.Emby -> "http://localhost:8096"
                    },
                    clientIdentifier = plexAuthUseCase.generateClientIdentifier(),
                    authMode = when (serverType) {
                        MediaServerType.Plex -> MediaServerAuthMode.OAuthOrQuickConnect
                        MediaServerType.Jellyfin -> MediaServerAuthMode.OAuthOrQuickConnect
                        MediaServerType.Emby -> MediaServerAuthMode.UsernamePassword
                    },
                )
            }
        }
    }

    fun closeConfiguration() {
        cancelPlexSignIn()
        cancelQuickConnect()
        _configState.value = null
    }

    fun updateLabel(label: String) {
        _configState.update { it?.copy(label = label) }
    }

    fun updateUrl(url: String) {
        _configState.update { it?.copy(url = url, testSuccess = null, errorMessage = null) }
    }

    fun updateToken(token: String) {
        _configState.update { it?.copy(token = token, testSuccess = null, errorMessage = null) }
    }

    fun updateUsername(username: String) {
        _configState.update { it?.copy(username = username, testSuccess = null, errorMessage = null) }
    }

    fun updatePassword(password: String) {
        _configState.update { it?.copy(password = password, testSuccess = null, errorMessage = null) }
    }

    fun updateAuthMode(mode: MediaServerAuthMode) {
        _configState.update { it?.copy(authMode = mode, errorMessage = null, testSuccess = null) }
    }

    fun updateEnabled(enabled: Boolean) {
        _configState.update { it?.copy(enabled = enabled) }
    }

    fun updateLocalNetworkEnabled(enabled: Boolean) {
        _configState.update { it?.copy(localNetworkEnabled = enabled) }
    }

    fun updateLocalNetworkEndpoint(endpoint: String) {
        _configState.update { it?.copy(localNetworkEndpoint = endpoint) }
    }

    fun updateLocalNetworkSsids(ssids: List<String>) {
        _configState.update { it?.copy(localNetworkSsids = ssids) }
    }

    fun updateHeaders(headers: List<InstanceHeader>) {
        _configState.update { it?.copy(headers = headers) }
    }

    fun selectDiscoveredPlexServer(server: PlexServerResource) {
        val bestConnection = server.connections.firstOrNull { it.local } ?: server.connections.firstOrNull()
        _configState.update {
            it?.copy(
                label = server.name.ifBlank { it.label },
                serverId = server.clientIdentifier,
                url = bestConnection?.uri ?: it.url,
                testSuccess = null,
                errorMessage = null,
            )
        }
    }

    fun startPlexSignIn(onOpenUrl: (String) -> Unit) {
        val state = _configState.value ?: return
        cancelPlexSignIn()
        _configState.update { it?.copy(isPlexWaiting = true, errorMessage = null) }

        plexJob = viewModelScope.launch {
            when (val pinRes = plexAuthUseCase.createPin(state.clientIdentifier)) {
                is NetworkResult.Success -> {
                    val pin = pinRes.data
                    val authUrl = plexAuthUseCase.getAuthUrl(state.clientIdentifier, pin.code)
                    _configState.update { it?.copy(plexAuthUrl = authUrl, isPlexWaiting = true) }
                    onOpenUrl(authUrl)

                    var acquiredToken: String? = null
                    val expiresInSeconds = pin.expiresIn ?: 300L
                    val startTime = Clock.System.now().toEpochMilliseconds()
                    while (Clock.System.now().toEpochMilliseconds() - startTime < (expiresInSeconds * 1000L) && isActive) {
                        delay(2000)
                        when (val checkRes = plexAuthUseCase.checkPin(pin.id, pin.code, state.clientIdentifier)) {
                            is NetworkResult.Success -> {
                                val token = checkRes.data.authToken
                                if (!token.isNullOrBlank()) {
                                    acquiredToken = token
                                    break
                                }
                            }
                            else -> {
                                // continue polling
                            }
                        }
                    }

                    if (acquiredToken != null) {
                        _configState.update {
                            it?.copy(
                                token = acquiredToken,
                                isPlexWaiting = false,
                            )
                        }
                        when (val serversRes = plexAuthUseCase.fetchPlexServers(acquiredToken, state.clientIdentifier)) {
                            is NetworkResult.Success -> {
                                val servers = serversRes.data.filter { it.provides.contains("server") }
                                _configState.update { it?.copy(discoveredPlexServers = servers) }
                                if (servers.size == 1) {
                                    selectDiscoveredPlexServer(servers.first())
                                }
                            }
                            else -> {
                                // servers fetch failed, manual URL entry allowed
                            }
                        }
                    } else {
                        _configState.update {
                            it?.copy(
                                isPlexWaiting = false,
                                errorMessage = "Plex sign in expired or was cancelled",
                            )
                        }
                    }
                }
                is NetworkResult.Error -> {
                    _configState.update {
                        it?.copy(
                            isPlexWaiting = false,
                            errorMessage = pinRes.message ?: "Failed to initiate Plex sign in",
                        )
                    }
                }
                else -> {}
            }
        }
    }

    fun cancelPlexSignIn() {
        plexJob?.cancel()
        plexJob = null
        _configState.update { it?.copy(isPlexWaiting = false, plexAuthUrl = null) }
    }

    fun startJellyfinQuickConnect() {
        val state = _configState.value ?: return
        if (state.url.isBlank()) {
            _configState.update { it?.copy(errorMessage = "Server URL is required for Quick Connect") }
            return
        }
        cancelQuickConnect()
        _configState.update { it?.copy(isQuickConnectWaiting = true, errorMessage = null) }

        quickConnectJob = viewModelScope.launch {
            when (val initRes = jellyfinQuickConnectUseCase.initiate(state.url, state.clientIdentifier, state.headers)) {
                is NetworkResult.Success -> {
                    val code = initRes.data.code
                    val secret = initRes.data.secret
                    _configState.update {
                        it?.copy(
                            quickConnectCode = code,
                            quickConnectSecret = secret,
                            isQuickConnectWaiting = true,
                        )
                    }

                    var authenticated = false
                    val startTime = Clock.System.now().toEpochMilliseconds()
                    while (Clock.System.now().toEpochMilliseconds() - startTime < 300_000L && isActive) {
                        delay(3000)
                        when (val checkRes = jellyfinQuickConnectUseCase.check(state.url, secret, state.clientIdentifier, state.headers)) {
                            is NetworkResult.Success -> {
                                if (checkRes.data.authenticated) {
                                    authenticated = true
                                    break
                                }
                            }
                            else -> {
                                // continue polling
                            }
                        }
                    }

                    if (authenticated) {
                        when (val authRes = jellyfinQuickConnectUseCase.authenticate(state.url, secret, state.clientIdentifier, state.headers)) {
                            is NetworkResult.Success -> {
                                val auth = authRes.data
                                _configState.update {
                                    it?.copy(
                                        token = auth.accessToken,
                                        userId = auth.user.id,
                                        username = auth.user.name,
                                        serverId = auth.serverId ?: it.serverId,
                                        isQuickConnectWaiting = false,
                                        quickConnectCode = null,
                                        quickConnectSecret = null,
                                    )
                                }
                                testConnection()
                            }
                            is NetworkResult.Error -> {
                                _configState.update {
                                    it?.copy(
                                        isQuickConnectWaiting = false,
                                        errorMessage = authRes.message ?: "Authentication failed",
                                    )
                                }
                            }
                            else -> {}
                        }
                    } else {
                        _configState.update {
                            it?.copy(
                                isQuickConnectWaiting = false,
                                errorMessage = "Quick Connect timed out or was cancelled",
                            )
                        }
                    }
                }
                is NetworkResult.Error -> {
                    _configState.update {
                        it?.copy(
                            isQuickConnectWaiting = false,
                            errorMessage = initRes.message ?: "Failed to initiate Quick Connect",
                        )
                    }
                }
                else -> {}
            }
        }
    }

    fun cancelQuickConnect() {
        quickConnectJob?.cancel()
        quickConnectJob = null
        _configState.update {
            it?.copy(
                isQuickConnectWaiting = false,
                quickConnectCode = null,
                quickConnectSecret = null,
            )
        }
    }

    fun authenticateCredentials() {
        val state = _configState.value ?: return
        if (state.url.isBlank() || state.username.isBlank() || state.password.isBlank()) {
            _configState.update { it?.copy(errorMessage = "Server URL, username, and password are required") }
            return
        }
        _configState.update { it?.copy(isTesting = true, errorMessage = null, testSuccess = null) }

        viewModelScope.launch {
            when (val res = mediaServerManager.authenticateMediaBrowser(
                serverType = state.serverType,
                baseUrl = state.url,
                username = state.username,
                password = state.password,
                clientIdentifier = state.clientIdentifier,
                headers = state.headers,
            )) {
                is NetworkResult.Success -> {
                    val auth = res.data
                    _configState.update {
                        it?.copy(
                            token = auth.accessToken,
                            userId = auth.user.id,
                            username = auth.user.name,
                            serverId = auth.serverId ?: it.serverId,
                            isTesting = false,
                            testSuccess = true,
                            errorMessage = null,
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _configState.update {
                        it?.copy(
                            isTesting = false,
                            testSuccess = false,
                            errorMessage = res.message ?: "Authentication failed",
                        )
                    }
                }
                else -> {}
            }
        }
    }

    fun testConnection() {
        val state = _configState.value ?: return
        if (state.url.isBlank() || state.token.isBlank()) {
            _configState.update { it?.copy(errorMessage = "Server URL and token are required") }
            return
        }
        _configState.update { it?.copy(isTesting = true, errorMessage = null, testSuccess = null) }

        viewModelScope.launch {
            val candidate = MediaServerIntegration(
                id = state.existingId,
                serverType = state.serverType,
                label = state.label.ifBlank { state.serverType.name },
                url = state.url,
                token = EncryptedString(state.token),
                userId = state.userId.takeIf { it.isNotBlank() },
                username = state.username.takeIf { it.isNotBlank() },
                clientIdentifier = state.clientIdentifier,
                serverId = state.serverId.takeIf { it.isNotBlank() },
                enabled = state.enabled,
                localNetworkEnabled = state.localNetworkEnabled,
                localNetworkSsids = state.localNetworkSsids,
                localNetworkEndpoint = state.localNetworkEndpoint.takeIf { it.isNotBlank() },
                headers = state.headers,
            )

            when (val res = mediaServerManager.testConnection(candidate)) {
                is NetworkResult.Success -> {
                    _configState.update {
                        it?.copy(
                            isTesting = false,
                            testSuccess = true,
                            errorMessage = null,
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _configState.update {
                        it?.copy(
                            isTesting = false,
                            testSuccess = false,
                            errorMessage = res.message ?: "Connection test failed",
                        )
                    }
                }
                else -> {}
            }
        }
    }

    fun saveConfiguration() {
        val state = _configState.value ?: return
        if (state.url.isBlank() || state.token.isBlank()) {
            _configState.update { it?.copy(errorMessage = "Server URL and token are required") }
            return
        }
        _configState.update { it?.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val integration = MediaServerIntegration(
                    id = state.existingId,
                    serverType = state.serverType,
                    label = state.label.ifBlank { state.serverType.name },
                    url = state.url,
                    token = EncryptedString(state.token),
                    userId = state.userId.takeIf { it.isNotBlank() },
                    username = state.username.takeIf { it.isNotBlank() },
                    clientIdentifier = state.clientIdentifier,
                    serverId = state.serverId.takeIf { it.isNotBlank() },
                    enabled = state.enabled,
                    localNetworkEnabled = state.localNetworkEnabled,
                    localNetworkSsids = state.localNetworkSsids,
                    localNetworkEndpoint = state.localNetworkEndpoint.takeIf { it.isNotBlank() },
                    headers = state.headers,
                )
                mediaServerManager.saveIntegration(integration)
                _configState.update { it?.copy(isSaving = false, saveCompleted = true) }
            } catch (e: Exception) {
                _configState.update { it?.copy(isSaving = false, errorMessage = e.message ?: "Failed to save") }
            }
        }
    }

    fun disconnectServer(serverType: MediaServerType) {
        viewModelScope.launch {
            mediaServerManager.deleteIntegration(serverType)
            if (_configState.value?.serverType == serverType) {
                closeConfiguration()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        cancelPlexSignIn()
        cancelQuickConnect()
    }
}
