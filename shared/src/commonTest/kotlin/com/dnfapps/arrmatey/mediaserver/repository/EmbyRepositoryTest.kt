package com.dnfapps.arrmatey.mediaserver.repository

import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.mediaserver.client.EmbyClient
import com.dnfapps.arrmatey.mediaserver.model.MediaServerIntegration
import com.dnfapps.arrmatey.mediaserver.model.MediaServerType
import com.dnfapps.arrmatey.mediaserver.model.MediaType
import com.dnfapps.networking.NetworkResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmbyRepositoryTest {
    private val fakeIntegration = MediaServerIntegration(
        id = 3,
        serverType = MediaServerType.Emby,
        label = "Home Emby",
        url = "http://192.168.1.70:8096",
        token = EncryptedString("emby-token-789"),
        userId = "user-id-emby",
        clientIdentifier = "emby-client-uuid",
        serverId = "emby-server-uuid",
    )

    private fun createClient(mockEngine: MockEngine): EmbyClient {
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        return EmbyClient(fakeIntegration, httpClient)
    }

    @Test
    fun getLibrariesParsesViewsCorrectly() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/Users/user-id-emby/Views", request.url.encodedPath)
            assertEquals("emby-token-789", request.headers["X-Emby-Token"])
            respond(
                content = """
                    {
                      "Items": [
                        { "Id": "emby-lib-1", "Name": "Movies", "Type": "CollectionFolder", "CollectionType": "movies" }
                      ],
                      "TotalRecordCount": 1
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val repository = EmbyRepository(createClient(mockEngine))
        val result = repository.getLibraries()

        assertTrue(result is NetworkResult.Success)
        val libs = result.data
        assertEquals(1, libs.size)
        assertEquals("emby-lib-1", libs[0].id)
        assertEquals("Movies", libs[0].title)
        assertEquals(MediaType.MOVIE, libs[0].type)
        assertEquals(MediaServerType.Emby, libs[0].serverType)
    }

    @Test
    fun buildDeepLinkUrlMatchesEmbyScheme() {
        val repository = EmbyRepository(createClient(MockEngine { respond("") }))
        val deepLink = repository.buildDeepLinkUrl(
            com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaItem(
                id = "item-emby-1",
                title = "Test",
                type = MediaType.MOVIE,
                serverType = MediaServerType.Emby,
            ),
        )
        assertEquals("emby://open?itemId=item-emby-1&serverId=emby-server-uuid", deepLink)
    }
}
