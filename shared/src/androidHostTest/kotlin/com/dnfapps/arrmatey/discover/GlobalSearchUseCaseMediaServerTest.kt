package com.dnfapps.arrmatey.discover

import com.dnfapps.arrmatey.arr.api.client.HttpClientFactory
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.Language
import com.dnfapps.arrmatey.arr.api.model.MediaStatus
import com.dnfapps.arrmatey.database.CredentialMigrationUseCase
import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.database.dao.InstanceDao
import com.dnfapps.arrmatey.datastore.DataStoreFactory
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.discover.model.SearchFilter
import com.dnfapps.arrmatey.discover.model.SearchResult
import com.dnfapps.arrmatey.discover.usecase.GlobalSearchUseCase
import com.dnfapps.arrmatey.downloadclient.database.DownloadClientDao
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.mediaserver.model.MediaServerIntegration
import com.dnfapps.arrmatey.mediaserver.model.MediaServerType
import com.dnfapps.arrmatey.mediaserver.model.MediaType
import com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaItem
import com.dnfapps.arrmatey.mediaserver.repository.MediaServerManager
import com.dnfapps.arrmatey.mediaserver.repository.MediaServerRepository
import com.dnfapps.arrmatey.utils.fakeMokoStrings
import com.dnfapps.arrmatey.utils.testLogger
import com.dnfapps.networking.NetworkResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logger
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class GlobalSearchUseCaseMediaServerTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val logger = testLogger()
    private val managers = mutableListOf<InstanceManager>()

    @AfterTest
    fun cleanupManagers() {
        managers.forEach { it.cleanup() }
        managers.clear()
    }

    private class FakeInstanceDao(
        initialInstances: List<Instance> = emptyList(),
    ) : InstanceDao {
        val instances = MutableStateFlow(initialInstances)

        override suspend fun insert(instance: Instance): Long = 0L
        override suspend fun delete(instance: Instance) {}
        override suspend fun update(instance: Instance): Int = 0
        override suspend fun updateAll(instances: List<Instance>) {}
        override fun observeAllInstances(): Flow<List<Instance>> = instances
        override fun observeInstancesByType(type: InstanceType): Flow<List<Instance>> = MutableStateFlow(instances.value.filter { it.type == type })
        override fun observeInstancesByTypes(types: List<InstanceType>): Flow<List<Instance>> = MutableStateFlow(instances.value.filter { it.type in types })
        override suspend fun getAllInstances(): List<Instance> = instances.value
        override suspend fun getInstanceById(id: Long): Instance? = instances.value.find { it.id == id }
        override fun observeSelectedInstance(type: InstanceType): Flow<Instance?> = MutableStateFlow(instances.value.find { it.type == type && it.selected })
        override fun observeSelectedInstanceByTypes(types: List<InstanceType>): Flow<Instance?> = MutableStateFlow(instances.value.find { it.type in types && it.selected })
        override suspend fun getInstancesOfType(type: InstanceType): List<Instance> = instances.value.filter { it.type == type }
        override suspend fun getInstancesOfTypes(types: List<InstanceType>): List<Instance> = instances.value.filter { it.type in types }
        override suspend fun unselectAllOf(type: InstanceType) {}
        override suspend fun unselectAllOfTypes(types: List<InstanceType>) {}
        override suspend fun selectInstance(id: Long) {}
        override suspend fun findByUrl(url: String): Long? = null
        override suspend fun findByLabel(label: String): Long? = null
        override suspend fun findOtherByUrl(url: String, currentId: Long): Long? = null
        override suspend fun findOtherByLabel(label: String, currentId: Long): Long? = null
        override suspend fun ensureFirstSelectedIfNone(type: InstanceType) {}
        override suspend fun ensureFirstSelectedIfNone(types: List<InstanceType>) {}
    }

    private class FakeDownloadClientDao : DownloadClientDao {
        private val clients = MutableStateFlow<List<DownloadClient>>(emptyList())
        override fun observeAllDownloadClients(): Flow<List<DownloadClient>> = clients
        override fun observeSelectedDownloadClient(): Flow<DownloadClient?> = MutableStateFlow(clients.value.firstOrNull { it.selected })
        override suspend fun getDownloadClientById(id: Long): DownloadClient? = clients.value.firstOrNull { it.id == id }
        override suspend fun getAllDownloadClients(): List<DownloadClient> = clients.value
        override suspend fun insert(downloadClient: DownloadClient): Long = 0L
        override suspend fun delete(downloadClient: DownloadClient) {}
        override suspend fun update(downloadClient: DownloadClient): Int = 0
        override suspend fun updateAll(downloadClients: List<DownloadClient>) {}
        override suspend fun findByUrl(url: String): Long? = null
        override suspend fun findByLabel(label: String): Long? = null
        override suspend fun findOtherByUrl(url: String, currentId: Long): Long? = null
        override suspend fun findOtherByLabel(label: String, currentId: Long): Long? = null
        override suspend fun unselectAll() = Unit
        override suspend fun selectDownloadClient(id: Long) = Unit
        override suspend fun ensureFirstSelectedIfNone() = Unit
    }

    private val silentLogger = object : Logger {
        override fun log(message: String) = Unit
    }

    private inner class MockHttpClientFactory(
        private val json: Json,
        private val handler: (Instance) -> MockEngine,
    ) : HttpClientFactory(json, silentLogger) {
        override fun create(instance: Instance): HttpClient {
            val engine = handler(instance)
            return HttpClient(engine) {
                install(ContentNegotiation) {
                    json(json)
                }
            }
        }
    }

    private val fakeMigrationUseCase = object : CredentialMigrationUseCase(
        FakeInstanceDao(),
        FakeDownloadClientDao(),
        PreferencesStore(DataStoreFactory()),
    ) {
        override suspend fun invoke() {}
    }

    private fun createMockMediaServerRepo(
        serverType: MediaServerType,
        serverLabel: String,
        items: List<UnifiedMediaItem>,
    ): MediaServerRepository {
        val repo = mockk<MediaServerRepository>()
        val integration = MediaServerIntegration(
            id = 1L,
            serverType = serverType,
            label = serverLabel,
            serverId = "srv-1",
            url = "http://localhost:32400",
            token = EncryptedString("token"),
            clientIdentifier = "client-1",
            enabled = true,
        )
        every { repo.serverType } returns serverType
        every { repo.integration } returns integration
        coEvery { repo.search(any(), any()) } returns NetworkResult.Success(items)
        return repo
    }

    private fun createMockMediaServerManager(repos: List<MediaServerRepository>): MediaServerManager {
        val manager = mockk<MediaServerManager>()
        every { manager.activeRepositories } returns MutableStateFlow(repos)
        every { manager.findMatchingItem(any(), any(), any(), any(), any(), any()) } returns null
        return manager
    }

    @Test
    fun testMediaServerResultsIncludedInSearch() = runBlocking {
        val fakeDao = FakeInstanceDao(emptyList())
        val instanceRepo = InstanceRepository(fakeDao)
        val mockFactory = MockHttpClientFactory(json) { MockEngine { respond("[]", HttpStatusCode.OK) } }
        val instanceManager = InstanceManager(instanceRepo, mockFactory, fakeMigrationUseCase, logger, fakeMokoStrings).also(managers::add)

        val plexItem = UnifiedMediaItem(
            id = "plex-101",
            serverType = MediaServerType.Plex,
            serverId = "srv-1",
            title = "Inception",
            type = MediaType.MOVIE,
            year = 2010,
            tmdbId = 27205L,
        )
        val mockRepo = createMockMediaServerRepo(MediaServerType.Plex, "My Plex", listOf(plexItem))
        val mockMediaServerManager = createMockMediaServerManager(listOf(mockRepo))

        val useCase = GlobalSearchUseCase(instanceManager, mockMediaServerManager)
        val emissions = useCase("Inception").toList()

        assertTrue(emissions.isNotEmpty())
        val finalResults = emissions.last()
        assertEquals(1, finalResults.size)
        val mediaServerResult = finalResults.first() as SearchResult.MediaServerResult
        assertEquals("Inception", mediaServerResult.title)
        assertEquals(MediaServerType.Plex, mediaServerResult.serverType)
        assertEquals("My Plex", mediaServerResult.serverLabel)
        assertEquals(27205L, mediaServerResult.item.tmdbId)
        assertNull(mediaServerResult.instanceType)
        assertEquals(SearchFilter.MediaServer(MediaServerType.Plex), mediaServerResult.searchFilter)
    }

    @Test
    fun testMediaServerSearchFilterMatchingMovieAndEpisodeWithoutRadarr() = runBlocking {
        val fakeDao = FakeInstanceDao(emptyList()) // No Radarr or Sonarr configured
        val instanceRepo = InstanceRepository(fakeDao)
        val mockFactory = MockHttpClientFactory(json) { MockEngine { respond("[]", HttpStatusCode.OK) } }
        val instanceManager = InstanceManager(instanceRepo, mockFactory, fakeMigrationUseCase, logger, fakeMokoStrings).also(managers::add)

        val plexMovie = UnifiedMediaItem(
            id = "plex-m1",
            serverType = MediaServerType.Plex,
            serverId = "srv-1",
            title = "Inception",
            type = MediaType.MOVIE,
            year = 2010,
            tmdbId = 27205L,
        )
        val plexEpisode = UnifiedMediaItem(
            id = "plex-e1",
            serverType = MediaServerType.Plex,
            serverId = "srv-1",
            title = "Pilot",
            type = MediaType.EPISODE,
            seriesTitle = "Breaking Bad",
            seasonNumber = 1,
            episodeNumber = 1,
            tvdbId = 81189L,
        )
        val mockRepo = createMockMediaServerRepo(MediaServerType.Plex, "Home Plex", listOf(plexMovie, plexEpisode))
        val mockMediaServerManager = createMockMediaServerManager(listOf(mockRepo))

        val useCase = GlobalSearchUseCase(instanceManager, mockMediaServerManager)
        val results = useCase("test").toList().last()

        assertEquals(2, results.size)
        results.forEach { result ->
            assertTrue(result is SearchResult.MediaServerResult)
            assertNull(result.instanceType, "MediaServer results should not have an InstanceType like Radarr or Sonarr")
            assertEquals(SearchFilter.MediaServer(MediaServerType.Plex), result.searchFilter)
            assertTrue(SearchFilter.MediaServer(MediaServerType.Plex).matches(result))
            assertFalse(SearchFilter.Instance(InstanceType.Radarr).matches(result))
            assertFalse(SearchFilter.Instance(InstanceType.Sonarr).matches(result))
        }
    }

    @Test
    fun testDuplicatePrecedenceArrOverMediaServerWithSecondaryBadge() = runBlocking {
        val radarrInstance = Instance(
            id = 1L,
            type = InstanceType.Radarr,
            url = "http://localhost:7878",
            apiKey = EncryptedString("key"),
            label = "Radarr",
            enabled = true,
            selected = true,
        )
        val fakeDao = FakeInstanceDao(listOf(radarrInstance))
        val instanceRepo = InstanceRepository(fakeDao)

        val radarrLookupJson =
            json.encodeToString(
                listOf(
                    ArrMovie(
                        title = "Inception",
                        cleanTitle = "inception",
                        originalLanguage = Language(1, "English"),
                        year = 2010,
                        qualityProfileId = 1,
                        monitored = true,
                        runtime = 148,
                        status = MediaStatus.Released,
                        minimumAvailability = MediaStatus.Released,
                        tmdbId = 27205,
                        secondaryYearSourceId = 0,
                    ),
                ),
            )

        val mockFactory = MockHttpClientFactory(json) {
            MockEngine { request ->
                val path = request.url.encodedPath
                if (path.contains("movie/lookup")) {
                    respond(radarrLookupJson, HttpStatusCode.OK, headersOf("Content-Type", "application/json"))
                } else {
                    respond("[]", HttpStatusCode.OK, headersOf("Content-Type", "application/json"))
                }
            }
        }
        val instanceManager = InstanceManager(instanceRepo, mockFactory, fakeMigrationUseCase, logger, fakeMokoStrings).also(managers::add)
        instanceManager.awaitRepos(1)

        val plexItem = UnifiedMediaItem(
            id = "plex-101",
            serverType = MediaServerType.Plex,
            serverId = "srv-1",
            title = "Inception",
            type = MediaType.MOVIE,
            year = 2010,
            tmdbId = 27205L,
        )
        val mockRepo = createMockMediaServerRepo(MediaServerType.Plex, "Home Plex", listOf(plexItem))
        val mockMediaServerManager = createMockMediaServerManager(listOf(mockRepo))

        val useCase = GlobalSearchUseCase(instanceManager, mockMediaServerManager)
        val emissions = useCase("Inception").toList()

        assertTrue(emissions.isNotEmpty())
        val finalResults = emissions.last()
        assertEquals(1, finalResults.size, "Arr and Plex should be deduplicated into 1 item")

        val result = finalResults.first()
        assertTrue(result is SearchResult.ArrMediaResult, "Arr should take precedence over MediaServer")
        assertEquals(MediaServerType.Plex, result.mediaServerType)
        assertEquals("Home Plex", result.mediaServerLabel)
    }

    @Test
    fun testDuplicatePrecedenceArrOverMediaServerWhenMediaServerTmdbIdIsNull() = runBlocking {
        val radarrInstance = Instance(
            id = 1L,
            type = InstanceType.Radarr,
            url = "http://localhost:7878",
            apiKey = EncryptedString("key"),
            label = "Radarr",
            enabled = true,
            selected = true,
        )
        val fakeDao = FakeInstanceDao(listOf(radarrInstance))
        val instanceRepo = InstanceRepository(fakeDao)

        val radarrLookupJson =
            json.encodeToString(
                listOf(
                    ArrMovie(
                        title = "Avatar",
                        cleanTitle = "avatar",
                        originalLanguage = Language(1, "English"),
                        year = 2009,
                        qualityProfileId = 1,
                        monitored = true,
                        runtime = 162,
                        status = MediaStatus.Released,
                        minimumAvailability = MediaStatus.Released,
                        tmdbId = 19995,
                        secondaryYearSourceId = 0,
                    ),
                ),
            )

        val mockFactory = MockHttpClientFactory(json) {
            MockEngine { request ->
                val path = request.url.encodedPath
                if (path.contains("movie/lookup")) {
                    respond(radarrLookupJson, HttpStatusCode.OK, headersOf("Content-Type", "application/json"))
                } else {
                    respond("[]", HttpStatusCode.OK, headersOf("Content-Type", "application/json"))
                }
            }
        }
        val instanceManager = InstanceManager(instanceRepo, mockFactory, fakeMigrationUseCase, logger, fakeMokoStrings).also(managers::add)
        instanceManager.awaitRepos(1)

        val plexItem = UnifiedMediaItem(
            id = "plex-avatar-1",
            serverType = MediaServerType.Plex,
            serverId = "srv-1",
            title = "Avatar",
            type = MediaType.MOVIE,
            year = 2009,
            tmdbId = null,
        )
        val mockRepo = createMockMediaServerRepo(MediaServerType.Plex, "Home Plex", listOf(plexItem))
        val mockMediaServerManager = createMockMediaServerManager(listOf(mockRepo))

        val useCase = GlobalSearchUseCase(instanceManager, mockMediaServerManager)
        val emissions = useCase("Avatar").toList()

        assertTrue(emissions.isNotEmpty())
        val finalResults = emissions.last()
        assertEquals(1, finalResults.size, "Arr and Plex should be deduplicated even when Plex tmdbId is null")

        val result = finalResults.first()
        assertTrue(result is SearchResult.ArrMediaResult, "Arr should take precedence over MediaServer")
        assertEquals(MediaServerType.Plex, result.mediaServerType)
        assertEquals("Home Plex", result.mediaServerLabel)
    }

    @Test
    fun testDuplicatePrecedenceMediaServerOverSeerr() = runBlocking {
        val seerrInstance = Instance(
            id = 2L,
            type = InstanceType.Seerr,
            url = "http://localhost:5055",
            apiKey = EncryptedString("key"),
            label = "Seerr",
            enabled = true,
            selected = true,
        )
        val fakeDao = FakeInstanceDao(listOf(seerrInstance))
        val instanceRepo = InstanceRepository(fakeDao)

        val seerrSearchJson = """
            {
                "page": 1,
                "totalPages": 1,
                "totalResults": 1,
                "results": [
                    {
                        "id": 27205,
                        "mediaType": "movie",
                        "title": "Inception",
                        "releaseDate": "2010-07-16"
                    }
                ]
            }
        """.trimIndent()

        val mockFactory = MockHttpClientFactory(json) {
            MockEngine { request ->
                val path = request.url.encodedPath
                if (path.contains("search")) {
                    respond(seerrSearchJson, HttpStatusCode.OK, headersOf("Content-Type", "application/json"))
                } else {
                    respond("[]", HttpStatusCode.OK, headersOf("Content-Type", "application/json"))
                }
            }
        }
        val instanceManager = InstanceManager(instanceRepo, mockFactory, fakeMigrationUseCase, logger, fakeMokoStrings).also(managers::add)
        instanceManager.awaitRepos(1)

        val jellyfinItem = UnifiedMediaItem(
            id = "jf-999",
            serverType = MediaServerType.Jellyfin,
            serverId = "jf-srv",
            title = "Inception",
            type = MediaType.MOVIE,
            year = 2010,
            tmdbId = 27205L,
        )
        val mockRepo = createMockMediaServerRepo(MediaServerType.Jellyfin, "Jellyfin Server", listOf(jellyfinItem))
        val mockMediaServerManager = createMockMediaServerManager(listOf(mockRepo))

        val useCase = GlobalSearchUseCase(instanceManager, mockMediaServerManager)
        val emissions = useCase("Inception").toList()

        assertTrue(emissions.isNotEmpty())
        val finalResults = emissions.last()
        assertEquals(1, finalResults.size, "Jellyfin and Seerr should be deduplicated")

        val result = finalResults.first()
        assertTrue(result is SearchResult.MediaServerResult, "MediaServer should take precedence over Seerr")
        assertEquals(MediaServerType.Jellyfin, result.serverType)
        assertEquals("Jellyfin Server", result.serverLabel)
    }

    private suspend fun InstanceManager.awaitRepos(count: Int) {
        withTimeout(5000.milliseconds) {
            instanceRepositories.first { it.size == count }
        }
    }
}
