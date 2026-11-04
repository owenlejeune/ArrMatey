package com.dnfapps.arrmatey.mediaserver.usecase

import com.dnfapps.arrmatey.arr.api.client.HttpClientFactory
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.Language
import com.dnfapps.arrmatey.arr.api.model.MediaStatus
import com.dnfapps.arrmatey.arr.api.model.MonitorNewItems
import com.dnfapps.arrmatey.arr.api.model.SeriesType
import com.dnfapps.arrmatey.database.CredentialMigrationUseCase
import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.database.dao.InstanceDao
import com.dnfapps.arrmatey.datastore.DataStoreFactory
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.downloadclient.database.DownloadClientDao
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.mediaserver.model.MediaServerType
import com.dnfapps.arrmatey.mediaserver.model.MediaType
import com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaItem
import com.dnfapps.arrmatey.utils.fakeMokoStrings
import com.dnfapps.arrmatey.utils.testLogger
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logger
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.milliseconds

class ResolveSonarrEpisodeUseCaseTest {
    private val logger = testLogger()
    private val fakeMoko = fakeMokoStrings
    private val managers = mutableListOf<InstanceManager>()

    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
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

    private val silentLogger =
        object : Logger {
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

    private val fakeMigrationUseCase =
        object : CredentialMigrationUseCase(
            FakeInstanceDao(),
            FakeDownloadClientDao(),
            PreferencesStore(DataStoreFactory()),
        ) {
            override suspend fun invoke() {}
        }

    @AfterTest
    fun tearDown() {
        managers.forEach(InstanceManager::cleanup)
        managers.clear()
    }

    private fun sonarrInstance(id: Long = 1) = Instance(
        id = id,
        label = "Sonarr $id",
        url = "http://localhost:8989",
        apiKey = EncryptedString("key"),
        type = InstanceType.Sonarr,
        enabled = true,
    )

    private suspend fun InstanceManager.awaitRepos(count: Int) {
        withTimeout(5000.milliseconds) {
            instanceRepositories.first { it.size == count }
        }
    }

    private fun createSeries(
        id: Long = 10,
        title: String = "The Office (US)",
        cleanTitle: String = "theofficeus",
        tvdbId: Long = 73244,
    ) = ArrSeries(
        id = id,
        title = title,
        cleanTitle = cleanTitle,
        tvdbId = tvdbId,
        year = 2005,
        originalLanguage = Language(id = 1, name = "English"),
        qualityProfileId = 1,
        monitored = true,
        runtime = 30,
        status = MediaStatus.Continuing,
        ended = false,
        seasonFolder = true,
        monitorNewItems = MonitorNewItems.All,
        useSceneNumbering = false,
        seriesType = SeriesType.Standard,
    )

    private fun createEpisode(
        id: Long = 100,
        seriesId: Long = 10,
        seasonNumber: Int = 2,
        episodeNumber: Int = 5,
        title: String = "Halloween",
    ) = Episode(
        id = id,
        seriesId = seriesId,
        tvdbId = null,
        episodeFileId = null,
        seasonNumber = seasonNumber,
        episodeNumber = episodeNumber,
        title = title,
        runtime = 22,
        hasFile = true,
        monitored = true,
        unverifiedSceneNumbering = false,
    )

    @Test
    fun testNonEpisodeReturnsNull() = runBlocking {
        val fakeDao = FakeInstanceDao(listOf(sonarrInstance()))
        val instanceRepo = InstanceRepository(fakeDao)
        val mockFactory = MockHttpClientFactory(json) { MockEngine { respond("[]", HttpStatusCode.OK) } }
        val manager = InstanceManager(instanceRepo, mockFactory, fakeMigrationUseCase, logger, fakeMoko).also(managers::add)
        manager.awaitRepos(1)

        val useCase = ResolveSonarrEpisodeUseCase(manager)
        val movieItem = UnifiedMediaItem(
            id = "1",
            title = "Inception",
            type = MediaType.MOVIE,
            serverType = MediaServerType.Plex,
        )
        val result = useCase(movieItem)
        assertNull(result)
    }

    @Test
    fun testNoSonarrConfiguredReturnsNull() = runBlocking {
        val fakeDao = FakeInstanceDao(emptyList())
        val instanceRepo = InstanceRepository(fakeDao)
        val mockFactory = MockHttpClientFactory(json) { MockEngine { respond("[]", HttpStatusCode.OK) } }
        val manager = InstanceManager(instanceRepo, mockFactory, fakeMigrationUseCase, logger, fakeMoko).also(managers::add)

        val useCase = ResolveSonarrEpisodeUseCase(manager)
        val episodeItem = UnifiedMediaItem(
            id = "100",
            title = "Halloween",
            type = MediaType.EPISODE,
            seriesTitle = "The Office (US)",
            seasonNumber = 2,
            episodeNumber = 5,
            serverType = MediaServerType.Plex,
        )
        val result = useCase(episodeItem)
        assertNull(result)
    }

    @Test
    fun testResolvesEpisodeMatchingSonarrSeriesAndEpisode() = runBlocking {
        val series = createSeries()
        val episode = createEpisode()

        val fakeDao = FakeInstanceDao(listOf(sonarrInstance(1)))
        val instanceRepo = InstanceRepository(fakeDao)
        val mockFactory = MockHttpClientFactory(json) {
            MockEngine { request ->
                val path = request.url.encodedPath
                val responseJson = when {
                    path.endsWith("/series") -> json.encodeToString(listOf(series))
                    path.endsWith("/episode") -> json.encodeToString(listOf(episode))
                    else -> "[]"
                }
                respond(
                    content = responseJson,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        }
        val manager = InstanceManager(instanceRepo, mockFactory, fakeMigrationUseCase, logger, fakeMoko).also(managers::add)
        manager.awaitRepos(1)

        val useCase = ResolveSonarrEpisodeUseCase(manager)
        val episodeItem = UnifiedMediaItem(
            id = "plex-ep-1",
            title = "Halloween",
            type = MediaType.EPISODE,
            seriesTitle = "The Office (US)",
            seasonNumber = 2,
            episodeNumber = 5,
            tvdbId = 73244,
            serverType = MediaServerType.Plex,
        )

        val result = useCase(episodeItem)
        assertNotNull(result)
        assertEquals(series.id, result.series.id)
        assertEquals(episode.id, result.episode.id)
        assertEquals(1L, result.instanceId)
    }

    @Test
    fun testResolvesEpisodeByCleanTitle() = runBlocking {
        val series = createSeries(title = "The Office (US)", cleanTitle = "theofficeus")
        val episode = createEpisode()

        val fakeDao = FakeInstanceDao(listOf(sonarrInstance(1)))
        val instanceRepo = InstanceRepository(fakeDao)
        val mockFactory = MockHttpClientFactory(json) {
            MockEngine { request ->
                val path = request.url.encodedPath
                val responseJson = when {
                    path.endsWith("/series") -> json.encodeToString(listOf(series))
                    path.endsWith("/episode") -> json.encodeToString(listOf(episode))
                    else -> "[]"
                }
                respond(
                    content = responseJson,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        }
        val manager = InstanceManager(instanceRepo, mockFactory, fakeMigrationUseCase, logger, fakeMoko).also(managers::add)
        manager.awaitRepos(1)

        val useCase = ResolveSonarrEpisodeUseCase(manager)
        val episodeItem = UnifiedMediaItem(
            id = "plex-ep-1",
            title = "Halloween",
            type = MediaType.EPISODE,
            seriesTitle = "The Office (US)!",
            seasonNumber = 2,
            episodeNumber = 5,
            tvdbId = null,
            serverType = MediaServerType.Plex,
        )

        val result = useCase(episodeItem)
        assertNotNull(result)
        assertEquals(series.id, result.series.id)
        assertEquals(episode.id, result.episode.id)
    }
}
