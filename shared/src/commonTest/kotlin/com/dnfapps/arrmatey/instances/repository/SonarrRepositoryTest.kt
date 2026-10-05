package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.arr.api.client.ListenarrInstantSerializer
import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import dev.shivathapaa.logger.api.LoggerFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
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

    private val fakeLogger = LoggerFactory.get("test")

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
                    json(
                        Json {
                            isLenient = true
                            ignoreUnknownKeys = true
                            encodeDefaults = true
                            explicitNulls = false
                            coerceInputValues = true
                            serializersModule =
                                SerializersModule {
                                    contextual(Instant::class, ListenarrInstantSerializer)
                                }
                        },
                    )
                }
            }
        val repository = SonarrRepository(fakeInstance, httpClient, fakeLogger)

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
            json(
                Json {
                    ignoreUnknownKeys = true
                    serializersModule =
                        SerializersModule {
                            contextual(Instant::class, ListenarrInstantSerializer)
                        }
                },
            )
        }
    }

    @Test
    fun testGetSeriesHistoryIsScopedToSeries() = runTest {
        val requests = mutableListOf<Url>()
        val repository = SonarrRepository(fakeInstance, historyHttpClient(requests), fakeLogger)

        repository.getSeriesHistory(seriesId = 7)

        val url = requests.single()
        assertEquals("/api/v3/history/series", url.encodedPath)
        assertEquals("7", url.parameters["seriesId"])
        assertEquals(1, repository.observeItemHistory(7).first().size)
    }

    @Test
    fun testEpisodeHistoryDoesNotOverwriteSeriesHistory() = runTest {
        val requests = mutableListOf<Url>()
        val repository = SonarrRepository(fakeInstance, historyHttpClient(requests), fakeLogger)

        repository.getSeriesHistory(seriesId = 7)
        // Episode id equal to the series id must not clobber the series entry.
        repository.getItemHistory(itemId = 7)

        assertEquals("7", requests.last().parameters["episodeId"])
        assertEquals(1, repository.observeItemHistory(7).first().size)
    }
}
