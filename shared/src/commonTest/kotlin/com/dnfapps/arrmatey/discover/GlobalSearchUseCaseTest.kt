package com.dnfapps.arrmatey.discover

import com.dnfapps.arrmatey.arr.api.client.HttpClientFactory
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
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
import com.dnfapps.arrmatey.discover.model.SearchResult
import com.dnfapps.arrmatey.discover.usecase.GlobalSearchUseCase
import com.dnfapps.arrmatey.downloadclient.database.DownloadClientDao
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import dev.shivathapaa.logger.api.LoggerFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logger
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class GlobalSearchUseCaseTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val logger = LoggerFactory.get("test")

    private class FakeInstanceDao(
        initialInstances: List<Instance> = emptyList(),
    ) : InstanceDao {
        val instances = MutableStateFlow(initialInstances)

        override suspend fun insert(instance: Instance): Long = 0L

        override suspend fun delete(instance: Instance) {}

        override suspend fun update(instance: Instance): Int = 0

        override suspend fun updateAll(instances: List<Instance>) {}

        override fun observeAllInstances(): Flow<List<Instance>> = instances

        override fun observeInstancesByType(type: InstanceType): Flow<List<Instance>> =
            MutableStateFlow(instances.value.filter { it.type == type })

        override suspend fun getAllInstances(): List<Instance> = instances.value

        override suspend fun getInstanceById(id: Long): Instance? = instances.value.find { it.id == id }

        override fun observeSelectedInstance(type: InstanceType): Flow<Instance?> =
            MutableStateFlow(instances.value.find { it.type == type && it.selected })

        override suspend fun getInstancesOfType(type: InstanceType): List<Instance> = instances.value.filter { it.type == type }

        override suspend fun unselectAllOf(type: InstanceType) {}

        override suspend fun selectInstance(id: Long) {}

        override suspend fun findByUrl(url: String): Long? = null

        override suspend fun findByLabel(label: String): Long? = null

        override suspend fun findOtherByUrl(
            url: String,
            currentId: Long,
        ): Long? = null

        override suspend fun findOtherByLabel(
            label: String,
            currentId: Long,
        ): Long? = null

        override suspend fun ensureFirstSelectedIfNone(type: InstanceType) {}
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

        override suspend fun findOtherByUrl(
            url: String,
            currentId: Long,
        ): Long? = null

        override suspend fun findOtherByLabel(
            label: String,
            currentId: Long,
        ): Long? = null

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

    private fun sonarrInstance(id: Long) =
        Instance(
            id = id,
            label = "Sonarr $id",
            url = "http://localhost:8989",
            apiKey = EncryptedString("key"),
            type = InstanceType.Sonarr,
            enabled = true,
        )

    private fun radarrInstance(id: Long) =
        Instance(
            id = id,
            label = "Radarr $id",
            url = "http://localhost:7878",
            apiKey = EncryptedString("key"),
            type = InstanceType.Radarr,
            enabled = true,
        )

    private fun seerrInstance(id: Long) =
        Instance(
            id = id,
            label = "Seerr $id",
            url = "http://localhost:5055",
            apiKey = EncryptedString("key"),
            type = InstanceType.Seerr,
            enabled = true,
        )

    private suspend fun InstanceManager.awaitRepos(count: Int) {
        withTimeout(5000.milliseconds) {
            instanceRepositories.first { it.size == count }
        }
    }

    @Test
    fun testBlankQueryEmitsEmptyList() =
        runBlocking {
            val fakeDao = FakeInstanceDao()
            val instanceRepo = InstanceRepository(fakeDao)
            val mockFactory = MockHttpClientFactory(json) { MockEngine { respond("[]", HttpStatusCode.OK) } }
            val manager = InstanceManager(instanceRepo, mockFactory, fakeMigrationUseCase, logger)

            val useCase = GlobalSearchUseCase(manager)
            val results = useCase("").toList()
            assertEquals(listOf(emptyList<SearchResult>()), results)
        }

    @Test
    fun testBothSonarrAndRadarrConfiguredExcludesSeerrMediaResults() =
        runBlocking {
            val fakeDao = FakeInstanceDao(listOf(sonarrInstance(1), radarrInstance(2), seerrInstance(3)))
            val instanceRepo = InstanceRepository(fakeDao)

            val sonarrLookupJson =
                json.encodeToString(
                    listOf(
                        ArrSeries(
                            title = "Breaking Bad",
                            cleanTitle = "breakingbad",
                            originalLanguage = Language(1, "English"),
                            year = 2008,
                            qualityProfileId = 1,
                            monitored = true,
                            runtime = 45,
                            status = MediaStatus.Ended,
                            seriesType = SeriesType.Standard,
                            ended = true,
                            seasonFolder = true,
                            monitorNewItems = MonitorNewItems.None,
                            useSceneNumbering = false,
                            tvdbId = 81189,
                        ),
                    ),
                )

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

            val seerrSearchJson =
                """
                {
                  "page": 1,
                  "totalPages": 1,
                  "totalResults": 3,
                  "results": [
                    {
                      "id": 27205,
                      "mediaType": "movie",
                      "title": "Inception (Seerr)",
                      "voteCount": 100,
                      "voteAverage": 8.5
                    },
                    {
                      "id": 81189,
                      "mediaType": "tv",
                      "name": "Breaking Bad (Seerr)",
                      "voteCount": 200,
                      "voteAverage": 9.0
                    },
                    {
                      "id": 12345,
                      "mediaType": "person",
                      "name": "Christopher Nolan",
                      "popularity": 50.0
                    }
                  ]
                }
                """.trimIndent()

            val mockFactory =
                MockHttpClientFactory(json) { instance ->
                    MockEngine { request ->
                        val path = request.url.encodedPath
                        val content =
                            when {
                                instance.type == InstanceType.Sonarr && path.contains("series/lookup") -> sonarrLookupJson
                                instance.type == InstanceType.Radarr && path.contains("movie/lookup") -> radarrLookupJson
                                instance.type == InstanceType.Seerr && path.contains("search") -> seerrSearchJson
                                else -> "[]"
                            }
                        respond(content, HttpStatusCode.OK, headersOf("Content-Type", "application/json"))
                    }
                }

            val manager = InstanceManager(instanceRepo, mockFactory, fakeMigrationUseCase, logger)
            manager.awaitRepos(3)

            val useCase = GlobalSearchUseCase(manager)
            val emissions = useCase("test").toList()

            assertTrue(emissions.isNotEmpty())
            val finalResult = emissions.last()

            // Sonarr and Radarr results should be present
            assertTrue(finalResult.any { it is SearchResult.ArrMediaResult && it.title == "Breaking Bad" })
            assertTrue(finalResult.any { it is SearchResult.ArrMediaResult && it.title == "Inception" })

            // Seerr Person should be present
            assertTrue(finalResult.any { it is SearchResult.SeerrPersonResult && it.title == "Christopher Nolan" })

            // Seerr Media results should NOT be present (since both Sonarr and Radarr are configured)
            assertTrue(finalResult.none { it is SearchResult.SeerrMediaResult })

            manager.cleanup()
        }

    @Test
    fun testOnlySonarrConfiguredIncludesSeerrMediaResults() =
        runBlocking {
            val fakeDao = FakeInstanceDao(listOf(sonarrInstance(1), seerrInstance(2)))
            val instanceRepo = InstanceRepository(fakeDao)

            val sonarrLookupJson =
                json.encodeToString(
                    listOf(
                        ArrSeries(
                            title = "Breaking Bad",
                            cleanTitle = "breakingbad",
                            originalLanguage = Language(1, "English"),
                            year = 2008,
                            qualityProfileId = 1,
                            monitored = true,
                            runtime = 45,
                            status = MediaStatus.Ended,
                            seriesType = SeriesType.Standard,
                            ended = true,
                            seasonFolder = true,
                            monitorNewItems = MonitorNewItems.None,
                            useSceneNumbering = false,
                            tvdbId = 81189,
                        ),
                    ),
                )

            val seerrSearchJson =
                """
                {
                  "page": 1,
                  "totalPages": 1,
                  "totalResults": 2,
                  "results": [
                    {
                      "id": 27205,
                      "mediaType": "movie",
                      "title": "Inception",
                      "voteCount": 100,
                      "voteAverage": 8.5
                    },
                    {
                      "id": 12345,
                      "mediaType": "person",
                      "name": "Christopher Nolan",
                      "popularity": 50.0
                    }
                  ]
                }
                """.trimIndent()

            val mockFactory =
                MockHttpClientFactory(json) { instance ->
                    MockEngine { request ->
                        val path = request.url.encodedPath
                        val content =
                            when {
                                instance.type == InstanceType.Sonarr && path.contains("series/lookup") -> sonarrLookupJson
                                instance.type == InstanceType.Seerr && path.contains("search") -> seerrSearchJson
                                else -> "[]"
                            }
                        respond(content, HttpStatusCode.OK, headersOf("Content-Type", "application/json"))
                    }
                }

            val manager = InstanceManager(instanceRepo, mockFactory, fakeMigrationUseCase, logger)
            manager.awaitRepos(2)

            val useCase = GlobalSearchUseCase(manager)
            val emissions = useCase("test").toList()

            assertTrue(emissions.isNotEmpty())
            val finalResult = emissions.last()

            // Sonarr result present
            assertTrue(finalResult.any { it is SearchResult.ArrMediaResult && it.title == "Breaking Bad" })

            // Seerr Person present
            assertTrue(finalResult.any { it is SearchResult.SeerrPersonResult && it.title == "Christopher Nolan" })

            // Seerr Media result present because Radarr is not configured
            assertTrue(finalResult.any { it is SearchResult.SeerrMediaResult && it.title == "Inception" })

            manager.cleanup()
        }

    @Test
    fun testSearchCancellationAbortsPendingRequests() =
        runBlocking {
            val fakeDao = FakeInstanceDao(listOf(sonarrInstance(1)))
            val instanceRepo = InstanceRepository(fakeDao)

            val requestStarted = CompletableDeferred<Unit>()
            var cancelled = false
            val mockFactory =
                MockHttpClientFactory(json) { _ ->
                    MockEngine { _ ->
                        try {
                            requestStarted.complete(Unit)
                            delay(10_000)
                            respond("[]", HttpStatusCode.OK)
                        } catch (e: CancellationException) {
                            cancelled = true
                            throw e
                        }
                    }
                }

            val manager = InstanceManager(instanceRepo, mockFactory, fakeMigrationUseCase, logger)
            manager.awaitRepos(1)

            val useCase = GlobalSearchUseCase(manager)
            val job =
                launch {
                    useCase("test").collect {}
                }
            requestStarted.await()
            job.cancel()
            job.join()

            assertTrue(cancelled)
            manager.cleanup()
        }
}
