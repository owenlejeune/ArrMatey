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

class RadarrRepositoryTest {
    private val fakeInstance =
        Instance(
            id = 2,
            label = "Test Radarr",
            url = "http://localhost:7878",
            apiKey = EncryptedString("test-api-key"),
            type = InstanceType.Radarr,
            enabled = true,
        )

    private val fakeLogger = LoggerFactory.get("test")

    @Test
    fun testGetMovieExtraFiles() =
        runTest {
            val mockEngine =
                MockEngine { _ ->
                    respond(
                        content =
                            """
                            [{
                                "id": 100,
                                "movieId": 5,
                                "movieFileId": 1,
                                "relativePath": "sub.srt",
                                "extension": ".srt",
                                "languageTags": [],
                                "type": "subtitle"
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
            val repository = RadarrRepository(fakeInstance, httpClient, fakeLogger)

            repository.getMovieExtraFiles(movieId = 5)

            assertNotNull(repository.movieExtraFiles.value[5])
            assertEquals(1, repository.movieExtraFiles.value[5]?.size)
        }
}
