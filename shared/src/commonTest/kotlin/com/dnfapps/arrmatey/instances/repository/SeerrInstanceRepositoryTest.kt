package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.model.SeerrAuthType
import com.dnfapps.networking.NetworkResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SeerrInstanceRepositoryTest {
    private val fakeInstance =
        Instance(
            id = 1,
            label = "Test Seerr",
            url = "http://localhost:5055",
            apiKey = EncryptedString("test-api-key"),
            type = InstanceType.Seerr,
            seerrAuthType = SeerrAuthType.ApiKey,
            enabled = true,
        )

    private fun httpClient(mockEngine: MockEngine) = HttpClient(mockEngine) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                },
            )
        }
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
    }

    @Test
    fun testGetLoggedInUser() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = """{"id": 1, "displayName": "Admin", "email": "admin@test.com", "permissions": 0, "userType": 1}""",
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", "application/json"),
            )
        }
        val repository = SeerrInstanceRepository(fakeInstance, httpClient(mockEngine))

        repository.getLoggedInUser()

        assertNotNull(repository.loggedInUser.value)
        assertEquals(1, repository.loggedInUser.value?.id)
        assertEquals("Admin", repository.loggedInUser.value?.displayName)
    }

    @Test
    fun testLoginLocal() = runTest {
        val mockEngine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/api/v1/auth/local" -> respond(
                    content = """{"id": 42, "displayName": "Local User", "email": "user@test.com", "permissions": 32, "userType": 1}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(
                        "Content-Type" to listOf("application/json"),
                        "Set-Cookie" to listOf("connect.sid=s%3Alocal-session-cookie; Path=/; HttpOnly"),
                    ),
                )
                "/api/v1/auth/me" -> respond(
                    content = """{"id": 42, "displayName": "Local User", "email": "user@test.com", "permissions": 32, "userType": 1}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type" to listOf("application/json")),
                )
                else -> error("Unexpected url: ${request.url.encodedPath}")
            }
        }
        val repository = SeerrInstanceRepository(fakeInstance, httpClient(mockEngine))

        val result = repository.loginLocal("user@test.com", "secret")
        assertTrue(result is NetworkResult.Success)
        assertEquals(42, result.data.user.id)
        assertEquals("Local User", result.data.user.displayName)
        assertEquals("s%3Alocal-session-cookie", result.data.sessionToken)
        assertEquals(42, repository.loggedInUser.value?.id)
    }

    @Test
    fun testLoginPlex() = runTest {
        val mockEngine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/api/v1/auth/plex" -> respond(
                    content = """{"id": 99, "displayName": "Plex User", "plexUsername": "plex_master", "permissions": 32, "userType": 2}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(
                        "Content-Type" to listOf("application/json"),
                        "Set-Cookie" to listOf("connect.sid=s%3Aplex-session-cookie; Path=/; HttpOnly"),
                    ),
                )
                "/api/v1/auth/me" -> respond(
                    content = """{"id": 99, "displayName": "Plex User", "plexUsername": "plex_master", "permissions": 32, "userType": 2}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type" to listOf("application/json")),
                )
                else -> error("Unexpected url: ${request.url.encodedPath}")
            }
        }
        val repository = SeerrInstanceRepository(fakeInstance, httpClient(mockEngine))

        val result = repository.loginPlex("valid-plex-token")
        assertTrue(result is NetworkResult.Success)
        assertEquals(99, result.data.user.id)
        assertEquals("Plex User", result.data.user.displayName)
        assertEquals("s%3Aplex-session-cookie", result.data.sessionToken)
        assertEquals(99, repository.loggedInUser.value?.id)
    }

    @Test
    fun testLoginJellyfin() = runTest {
        val mockEngine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/api/v1/auth/jellyfin" -> respond(
                    content = """{"id": 105, "displayName": "Jellyfin User", "jellyfinUsername": "jf_fan", "permissions": 32, "userType": 3}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(
                        "Content-Type" to listOf("application/json"),
                        "Set-Cookie" to listOf("connect.sid=s%3Ajf-session-cookie; Path=/; HttpOnly"),
                    ),
                )
                "/api/v1/auth/me" -> respond(
                    content = """{"id": 105, "displayName": "Jellyfin User", "jellyfinUsername": "jf_fan", "permissions": 32, "userType": 3}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type" to listOf("application/json")),
                )
                else -> error("Unexpected url: ${request.url.encodedPath}")
            }
        }
        val repository = SeerrInstanceRepository(fakeInstance, httpClient(mockEngine))

        val result = repository.loginJellyfin("jf_fan", "jf_pass")
        assertTrue(result is NetworkResult.Success)
        assertEquals(105, result.data.user.id)
        assertEquals("Jellyfin User", result.data.user.displayName)
        assertEquals("s%3Ajf-session-cookie", result.data.sessionToken)
        assertEquals(105, repository.loggedInUser.value?.id)
    }

    @Test
    fun testJellyfinQuickConnectFlow() = runTest {
        val mockEngine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/api/v1/auth/jellyfin/quickconnect/initiate" -> {
                    respond(
                        content = """{"code": "123456", "secret": "sec-abc-123"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", "application/json"),
                    )
                }
                "/api/v1/auth/jellyfin/quickconnect/check" -> {
                    assertEquals("sec-abc-123", request.url.parameters["secret"])
                    respond(
                        content = """{"authenticated": true}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", "application/json"),
                    )
                }
                "/api/v1/auth/jellyfin/quickconnect/authenticate" -> {
                    respond(
                        content = """{"id": 200, "displayName": "QC User", "permissions": 32, "userType": 3}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(
                            "Content-Type" to listOf("application/json"),
                            "Set-Cookie" to listOf("connect.sid=s%3Aqc-session-cookie; Path=/; HttpOnly"),
                        ),
                    )
                }
                "/api/v1/auth/me" -> {
                    respond(
                        content = """{"id": 200, "displayName": "QC User", "permissions": 32, "userType": 3}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type" to listOf("application/json")),
                    )
                }
                else -> error("Unexpected url: ${request.url.encodedPath}")
            }
        }
        val repository = SeerrInstanceRepository(fakeInstance, httpClient(mockEngine))

        val initiate = repository.initiateJellyfinQuickConnect()
        assertTrue(initiate is NetworkResult.Success)
        assertEquals("123456", initiate.data.code)
        assertEquals("sec-abc-123", initiate.data.secret)

        val check = repository.checkJellyfinQuickConnect("sec-abc-123")
        assertTrue(check is NetworkResult.Success)
        assertTrue(check.data.authenticated)

        val auth = repository.authenticateJellyfinQuickConnect("sec-abc-123")
        assertTrue(auth is NetworkResult.Success)
        assertEquals(200, auth.data.user.id)
        assertEquals("s%3Aqc-session-cookie", auth.data.sessionToken)
        assertEquals(200, repository.loggedInUser.value?.id)
    }

    @Test
    fun testLogout() = runTest {
        val mockEngine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/api/v1/auth/me" -> {
                    respond(
                        content = """{"id": 1, "displayName": "Admin", "permissions": 0, "userType": 1}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", "application/json"),
                    )
                }
                "/api/v1/auth/logout" -> {
                    respond(
                        content = "{}",
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", "application/json"),
                    )
                }
                else -> error("Unexpected url: ${request.url.encodedPath}")
            }
        }
        val repository = SeerrInstanceRepository(fakeInstance, httpClient(mockEngine))

        repository.getLoggedInUser()
        assertNotNull(repository.loggedInUser.value)

        val logoutResult = repository.logout()
        assertTrue(logoutResult is NetworkResult.Success)
        assertNull(repository.loggedInUser.value)
    }

    @Test
    fun testRefreshCountsRestrictsUserWithoutViewAllOrIssues() = runTest {
        var requestedByParam: String? = null
        var issueApiCalled = false
        val mockEngine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/api/v1/auth/me" -> respond(
                    content = """{"id": 2, "displayName": "testlocal", "permissions": 32, "userType": 2}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type", "application/json"),
                )
                "/api/v1/request" -> {
                    requestedByParam = request.url.parameters["requestedBy"]
                    respond(
                        content = """{"pageInfo": {"page": 1, "pages": 1, "pageSize": 50, "results": 1}, "results": [{"id": 100, "status": 1, "type": "movie", "media": {"id": 10, "status": 2, "tmdbId": 123}, "requestedBy": {"id": 2, "displayName": "testlocal"}}]}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", "application/json"),
                    )
                }
                "/api/v1/issue", "/api/v1/issue/count" -> {
                    issueApiCalled = true
                    respond(
                        content = """{"open": 5}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", "application/json"),
                    )
                }
                else -> error("Unexpected url: ${request.url.encodedPath}")
            }
        }
        val repository = SeerrInstanceRepository(fakeInstance, httpClient(mockEngine))

        repository.refreshCounts()

        assertEquals("2", requestedByParam)
        kotlin.test.assertFalse(issueApiCalled)
        assertEquals(0, repository.openIssuesCount.value)
        assertTrue(repository.openIssues.value.isEmpty())
        assertEquals(1, repository.pendingRequestsCount.value)
        assertEquals(1, repository.requests.value.size)
    }
}
