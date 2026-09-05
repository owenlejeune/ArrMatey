package com.dnfapps.arrmatey.instances.repository

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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class LidarrRepositoryTest {
    private val fakeInstance =
        Instance(
            id = 3,
            label = "Test Lidarr",
            url = "http://localhost:8686",
            apiKey = EncryptedString("test-api-key"),
            type = InstanceType.Lidarr,
            enabled = true,
        )

    private val fakeLogger = LoggerFactory.get("test")

    @Test
    fun testGetArtistAlbums() =
        runTest {
            val mockEngine =
                MockEngine { _ ->
                    respond(
                        content =
                            """
                            [{
                                "id": 50,
                                "artistId": 2,
                                "foreignAlbumId":
                                "foreign-123",
                                "anyReleaseOk": true,
                                "profileId": 1,
                                "duration": 3600,
                                "title": "Greatest Hits",
                                "monitored": true
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
                                ignoreUnknownKeys = true
                            },
                        )
                    }
                }
            val repository = LidarrRepository(fakeInstance, httpClient, fakeLogger)

            repository.getArtistAlbums(artistId = 2)

            assertNotNull(repository.artistAlbums.value[2])
            assertEquals(1, repository.artistAlbums.value[2]?.size)
            assertEquals(
                "Greatest Hits",
                repository.artistAlbums.value[2]
                    ?.first()
                    ?.title,
            )
        }
}
