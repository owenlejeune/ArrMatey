package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.arr.api.client.ListenarrInstantSerializer
import com.dnfapps.arrmatey.arr.api.client.SonarrApiVersion
import com.dnfapps.arrmatey.arr.api.client.SonarrClient
import com.dnfapps.arrmatey.arr.api.model.SeriesRelease
import com.dnfapps.arrmatey.arr.api.model.SonarrQueueItem
import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.networking.NetworkResult
import dev.shivathapaa.logger.api.LoggerFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class SonarrRepositoryTest {
    private val fakeInstance =
        Instance(
            id = 1,
            label = "Test Sonarr",
            url = "http://localhost:8989",
            apiKey = EncryptedString("test-api-key"),
            type = InstanceType.Sonarr,
            enabled = true,
        )

    private val fakeV5Instance =
        Instance(
            id = 2,
            label = "Test Sonarr V5",
            url = "http://localhost:8989",
            apiKey = EncryptedString("test-api-key"),
            type = InstanceType.Sonarr,
            enabled = true,
        )

    private val fakeLogger = LoggerFactory.get("test")

    private val testJson = Json {
        isLenient = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
        coerceInputValues = true
        serializersModule = SerializersModule {
            contextual(Instant::class, ListenarrInstantSerializer)
        }
    }

    @Test
    fun testGetEpisodes() = runTest {
        val mockEngine =
            MockEngine { _ ->
                respond(
                    content =
                    """
                            [{
                                "id": 10,
                                "seriesId": 1,
                                "episodeNumber": 1,
                                "seasonNumber": 1,
                                 "title": "Pilot",
                                 "hasFile": false,
                                 "monitored": true,
                                 "unverifiedSceneNumbering": false
                             }]
                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type", "application/json"),
                )
            }
        val httpClient =
            HttpClient(mockEngine) {
                install(ContentNegotiation) {
                    json(testJson)
                }
            }
        val repository = SonarrRepository(fakeInstance, httpClient, fakeLogger, com.dnfapps.arrmatey.utils.fakeMokoStrings)

        repository.getEpisodes(seriesId = 1)

        assertNotNull(repository.episodes.value[1])
        assertEquals(1, repository.episodes.value[1]?.size)
        assertEquals(
            "Pilot",
            repository.episodes.value[1]
                ?.first()
                ?.title,
        )
    }

    private val historyItemJson =
        """
        {
            "id": 1,
            "eventType": "grabbed",
            "date": "2026-01-01T00:00:00Z",
            "quality": {"quality": {"id": 1, "name": "HDTV-720p"}, "revision": {"version": 1, "real": 0, "isRepack": false}},
            "seriesId": 7,
            "episodeId": 70
        }
        """.trimIndent()

    private fun historyHttpClient(requests: MutableList<Url>): HttpClient = HttpClient(
        MockEngine { request ->
            requests += request.url
            val body =
                if (request.url.encodedPath.endsWith("/history/series")) {
                    "[$historyItemJson]"
                } else {
                    """{"page": 1, "pageSize": 100, "totalRecords": 0, "records": []}"""
                }
            respond(body, HttpStatusCode.OK, headersOf("Content-Type", "application/json"))
        },
    ) {
        install(ContentNegotiation) {
            json(testJson)
        }
    }

    @Test
    fun testGetSeriesHistoryIsScopedToSeries() = runTest {
        val requests = mutableListOf<Url>()
        val repository = SonarrRepository(fakeInstance, historyHttpClient(requests), fakeLogger, com.dnfapps.arrmatey.utils.fakeMokoStrings)
        repository.sonarrClient.apiVersion = SonarrApiVersion.V3

        repository.getSeriesHistory(seriesId = 7)

        val url = requests.single()
        assertEquals("/api/v3/history/series", url.encodedPath)
        assertEquals("7", url.parameters["seriesId"])
        assertEquals(1, repository.observeItemHistory(7).first().size)
    }

    @Test
    fun testEpisodeHistoryDoesNotOverwriteSeriesHistory() = runTest {
        val requests = mutableListOf<Url>()
        val repository = SonarrRepository(fakeInstance, historyHttpClient(requests), fakeLogger, com.dnfapps.arrmatey.utils.fakeMokoStrings)
        repository.sonarrClient.apiVersion = SonarrApiVersion.V3

        repository.getSeriesHistory(seriesId = 7)
        repository.getItemHistory(itemId = 7)

        assertEquals("7", requests.last().parameters["episodeId"])
        assertEquals(1, repository.observeItemHistory(7).first().size)
    }

    @Test
    fun testV5SubresourceQueryParamsAndApiPath() = runTest {
        val requests = mutableListOf<Url>()
        val httpClient = HttpClient(
            MockEngine { request ->
                requests += request.url
                respond("""{"page": 1, "pageSize": 100, "totalRecords": 0, "records": []}""", HttpStatusCode.OK, headersOf("Content-Type", "application/json"))
            },
        ) {
            install(ContentNegotiation) {
                json(testJson)
            }
        }

        val client = SonarrClient(fakeV5Instance, httpClient, initialApiVersion = SonarrApiVersion.V5)
        client.getItemHistory(id = 42, page = 1, pageSize = 10)

        val url = requests.single()
        assertEquals("/api/v5/history", url.encodedPath)
        assertEquals(listOf("Series", "Episode"), url.parameters.getAll("includeSubresources"))
    }

    @Test
    fun testV5SonarrQueueItemDeserialization() {
        val v5QueueJson = """
        {
            "id": 100,
            "title": "Series.S01.1080p.WEB-DL",
            "size": 5000000000,
            "sizeLeft": 2500000000,
            "timeLeft": "00:15:00",
            "protocol": "torrent",
            "quality": {"quality": {"id": 1, "name": "WEB-DL-1080p"}, "revision": {"version": 1, "real": 0, "isRepack": false}},
            "trackedDownloadStatus": "ok",
            "trackedDownloadState": "downloading",
            "seriesId": 12,
            "episodeIds": [101, 102, 103],
            "seasons": [{"seasonNumber": 1, "monitored": true}],
            "episodesWithFilesCount": 0,
            "isFullSeason": true
        }
        """.trimIndent()

        val item = testJson.decodeFromString<SonarrQueueItem>(v5QueueJson)
        assertEquals(100, item.id)
        assertEquals(2500000000f, item.sizeleft)
        assertEquals("00:15:00", item.timeleft)
        assertEquals(listOf(101L, 102L, 103L), item.effectiveEpisodeIds)
        assertEquals(1, item.primarySeasonNumber)
        assertTrue(item.isFullSeason)
    }

    @Test
    fun testV5BulkQueueDeletion() = runTest {
        var requestedPath = ""
        val httpClient = HttpClient(
            MockEngine { request ->
                requestedPath = request.url.encodedPath
                respond("""{}""", HttpStatusCode.OK, headersOf("Content-Type", "application/json"))
            },
        ) {
            install(ContentNegotiation) {
                json(testJson)
            }
        }

        val client = SonarrClient(fakeV5Instance, httpClient, initialApiVersion = SonarrApiVersion.V5)
        client.deleteActivityTask(id = 99, removeFromClient = true, blocklist = false, skipRedownload = false)

        assertEquals("/api/v5/queue/bulk", requestedPath)
    }

    @Test
    fun testSeriesReleaseDeserializationWithDateOnly() {
        val releaseJson = """
        {
            "guid": "test-guid-123",
            "title": "Series.Title.S01E01.1080p.WEB-DL",
            "indexerId": 1,
            "protocol": "torrent",
            "seasonNumber": 1,
            "airDate": "2026-11-15",
            "publishDate": "2026-11-15"
        }
        """.trimIndent()

        val release = testJson.decodeFromString<SeriesRelease>(releaseJson)
        assertEquals("test-guid-123", release.guid)
        assertEquals(Instant.parse("2026-11-15T00:00:00Z"), release.airDate)
        assertEquals(Instant.parse("2026-11-15T00:00:00Z"), release.publishDate)
    }

    @Test
    fun testInstantSerializerWithVariousFormats() {
        val jsonWithFormats = """
        [
            {"guid": "g1", "title": "T1", "indexerId": 1, "protocol": "torrent", "seasonNumber": 1, "airDate": "2026-11-15"},
            {"guid": "g2", "title": "T2", "indexerId": 1, "protocol": "torrent", "seasonNumber": 1, "airDate": "2026-11-15T12:00:00"},
            {"guid": "g3", "title": "T3", "indexerId": 1, "protocol": "torrent", "seasonNumber": 1, "airDate": "2026-11-15T12:00:00Z"},
            {"guid": "g4", "title": "T4", "indexerId": 1, "protocol": "torrent", "seasonNumber": 1, "airDate": "2026-11-15T12:00:00+02:00"},
            {"guid": "g5", "title": "T5", "indexerId": 1, "protocol": "torrent", "seasonNumber": 1, "airDate": "2026-11-15T12:00:00-05:00"}
        ]
        """.trimIndent()

        val releases = testJson.decodeFromString<List<SeriesRelease>>(jsonWithFormats)
        assertEquals(5, releases.size)
        assertEquals(Instant.parse("2026-11-15T00:00:00Z"), releases[0].airDate)
        assertEquals(Instant.parse("2026-11-15T12:00:00Z"), releases[1].airDate)
        assertEquals(Instant.parse("2026-11-15T12:00:00Z"), releases[2].airDate)
        assertEquals(Instant.parse("2026-11-15T10:00:00Z"), releases[3].airDate)
        assertEquals(Instant.parse("2026-11-15T17:00:00Z"), releases[4].airDate)
    }

    @Test
    fun testConcurrentVersionDeterminationOnV3Server() = runTest {
        val requests = mutableListOf<Url>()
        val httpClient = HttpClient(
            MockEngine { request ->
                requests += request.url
                when {
                    request.url.encodedPath.endsWith("/api/v5/system/status") -> {
                        respond("Not Found", HttpStatusCode.NotFound)
                    }
                    request.url.encodedPath.endsWith("/api/v3/system/status") -> {
                        respond("""{"version": "3.0.10.1567"}""", HttpStatusCode.OK, headersOf("Content-Type", "application/json"))
                    }
                    request.url.encodedPath.endsWith("/api/v3/series") -> {
                        respond("[]", HttpStatusCode.OK, headersOf("Content-Type", "application/json"))
                    }
                    request.url.encodedPath.endsWith("/api/v3/qualityprofile") -> {
                        respond("[]", HttpStatusCode.OK, headersOf("Content-Type", "application/json"))
                    }
                    else -> respond("Error", HttpStatusCode.NotFound)
                }
            },
        ) {
            install(ContentNegotiation) {
                json(testJson)
            }
        }

        val client = SonarrClient(fakeInstance, httpClient, initialApiVersion = null)

        val deferredLibrary = async { client.getLibrary() }
        val deferredProfiles = async { client.getQualityProfiles() }

        val libraryResult = deferredLibrary.await()
        val profilesResult = deferredProfiles.await()

        assertTrue(libraryResult is NetworkResult.Success, "Library result should be Success on V3 server")
        assertTrue(profilesResult is NetworkResult.Success, "Quality profiles result should be Success on V3 server")
        assertEquals(SonarrApiVersion.V3, client.apiVersion)
    }
}
