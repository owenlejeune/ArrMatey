package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.networking.NetworkResult
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
import kotlin.test.assertTrue

class TracearrRepositoryTest {
    private val fakeInstance =
        Instance(
            id = 1,
            label = "Test Tracearr",
            url = "http://localhost:8080",
            apiKey = EncryptedString("test-api-key"),
            type = InstanceType.Tracearr,
            enabled = true,
        )

    private fun createHttpClient(handler: (requestUrl: String) -> String): HttpClient {
        val mockEngine = MockEngine { request ->
            val urlString = request.url.toString()
            respond(
                content = handler(urlString),
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", "application/json"),
            )
        }
        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    },
                )
            }
        }
    }

    @Test
    fun testGetPublicStreamsFetchesAndCachesMediaDetailsDeduplicated() = runTest {
        var mediaCallCount = 0
        val requestedMediaRefs = mutableListOf<String>()

        val httpClient = createHttpClient { url ->
            when {
                url.contains("/v2/public/streams") -> """
                    {
                      "data": [
                        {
                          "id": "session-1",
                          "media_id": "movie-1",
                          "show_media_id": null,
                          "media_title": "Movie One"
                        },
                        {
                          "id": "session-2",
                          "media_id": "movie-1",
                          "show_media_id": null,
                          "media_title": "Movie One Duplicate"
                        },
                        {
                          "id": "session-3",
                          "media_id": "ep-10",
                          "show_media_id": "show-100",
                          "media_title": "Episode Ten"
                        }
                      ]
                    }
                """.trimIndent()
                url.contains("/v2/public/media/") -> {
                    mediaCallCount++
                    val ref = url.substringAfter("/v2/public/media/")
                    requestedMediaRefs.add(ref)
                    """
                        {
                          "id": "$ref",
                          "title": "Title for $ref"
                        }
                    """.trimIndent()
                }
                else -> "{}"
            }
        }

        val repository = TracearrRepository(fakeInstance, httpClient)

        val result1 = repository.getPublicStreams()
        assertTrue(result1 is NetworkResult.Success)

        val streams1 = result1.data.data
        assertEquals(3, streams1.size)
        assertEquals("Title for movie-1", streams1[0].mediaDetails?.title)
        assertEquals("Title for movie-1", streams1[1].mediaDetails?.title)
        assertEquals("Title for show-100", streams1[2].mediaDetails?.title)

        assertEquals(2, mediaCallCount)
        assertEquals(listOf("movie-1", "show-100"), requestedMediaRefs)

        val result2 = repository.getPublicStreams()
        assertTrue(result2 is NetworkResult.Success)
        assertEquals(2, mediaCallCount)
    }
}
