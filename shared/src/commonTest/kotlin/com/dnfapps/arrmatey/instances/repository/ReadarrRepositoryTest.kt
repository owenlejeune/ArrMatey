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

class ReadarrRepositoryTest {
    private val fakeInstance =
        Instance(
            id = 4,
            label = "Test Readarr",
            url = "http://localhost:8787",
            apiKey = EncryptedString("test-api-key"),
            type = InstanceType.Bookshelf,
            enabled = true,
        )

    private val fakeLogger = LoggerFactory.get("test")

    @Test
    fun testGetAuthorSeries() = runTest {
        val mockEngine =
            MockEngine { _ ->
                respond(
                    content = """[{"id": 1, "title": "Foundation", "authorId": 10}]""",
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
        val repository = ReadarrRepository(fakeInstance, httpClient, fakeLogger)

        repository.getAuthorSeries(authorId = 10)

        assertNotNull(repository.authorSeries.value[10])
        assertEquals(1, repository.authorSeries.value[10]?.size)
        assertEquals(
            "Foundation",
            repository.authorSeries.value[10]
                ?.first()
                ?.title,
        )
    }

    @Test
    fun testChaptarrInstanceRepository() = runTest {
        val fakeChaptarrInstance =
            Instance(
                id = 5,
                label = "Test Chaptarr",
                url = "http://localhost:8789",
                apiKey = EncryptedString("test-chaptarr-api-key"),
                type = InstanceType.Chaptarr,
                enabled = true,
            )

        val mockEngine =
            MockEngine { _ ->
                respond(
                    content = """[{"id": 1, "title": "The Way of Kings", "authorId": 42, "mediaType": "audiobook"}]""",
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
        val repository = ReadarrRepository(fakeChaptarrInstance, httpClient, fakeLogger)

        assertEquals(InstanceType.Chaptarr, repository.instance.type)
        assertEquals("http://localhost:8789", repository.instance.url)
    }
}
