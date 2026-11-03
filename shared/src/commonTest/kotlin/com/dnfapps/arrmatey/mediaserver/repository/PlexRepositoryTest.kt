package com.dnfapps.arrmatey.mediaserver.repository

import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.mediaserver.client.PlexClient
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlexRepositoryTest {
    private val fakeIntegration = MediaServerIntegration(
        id = 1,
        serverType = MediaServerType.Plex,
        label = "Home Plex",
        url = "http://192.168.1.50:32400",
        token = EncryptedString("plex-token-123"),
        clientIdentifier = "test-client-uuid",
        serverId = "server-uuid-999",
    )

    private fun createClient(mockEngine: MockEngine): PlexClient {
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        return PlexClient(fakeIntegration, httpClient)
    }

    @Test
    fun getLibrariesParsesSectionsCorrectly() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/library/sections", request.url.encodedPath)
            assertEquals("plex-token-123", request.headers["X-Plex-Token"])
            respond(
                content = """
                    {
                      "MediaContainer": {
                        "size": 2,
                        "Directory": [
                          { "key": "1", "title": "Movies", "type": "movie" },
                          { "key": "2", "title": "TV Shows", "type": "show" }
                        ]
                      }
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val repository = PlexRepository(createClient(mockEngine))
        val result = repository.getLibraries()

        assertTrue(result is NetworkResult.Success)
        val libs = result.data
        assertEquals(2, libs.size)
        assertEquals("1", libs[0].id)
        assertEquals("Movies", libs[0].title)
        assertEquals(MediaType.MOVIE, libs[0].type)
        assertEquals("2", libs[1].id)
        assertEquals(MediaType.SHOW, libs[1].type)
    }

    @Test
    fun getLibraryContentsExtractsGuidsAndProgress() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/library/sections/1/all", request.url.encodedPath)
            respond(
                content = """
                    {
                      "MediaContainer": {
                        "size": 1,
                        "Metadata": [
                          {
                            "ratingKey": "101",
                            "title": "Inception",
                            "type": "movie",
                            "year": 2010,
                            "summary": "Dream within a dream",
                            "thumb": "/library/metadata/101/thumb",
                            "viewOffset": 3600000,
                            "duration": 7200000,
                            "viewCount": 1,
                            "rating": 8.8,
                            "Guid": [
                              { "id": "tmdb://27205" },
                              { "id": "imdb://tt1375666" }
                            ],
                            "Media": [
                              {
                                "videoResolution": "1080",
                                "videoCodec": "hevc",
                                "audioCodec": "dts"
                              }
                            ]
                          }
                        ]
                      }
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val repository = PlexRepository(createClient(mockEngine))
        val result = repository.getLibraryContents("1", page = 0, pageSize = 50)

        assertTrue(result is NetworkResult.Success)
        val items = result.data
        assertEquals(1, items.size)
        val item = items.first()
        assertEquals("101", item.id)
        assertEquals("Inception", item.title)
        assertEquals(2010, item.year)
        assertEquals(27205L, item.tmdbId)
        assertEquals("tt1375666", item.imdbId)
        assertEquals(36_000_000_000L, item.playbackPositionTicks) // 3600000 ms * 10,000
        assertEquals(72_000_000_000L, item.durationTicks)
        assertEquals(0.5f, item.progressPercentage)
        assertTrue(item.played)
        assertEquals("hevc", item.videoCodec)
        assertEquals("dts", item.audioCodec)
    }

    @Test
    fun getContinueWatchingParsesOnDeck() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/library/onDeck", request.url.encodedPath)
            respond(
                content = """
                    {
                      "MediaContainer": {
                        "size": 1,
                        "Metadata": [
                          {
                            "ratingKey": "505",
                            "title": "Severance - Episode 1",
                            "type": "episode",
                            "parentTitle": "Season 1",
                            "grandparentTitle": "Severance",
                            "index": 1,
                            "parentIndex": 1,
                            "viewOffset": 1200000,
                            "duration": 3000000
                          }
                        ]
                      }
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val repository = PlexRepository(createClient(mockEngine))
        val result = repository.getContinueWatching(limit = 10)

        assertTrue(result is NetworkResult.Success)
        val items = result.data
        assertEquals(1, items.size)
        assertEquals("505", items[0].id)
        assertEquals("Severance", items[0].seriesTitle)
        assertEquals(1, items[0].episodeNumber)
        assertEquals(1, items[0].seasonNumber)
    }

    @Test
    fun buildDeepLinkUrlMatchesPlexScheme() {
        val repository = PlexRepository(createClient(MockEngine { respond("") }))
        val deepLink = repository.buildDeepLinkUrl(
            com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaItem(
                id = "12345",
                title = "Test",
                type = MediaType.MOVIE,
                serverType = MediaServerType.Plex,
            ),
        )
        assertEquals("plex://preplay/?metadataKey=%2Flibrary%2Fmetadata%2F12345&server=server-uuid-999", deepLink)
    }

    @Test
    fun handlesNullOrMissingMetadataSafely() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = """
                    {
                      "MediaContainer": {
                        "size": 0
                      }
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val repository = PlexRepository(createClient(mockEngine))
        val result = repository.getLibraryContents("1", page = 0, pageSize = 50)

        assertTrue(result is NetworkResult.Success)
        assertTrue(result.data.isEmpty())
    }

    @Test
    fun parsesLegacyGuidsAndNullFields() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = """
                    {
                      "MediaContainer": {
                        "size": 1,
                        "Metadata": [
                          {
                            "ratingKey": "999",
                            "title": "Legacy Movie",
                            "type": "movie",
                            "guid": "com.plexapp.agents.themoviedb://550?lang=en"
                          }
                        ]
                      }
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val repository = PlexRepository(createClient(mockEngine))
        val result = repository.getLibraryContents("1", page = 0, pageSize = 50)

        assertTrue(result is NetworkResult.Success)
        val item = result.data.first()
        assertEquals("999", item.id)
        assertEquals("Legacy Movie", item.title)
        assertEquals(550L, item.tmdbId)
    }

    @Test
    fun getAllLibraryContentsFetchesWithoutContainerLimits() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/library/sections/1/all", request.url.encodedPath)
            assertNull(request.headers["X-Plex-Container-Start"])
            assertNull(request.headers["X-Plex-Container-Size"])
            respond(
                content = """
                    {
                      "MediaContainer": {
                        "size": 2,
                        "totalSize": 2,
                        "Metadata": [
                          { "ratingKey": "1", "title": "Movie 1", "type": "movie" },
                          { "ratingKey": "2", "title": "Movie 2", "type": "movie" }
                        ]
                      }
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val repository = PlexRepository(createClient(mockEngine))
        val result = repository.getAllLibraryContents("1")

        assertTrue(result is NetworkResult.Success)
        assertEquals(2, result.data.size)
        assertEquals("Movie 1", result.data[0].title)
        assertEquals("Movie 2", result.data[1].title)
    }

    @Test
    fun getAllLibraryContentsPaginatesWhenServerCapsResults() = runTest {
        val mockEngine = MockEngine { request ->
            val start = request.headers["X-Plex-Container-Start"]
            if (start == null) {
                // First request without pagination headers returns first item and totalSize = 2
                respond(
                    content = """
                        {
                          "MediaContainer": {
                            "size": 1,
                            "totalSize": 2,
                            "Metadata": [
                              { "ratingKey": "1", "title": "Movie 1", "type": "movie" }
                            ]
                          }
                        }
                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            } else {
                // Follow up batch
                assertEquals("1", start)
                respond(
                    content = """
                        {
                          "MediaContainer": {
                            "size": 1,
                            "totalSize": 2,
                            "Metadata": [
                              { "ratingKey": "2", "title": "Movie 2", "type": "movie" }
                            ]
                          }
                        }
                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        }

        val repository = PlexRepository(createClient(mockEngine))
        val result = repository.getAllLibraryContents("1")

        assertTrue(result is NetworkResult.Success)
        assertEquals(2, result.data.size)
        assertEquals("Movie 1", result.data[0].title)
        assertEquals("Movie 2", result.data[1].title)
    }

    @Test
    fun searchMapsEpisodeWithSeriesPosterAndMetadata() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/search", request.url.encodedPath)
            assertEquals("breaking", request.url.parameters["query"])
            respond(
                content = """
                    {
                      "MediaContainer": {
                        "size": 1,
                        "Metadata": [
                          {
                            "ratingKey": "1001",
                            "title": "Pilot",
                            "type": "episode",
                            "grandparentTitle": "Breaking Bad",
                            "grandparentRatingKey": "500",
                            "grandparentThumb": "/library/metadata/500/thumb",
                            "thumb": "/library/metadata/1001/thumb",
                            "parentIndex": 1,
                            "index": 1,
                            "studio": "AMC",
                            "originallyAvailableAt": "2008-01-20",
                            "duration": 3480000
                          }
                        ]
                      }
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val repository = PlexRepository(createClient(mockEngine))
        val result = repository.search("breaking")

        assertTrue(result is NetworkResult.Success)
        val items = result.data
        assertEquals(1, items.size)
        val ep = items.first()
        assertEquals("Pilot", ep.title)
        assertEquals("Breaking Bad", ep.seriesTitle)
        assertEquals(1, ep.seasonNumber)
        assertEquals(1, ep.episodeNumber)
        assertEquals("AMC", ep.studio)
        assertEquals("2008-01-20", ep.releaseDate)
        assertEquals("Jan 20, 2008", ep.formattedReleaseDate)
        assertEquals("58m", ep.formattedRuntime)
        assertTrue(ep.posterUrl?.contains("/library/metadata/500/thumb") == true)
        assertTrue(ep.seriesPosterUrl?.contains("/library/metadata/500/thumb") == true)
    }

    @Test
    fun searchMapsMovieWithStudioAndRuntime() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/search", request.url.encodedPath)
            assertEquals("inception", request.url.parameters["query"])
            respond(
                content = """
                    {
                      "MediaContainer": {
                        "size": 1,
                        "Metadata": [
                          {
                            "ratingKey": "2001",
                            "title": "Inception",
                            "type": "movie",
                            "year": 2010,
                            "studio": "Warner Bros. Pictures",
                            "duration": 8880000
                          }
                        ]
                      }
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val repository = PlexRepository(createClient(mockEngine))
        val result = repository.search("inception")

        assertTrue(result is NetworkResult.Success)
        val items = result.data
        assertEquals(1, items.size)
        val movie = items.first()
        assertEquals("Inception", movie.title)
        assertEquals(2010, movie.year)
        assertEquals("Warner Bros. Pictures", movie.studio)
        assertEquals("2h 28m", movie.formattedRuntime)
    }
}
