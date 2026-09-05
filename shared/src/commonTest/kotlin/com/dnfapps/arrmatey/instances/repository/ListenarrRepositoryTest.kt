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

class ListenarrRepositoryTest {
    private val fakeInstance =
        Instance(
            id = 5,
            label = "Test Listenarr",
            url = "http://localhost:8080",
            apiKey = EncryptedString("test-api-key"),
            type = InstanceType.Listenarr,
            enabled = true,
        )

    private val fakeLogger = LoggerFactory.get("test")

    @Test
    fun testGetAudiobookFiles() =
        runTest {
            val mockEngine =
                MockEngine { _ ->
                    respond(
                        content =
                            """
                            {
                                "id": 3,
                                "title": "Audiobook Title",
                                "files": [
                                    {
                                        "id": 30,
                                        "audiobookId": 3,
                                        "path": "/path/to/file.m4b"
                                    }
                                ]
                            }
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
            val repository = ListenarrRepository(fakeInstance, httpClient, fakeLogger)

            repository.getAudiobookFiles(audiobookId = 3)

            assertNotNull(repository.audiobookFiles.value[3])
            assertEquals(1, repository.audiobookFiles.value[3]?.size)
        }
}
