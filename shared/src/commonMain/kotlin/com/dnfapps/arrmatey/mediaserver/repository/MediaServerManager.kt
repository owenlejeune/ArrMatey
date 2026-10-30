package com.dnfapps.arrmatey.mediaserver.repository

import com.dnfapps.arrmatey.arr.api.client.HttpClientFactory
import com.dnfapps.arrmatey.mediaserver.client.EmbyClient
import com.dnfapps.arrmatey.mediaserver.client.JellyfinClient
import com.dnfapps.arrmatey.mediaserver.client.PlexClient
import com.dnfapps.arrmatey.mediaserver.database.MediaServerIntegrationDao
import com.dnfapps.arrmatey.mediaserver.model.MediaServerIntegration
import com.dnfapps.arrmatey.mediaserver.model.MediaServerType
import com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaItem
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MediaServerManager(
    private val dao: MediaServerIntegrationDao,
    private val httpClientFactory: HttpClientFactory,
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _repositories = MutableStateFlow<Map<MediaServerType, MediaServerRepository>>(emptyMap())
    val activeRepositories: StateFlow<List<MediaServerRepository>> =
        _repositories.map { it.values.toList() }
            .stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val cachedIntegrations = mutableMapOf<MediaServerType, MediaServerIntegration>()

    init {
        scope.launch {
            dao.observeAll().collect { integrations ->
                updateRepositories(integrations)
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

    fun getRepository(type: MediaServerType): MediaServerRepository? = _repositories.value[type]

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

    private fun createRepository(integration: MediaServerIntegration): MediaServerRepository {
        val httpClient = httpClientFactory.createMediaServerClient(integration)
        return when (integration.serverType) {
            MediaServerType.Plex -> PlexRepository(PlexClient(integration, httpClient))
            MediaServerType.Jellyfin -> JellyfinRepository(JellyfinClient(integration, httpClient))
            MediaServerType.Emby -> EmbyRepository(EmbyClient(integration, httpClient))
        }
    }
}
