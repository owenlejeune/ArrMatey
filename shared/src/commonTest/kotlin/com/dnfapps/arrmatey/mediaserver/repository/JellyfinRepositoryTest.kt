package com.dnfapps.arrmatey.mediaserver.repository

import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.mediaserver.client.JellyfinClient
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

class JellyfinRepositoryTest {
    private val fakeIntegration = MediaServerIntegration(
        id = 2,
        serverType = MediaServerType.Jellyfin,
        label = "Home Jellyfin",
        url = "http://192.168.1.60:8096",
        token = EncryptedString("jellyfin-token-456"),
        userId = "user-id-abc",
        clientIdentifier = "jf-client-uuid",
        serverId = "jf-server-uuid",
    )

    private fun createClient(mockEngine: MockEngine): JellyfinClient {
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        return JellyfinClient(fakeIntegration, httpClient)
    }

    @Test
    fun getLibrariesParsesViewsCorrectly() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/Users/user-id-abc/Views", request.url.encodedPath)
            assertTrue(request.headers["Authorization"]?.contains("Token=\"jellyfin-token-456\"") == true)
            respond(
                content = """
                    {
                      "Items": [
                        { "Id": "lib-1", "Name": "Movies", "Type": "CollectionFolder", "CollectionType": "movies" },
                        { "Id": "lib-2", "Name": "Shows", "Type": "CollectionFolder", "CollectionType": "tvshows" }
                      ],
                      "TotalRecordCount": 2
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val repository = JellyfinRepository(createClient(mockEngine))
        val result = repository.getLibraries()

        assertTrue(result is NetworkResult.Success)
        val libs = result.data
        assertEquals(2, libs.size)
        assertEquals("lib-1", libs[0].id)
        assertEquals("Movies", libs[0].title)
        assertEquals(MediaType.MOVIE, libs[0].type)
        assertEquals("lib-2", libs[1].id)
        assertEquals(MediaType.SHOW, libs[1].type)
    }

    @Test
    fun getLibraryContentsExtractsProviderIdsAndPlaybackState() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/Users/user-id-abc/Items", request.url.encodedPath)
            respond(
                content = """
                    {
                      "Items": [
                        {
                          "Id": "item-101",
                          "Name": "Interstellar",
                          "Type": "Movie",
                          "ProductionYear": 2014,
                          "Overview": "Mankind was born on Earth...",
                          "RunTimeTicks": 101400000000,
                          "UserData": {
                            "PlaybackPositionTicks": 50700000000,
                            "Played": false
                          },
                          "ProviderIds": {
                            "Tmdb": "157336",
                            "Imdb": "tt0816692"
                          },
                          "MediaSources": [
                            {
                              "MediaStreams": [
                                { "Type": "Video", "Codec": "hevc", "Width": 3840, "Height": 2160 },
                                { "Type": "Audio", "Codec": "truehd" }
                              ]
                            }
                          ]
                        }
                      ],
                      "TotalRecordCount": 1
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val repository = JellyfinRepository(createClient(mockEngine))
        val result = repository.getLibraryContents("lib-1")

        assertTrue(result is NetworkResult.Success)
        val items = result.data
        assertEquals(1, items.size)
        val item = items.first()
        assertEquals("item-101", item.id)
        assertEquals("Interstellar", item.title)
        assertEquals(157336L, item.tmdbId)
        assertEquals("tt0816692", item.imdbId)
        assertEquals(0.5f, item.progressPercentage)
        assertEquals("3840x2160", item.videoResolution)
        assertEquals("hevc", item.videoCodec)
        assertEquals("truehd", item.audioCodec)
    }

    @Test
    fun buildDeepLinkUrlMatchesJellyfinScheme() {
        val repository = JellyfinRepository(createClient(MockEngine { respond("") }))
        val deepLink = repository.buildDeepLinkUrl(
            com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaItem(
                id = "item-999",
                title = "Test",
                type = MediaType.MOVIE,
                serverType = MediaServerType.Jellyfin,
            ),
        )
        assertEquals("jellyfin://open?itemId=item-999&serverId=jf-server-uuid", deepLink)
    }
}
