package com.dnfapps.arrmatey.mediaserver.repository

import com.dnfapps.arrmatey.arr.api.client.HttpClientFactory
import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.instances.model.InstanceHeader
import com.dnfapps.arrmatey.mediaserver.client.EmbyClient
import com.dnfapps.arrmatey.mediaserver.client.JellyfinClient
import com.dnfapps.arrmatey.mediaserver.client.PlexClient
import com.dnfapps.arrmatey.mediaserver.database.MediaServerIntegrationDao
import com.dnfapps.arrmatey.mediaserver.model.MediaBrowserAuthRequest
import com.dnfapps.arrmatey.mediaserver.model.MediaBrowserAuthResult
import com.dnfapps.arrmatey.mediaserver.model.MediaServerIntegration
import com.dnfapps.arrmatey.mediaserver.model.MediaServerType
import com.dnfapps.arrmatey.mediaserver.model.MediaType
import com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaItem
import com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaLibrary
import com.dnfapps.networking.NetworkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MediaServerManager(
    private val dao: MediaServerIntegrationDao,
    private val httpClientFactory: HttpClientFactory,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
) {

    private val _repositories = MutableStateFlow<Map<MediaServerType, MediaServerRepository>>(emptyMap())
    val activeRepositories: StateFlow<List<MediaServerRepository>> =
        _repositories.map { it.values.toList() }
            .stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val _allMediaServerItems = MutableStateFlow<List<UnifiedMediaItem>>(emptyList())
    val allMediaServerItems: StateFlow<List<UnifiedMediaItem>> = _allMediaServerItems.asStateFlow()

    private val cachedIntegrations = mutableMapOf<MediaServerType, MediaServerIntegration>()

    init {
        scope.launch {
            dao.observeAll().collect { integrations ->
                updateRepositories(integrations)
            }
        }
        scope.launch {
            activeRepositories.collect { repos ->
                if (repos.isNotEmpty()) {
                    refreshAllMediaServerItems()
                } else {
                    _allMediaServerItems.value = emptyList()
                }
            }
        }
    }

    private fun updateRepositories(integrations: List<MediaServerIntegration>) {
        val current = _repositories.value.toMutableMap()
        val incomingTypes = integrations.filter { it.enabled }.mapTo(mutableSetOf()) { it.serverType }

        (current.keys - incomingTypes).forEach { type ->
            current.remove(type)
            cachedIntegrations.remove(type)
        }

        integrations.filter { it.enabled }.forEach { integration ->
            val cached = cachedIntegrations[integration.serverType]
            if (cached == null || cached != integration) {
                current[integration.serverType] = createRepository(integration)
                cachedIntegrations[integration.serverType] = integration
            }
        }

        _repositories.value = current
    }

    fun getRepository(type: MediaServerType): MediaServerRepository? = _repositories.value[type] ?: cachedIntegrations[type]?.let { integration ->
        val repo = createRepository(integration)
        _repositories.value += (type to repo)
        repo
    }

    fun getOrCreateRepository(integration: MediaServerIntegration): MediaServerRepository {
        val existing = _repositories.value[integration.serverType]
        if (existing != null && cachedIntegrations[integration.serverType] == integration) {
            return existing
        }
        val repo = createRepository(integration)
        cachedIntegrations[integration.serverType] = integration
        _repositories.value += (integration.serverType to repo)
        return repo
    }

    fun observeIntegrations(): Flow<List<MediaServerIntegration>> = dao.observeAll()

    fun observeIntegration(type: MediaServerType): Flow<MediaServerIntegration?> = dao.observeByType(type)

    suspend fun getIntegration(type: MediaServerType): MediaServerIntegration? = dao.getByType(type)

    suspend fun saveIntegration(integration: MediaServerIntegration): Long {
        val id = dao.insert(integration)
        val updated = integration.copy(id = id)
        cachedIntegrations[integration.serverType] = updated
        _repositories.value += (integration.serverType to createRepository(updated))
        return id
    }

    suspend fun updateIntegrationUrl(serverType: MediaServerType, newUrl: String) {
        val existing = getIntegration(serverType) ?: return
        if (existing.url != newUrl) {
            val updated = existing.copy(url = newUrl)
            dao.insert(updated)
            cachedIntegrations[serverType] = updated
        }
    }

    suspend fun deleteIntegration(type: MediaServerType) {
        dao.deleteByType(type)
        cachedIntegrations.remove(type)
        _repositories.value -= type
    }

    suspend fun deleteIntegration(integration: MediaServerIntegration) {
        dao.delete(integration)
        cachedIntegrations.remove(integration.serverType)
        _repositories.value -= integration.serverType
    }

    suspend fun getContinueWatching(limit: Int = 20): List<UnifiedMediaItem> = coroutineScope {
        val repos = activeRepositories.value
        if (repos.isEmpty()) return@coroutineScope emptyList()

        val results = repos.map { repo ->
            async {
                when (val res = repo.getContinueWatching(limit)) {
                    is NetworkResult.Success -> res.data
                    else -> emptyList()
                }
            }
        }.awaitAll().flatten()

        results.sortedByDescending { it.lastPlayedDate ?: 0L }.take(limit)
    }

    suspend fun searchAll(query: String, limit: Int = 30): List<UnifiedMediaItem> = coroutineScope {
        val repos = activeRepositories.value
        if (repos.isEmpty()) return@coroutineScope emptyList()

        repos.map { repo ->
            async {
                when (val res = repo.search(query, limit)) {
                    is NetworkResult.Success -> res.data
                    else -> emptyList()
                }
            }
        }.awaitAll().flatten()
    }

    suspend fun refreshAllMediaServerItems(): List<UnifiedMediaItem> = coroutineScope {
        val repos = activeRepositories.value
        if (repos.isEmpty()) {
            _allMediaServerItems.value = emptyList()
            return@coroutineScope emptyList()
        }

        val allItems = repos.map { repo ->
            async {
                when (val libRes = repo.getLibraries()) {
                    is NetworkResult.Success -> {
                        libRes.data.map { lib ->
                            async {
                                when (val itemsRes = repo.getAllLibraryContents(lib.id)) {
                                    is NetworkResult.Success -> itemsRes.data
                                    else -> emptyList()
                                }
                            }
                        }.awaitAll().flatten()
                    }
                    else -> emptyList()
                }
            }
        }.awaitAll().flatten()

        _allMediaServerItems.value = allItems
        allItems
    }

    fun findMatchingItem(
        items: List<UnifiedMediaItem> = _allMediaServerItems.value,
        tmdbId: Long? = null,
        tvdbId: Long? = null,
        title: String? = null,
        year: Int? = null,
        mediaType: MediaType? = null,
    ): UnifiedMediaItem? {
        if (items.isEmpty()) return null

        if (tmdbId != null && tmdbId > 0) {
            val tmdbMatch = items.firstOrNull { it.tmdbId == tmdbId }
            if (tmdbMatch != null) return tmdbMatch
        }

        if (tvdbId != null && tvdbId > 0) {
            val tvdbMatch = items.firstOrNull { it.tvdbId == tvdbId }
            if (tvdbMatch != null) return tvdbMatch
        }

        if (!title.isNullOrBlank()) {
            val cleanTitle = title.replace(Regex("[^a-zA-Z0-9]"), "").lowercase()
            return items.firstOrNull { item ->
                val itemCleanTitle = item.title.replace(Regex("[^a-zA-Z0-9]"), "").lowercase()
                val titleMatches = itemCleanTitle == cleanTitle
                val typeMatches = mediaType == null || item.type == mediaType
                val yearMatches = year == null || item.year == null || item.year == year
                titleMatches && typeMatches && yearMatches
            }
        }

        return null
    }

    suspend fun testConnection(integration: MediaServerIntegration): NetworkResult<List<UnifiedMediaLibrary>> = try {
        val repo = createRepository(integration)
        repo.getLibraries()
    } catch (e: Exception) {
        NetworkResult.Error(message = e.message ?: "Connection test failed", cause = e)
    }

    suspend fun authenticateMediaBrowser(
        serverType: MediaServerType,
        baseUrl: String,
        username: String,
        password: String,
        clientIdentifier: String,
        headers: List<InstanceHeader> = emptyList(),
    ): NetworkResult<MediaBrowserAuthResult> {
        val tempIntegration = MediaServerIntegration(
            serverType = serverType,
            label = serverType.name,
            url = baseUrl,
            token = EncryptedString(""),
            clientIdentifier = clientIdentifier,
            headers = headers,
        )
        val httpClient = httpClientFactory.createMediaServerClient(tempIntegration)
        return when (serverType) {
            MediaServerType.Jellyfin -> JellyfinClient(tempIntegration, httpClient).authenticate(MediaBrowserAuthRequest(username, password))
            MediaServerType.Emby -> EmbyClient(tempIntegration, httpClient).authenticate(MediaBrowserAuthRequest(username, password))
            else -> NetworkResult.Error(message = "Unsupported server type for credentials authentication")
        }
    }

    private fun createRepository(integration: MediaServerIntegration): MediaServerRepository {
        val httpClient = httpClientFactory.createMediaServerClient(integration)
        return when (integration.serverType) {
            MediaServerType.Plex -> PlexRepository(
                client = PlexClient(integration, httpClient),
                onUrlUpdated = { newUrl -> updateIntegrationUrl(integration.serverType, newUrl) },
            )
            MediaServerType.Jellyfin -> JellyfinRepository(JellyfinClient(integration, httpClient))
            MediaServerType.Emby -> EmbyRepository(EmbyClient(integration, httpClient))
        }
    }
}
