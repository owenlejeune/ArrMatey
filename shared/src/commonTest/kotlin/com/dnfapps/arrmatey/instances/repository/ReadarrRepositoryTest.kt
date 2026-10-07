package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.arr.api.client.BookshelfClient
import com.dnfapps.arrmatey.arr.api.model.ReleaseParams
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

    @Test
    fun testGetAuthorBooks() = runTest {
        val mockEngine =
            MockEngine { request ->
                assertEquals("http://localhost:8787/api/v1/book?authorId=10", request.url.toString())
                respond(
                    content = """[{"id": 101, "title": "Foundation", "authorId": 10}]""",
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

        val result = repository.getAuthorBooks(authorId = 10)

        assertNotNull(result)
        assertEquals(1, repository.booksLibrary.value.size)
        assertEquals(101L, repository.booksLibrary.value.first().id)
        assertEquals("Foundation", repository.booksLibrary.value.first().title)
    }

    @Test
    fun testGetReleasesReadarr() = runTest {
        val mockEngine =
            MockEngine { request ->
                assertEquals("http://localhost:8787/api/v1/release?bookId=1621", request.url.toString())
                respond(
                    content = """[{"guid": "g1", "title": "Release 1", "indexerId": 1, "protocol": "torrent"}]""",
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
        val client = BookshelfClient(fakeInstance, httpClient)
        val result = client.getReleases(ReleaseParams.Book(1621))

        assertNotNull(result)
        val data = (result as NetworkResult.Success).data
        assertEquals(1, data.size)
        assertEquals("Release 1", data.first().title)
    }

    @Test
    fun testGetReleasesChaptarr() = runTest {
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
            MockEngine { request ->
                assertEquals("http://localhost:8789/api/v1/release?bookId=1621", request.url.toString())
                respond(
                    content = """{
                        "releases": [{"guid": "g1", "title": "Approved Release", "indexerId": 3, "protocol": "torrent", "approved": true}],
                        "hiddenReleases": [{"guid": "g2", "title": "Hidden Release", "indexerId": 1, "protocol": "torrent", "rejected": true}]
                    }""",
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
        val client = com.dnfapps.arrmatey.arr.api.client.BookshelfClient(fakeChaptarrInstance, httpClient)
        val result = client.getReleases(com.dnfapps.arrmatey.arr.api.model.ReleaseParams.Book(1621))

        assertNotNull(result)
        val data = (result as com.dnfapps.networking.NetworkResult.Success).data
        assertEquals(1, data.size)
        assertEquals("Approved Release", data[0].title)
    }
}
