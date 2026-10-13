package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.tautulli.api.model.TautulliHomeStatId
import com.dnfapps.arrmatey.tautulli.api.model.TautulliStatsType
import com.dnfapps.networking.NetworkResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TautulliRepositoryTest {
    private val fakeInstance =
        Instance(
            id = 2,
            label = "Test Tautulli",
            url = "http://localhost:8181",
            apiKey = EncryptedString("my-tautulli-api-key"),
            type = InstanceType.Tautulli,
            enabled = true,
        )

    private fun createHttpClient(handler: suspend (request: HttpRequestData) -> String): HttpClient {
        val mockEngine =
            MockEngine { request ->
                respond(
                    content = handler(request),
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type", "application/json"),
                )
            }
        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    },
                )
            }
        }
    }

    @Test
    fun testConnectionSendsHeaderAuthAndStatusCmd() = runTest {
        var capturedApiKeyHeader: String? = null
        var capturedCmdParam: String? = null

        val httpClient =
            createHttpClient { request ->
                capturedApiKeyHeader = request.headers["X-Api-Key"]
                capturedCmdParam = request.url.parameters["cmd"]
                """
                {
                    "response": {
                        "result": "success",
                        "message": null,
                        "data": {
                            "tautulli_version": "v2.14.0",
                            "pms_name": "My Plex Server"
                        }
                    }
                }
                """.trimIndent()
            }

        val repository = TautulliRepository(fakeInstance, httpClient)
        val result = repository.testConnection()

        assertTrue(result is NetworkResult.Success)
        assertEquals("my-tautulli-api-key", capturedApiKeyHeader)
        assertEquals("status", capturedCmdParam)
    }

    @Test
    fun testRefreshStreamsPopulatesActivityAndActiveStreams() = runTest {
        val httpClient =
            createHttpClient { request ->
                when (request.url.parameters["cmd"]) {
                    "status" -> """
                    {
                        "response": {
                            "result": "success",
                            "message": null,
                            "data": {
                                "pms_name": "Breaking Bad PMS"
                            }
                        }
                    }
                    """.trimIndent()
                    "get_activity" -> """
                    {
                        "response": {
                            "result": "success",
                            "message": null,
                            "data": {
                                "stream_count": "1",
                                "total_bandwidth": 8500,
                                "lan_bandwidth": 0,
                                "wan_bandwidth": 8500,
                                "sessions": [
                                    {
                                        "session_id": "sess-123",
                                        "session_key": "42",
                                        "rating_key": "999",
                                        "title": "Pilot",
                                        "grandparent_title": "Breaking Bad",
                                        "media_type": "episode",
                                        "user": "jesse_pinkman",
                                        "friendly_name": "Jesse",
                                        "player": "Shield Android TV",
                                        "state": "playing",
                                        "transcode_decision": "transcode"
                                    }
                                ]
                            }
                        }
                    }
                    """.trimIndent()
                    else -> """{"response": {"result": "error", "message": "Unknown cmd", "data": null}}"""
                }
            }

        val repository = TautulliRepository(fakeInstance, httpClient)
        val result = repository.refreshStreams()

        assertTrue(result is NetworkResult.Success)
        assertEquals(1, repository.activeStreams.value.size)
        val session = repository.activeStreams.value.first()
        assertEquals("sess-123", session.id)
        assertEquals("Breaking Bad - Pilot", session.effectiveTitle)
        assertEquals("Jesse", session.effectiveUser)
        assertTrue(session.isTranscoding)
        assertEquals(1, repository.summaryStats.value?.activeUsersCount)
        assertEquals("Breaking Bad PMS", repository.streamSessions.value.first().serverName)
    }

    @Test
    fun testTerminateSessionSendsCommandAndMessage() = runTest {
        var capturedSessionId: String? = null
        var capturedMessage: String? = null

        val httpClient =
            createHttpClient { request ->
                val cmd = request.url.parameters["cmd"]
                if (cmd == "terminate_session") {
                    capturedSessionId = request.url.parameters["session_id"]
                    capturedMessage = request.url.parameters["message"]
                    """{"response": {"result": "success", "message": null, "data": {}}}"""
                } else {
                    """{"response": {"result": "success", "message": null, "data": {"sessions": []}}}"""
                }
            }

        val repository = TautulliRepository(fakeInstance, httpClient)
        val result = repository.terminateSession(sessionId = "sess-123", message = "Server maintenance")

        assertTrue(result is NetworkResult.Success)
        assertEquals("sess-123", capturedSessionId)
        assertEquals("Server maintenance", capturedMessage)
    }

    @Test
    fun testGetMetadataCachesResult() = runTest {
        var metadataCallCount = 0

        val httpClient =
            createHttpClient { request ->
                if (request.url.parameters["cmd"] == "get_metadata") {
                    metadataCallCount++
                    """
                    {
                        "response": {
                            "result": "success",
                            "message": null,
                            "data": {
                                "rating_key": "12345",
                                "title": "Inception",
                                "year": "2010",
                                "media_type": "movie"
                            }
                        }
                    }
                    """.trimIndent()
                } else {
                    """{"response": {"result": "error", "message": "Unknown cmd", "data": null}}"""
                }
            }

        val repository = TautulliRepository(fakeInstance, httpClient)

        val firstCall = repository.getMetadata("12345")
        assertTrue(firstCall is NetworkResult.Success)
        assertEquals("Inception", firstCall.data.title)
        assertEquals(1, metadataCallCount)

        val secondCall = repository.getMetadata("12345")
        assertTrue(secondCall is NetworkResult.Success)
        assertEquals("Inception", secondCall.data.title)
        assertEquals(1, metadataCallCount)
    }

    @Test
    fun testGetUsersAndWatchTimeStats() = runTest {
        val httpClient =
            createHttpClient { request ->
                when (request.url.parameters["cmd"]) {
                    "get_users" -> """
                    {
                        "response": {
                            "result": "success",
                            "message": null,
                            "data": [
                                {
                                    "user_id": 42,
                                    "username": "walterwhite",
                                    "friendly_name": "Heisenberg",
                                    "user_thumb": "https://plex.tv/avatar.jpg",
                                    "email": "walter@heisenberg.com",
                                    "is_active": 1,
                                    "is_admin": 1
                                }
                            ]
                        }
                    }
                    """.trimIndent()
                    "get_user_watch_time_stats" -> """
                    {
                        "response": {
                            "result": "success",
                            "message": null,
                            "data": [
                                {
                                    "user_id": 42,
                                    "total_time": 3600,
                                    "total_plays": 5
                                }
                            ]
                        }
                    }
                    """.trimIndent()
                    else -> """{"response": {"result": "error", "message": "Unknown cmd", "data": null}}"""
                }
            }

        val repository = TautulliRepository(fakeInstance, httpClient)
        val usersResult = repository.getUsers()
        assertTrue(usersResult is NetworkResult.Success)
        assertEquals(1, usersResult.data.size)
        val user = usersResult.data.first()
        assertEquals(42L, user.userId)
        assertEquals("Heisenberg", user.displayName)
        assertEquals("https://plex.tv/avatar.jpg", user.avatarUrl)

        val statsResult = repository.getUserWatchTimeStats(42L)
        assertTrue(statsResult is NetworkResult.Success)
        assertEquals(1, statsResult.data.size)
        assertEquals(5L, statsResult.data.first().totalPlays)
        assertEquals(3600L, statsResult.data.first().totalTime)

        val pmsUrl = repository.buildPmsImageUrl("library/metadata/123/thumb")
        assertTrue(pmsUrl != null && pmsUrl.contains("pms_image_proxy"))
    }

    @Test
    fun testRefreshHomeStatsWithEnumStatsType() = runTest {
        var capturedTimeRange: String? = null
        var capturedStatsType: String? = null

        val httpClient =
            createHttpClient { request ->
                if (request.url.parameters["cmd"] == "get_home_stats") {
                    capturedTimeRange = request.url.parameters["time_range"]
                    capturedStatsType = request.url.parameters["stats_type"]
                    """
                    {
                        "response": {
                            "result": "success",
                            "message": null,
                            "data": [
                                {
                                    "stat_id": "top_movies",
                                    "stat_title": "Top Movies",
                                    "stat_type": "plays",
                                    "rows": [
                                        {
                                            "title": "Inception",
                                            "total_plays": 12,
                                            "total_duration": 18000
                                        }
                                    ]
                                }
                            ]
                        }
                    }
                    """.trimIndent()
                } else {
                    """{"response": {"result": "error", "message": "Unknown cmd", "data": null}}"""
                }
            }

        val repository = TautulliRepository(fakeInstance, httpClient)
        val result = repository.refreshHomeStats(timeRange = 14, statsType = TautulliStatsType.Duration)

        assertTrue(result is NetworkResult.Success)
        assertEquals("14", capturedTimeRange)
        assertEquals("duration", capturedStatsType)
        assertEquals(1, result.data.size)
        assertEquals(TautulliHomeStatId.TopMovies, result.data.first().statId)
        assertEquals(1, repository.homeStats.value.size)
    }

    @Test
    fun testResolveServerNamePrecedence() = runTest {
        val httpClient = createHttpClient { """{"response": {"result": "success", "data": null}}""" }
        val repository = TautulliRepository(fakeInstance.copy(label = "Home Tautulli"), httpClient)

        assertEquals("Explicit Server", repository.resolveServerName("Explicit Server"))
        assertEquals("Home Tautulli", repository.resolveServerName())

        val defaultRepo = TautulliRepository(fakeInstance.copy(label = "Tautulli"), httpClient)
        assertEquals("Plex", defaultRepo.resolveServerName())
    }
}
