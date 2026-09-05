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
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
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
    fun testGetEpisodes() =
        runTest {
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
}
