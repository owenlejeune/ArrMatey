package com.dnfapps.arrmatey.mediaserver.usecase

import com.dnfapps.arrmatey.arr.api.client.HttpClientFactory
import com.dnfapps.networking.NetworkResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logger
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JellyfinQuickConnectUseCaseTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val silentLogger = object : Logger {
        override fun log(message: String) = Unit
    }

    private class MockHttpClientFactory(
        private val json: Json,
        private val mockEngine: MockEngine,
        logger: Logger,
    ) : HttpClientFactory(json, logger) {
        override fun createGeneric(): HttpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(this@MockHttpClientFactory.json)
            }
        }
    }

    @Test
    fun initiateReturnsCodeAndSecret() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/QuickConnect/Initiate", request.url.encodedPath)
            respond(
                content = """{ "Secret": "sec-123", "Code": "482910" }""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val useCase = JellyfinQuickConnectUseCase(MockHttpClientFactory(json, mockEngine, silentLogger))
        val result = useCase.initiate("http://localhost:8096", "client-uuid")

        assertTrue(result is NetworkResult.Success)
        assertEquals("482910", result.data.code)
        assertEquals("sec-123", result.data.secret)
    }

    @Test
    fun checkReturnsAuthenticatedStatus() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/QuickConnect/Connect", request.url.encodedPath)
            assertEquals("sec-123", request.url.parameters["secret"])
            respond(
                content = """{ "Authenticated": true, "Secret": "sec-123" }""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val useCase = JellyfinQuickConnectUseCase(MockHttpClientFactory(json, mockEngine, silentLogger))
        val result = useCase.check("http://localhost:8096", "sec-123", "client-uuid")

        assertTrue(result is NetworkResult.Success)
        assertTrue(result.data.authenticated)
    }

    @Test
    fun authenticateExchangesSecretForUserSession() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/Users/AuthenticateWithQuickConnect", request.url.encodedPath)
            respond(
                content = """
                    {
                      "AccessToken": "jf-access-token-999",
                      "User": { "Id": "usr-1", "Name": "Admin" },
                      "ServerId": "srv-1"
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val useCase = JellyfinQuickConnectUseCase(MockHttpClientFactory(json, mockEngine, silentLogger))
        val result = useCase.authenticate("http://localhost:8096", "sec-123", "client-uuid")

        assertTrue(result is NetworkResult.Success)
        assertEquals("jf-access-token-999", result.data.accessToken)
        assertEquals("usr-1", result.data.user.id)
        assertEquals("Admin", result.data.user.name)
        assertEquals("srv-1", result.data.serverId)
    }
}
