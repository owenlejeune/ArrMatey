package com.dnfapps.arrmatey.downloadclient.api

import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.model.DownloadClientType
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

private const val HEADER_SESSION_ID = "X-Transmission-Session-Id"
private const val SESSION_ID = "test-session-id"

class TransmissionClientTest {
    private val fakeClient =
        DownloadClient(
            id = 1,
            type = DownloadClientType.Transmission,
            label = "Test Transmission",
            url = "http://localhost:9091",
            username = EncryptedString("user"),
            password = EncryptedString("pass"),
        )

    private fun httpClient(mockEngine: MockEngine) =
        HttpClient(mockEngine) {
            expectSuccess = true
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    },
                )
            }
        }

    @Test
    fun testConnectionRetriesWithSessionId() =
        runTest {
            val sessionIds = mutableListOf<String?>()
            val mockEngine =
                MockEngine { request ->
                    val sessionId = request.headers[HEADER_SESSION_ID]
                    sessionIds.add(sessionId)
                    if (sessionId == SESSION_ID) {
                        respond(
                            content = """{"result":"success","arguments":{}}""",
                            status = HttpStatusCode.OK,
                            headers = headersOf("Content-Type", "application/json"),
                        )
                    } else {
                        respond(
                            content = "409: Conflict",
                            status = HttpStatusCode.Conflict,
                            headers = headersOf(HEADER_SESSION_ID, SESSION_ID),
                        )
                    }
                }

            val result = TransmissionClient(fakeClient, httpClient(mockEngine)).testConnection()

            assertTrue(result is NetworkResult.Success, "Expected Success but got $result")
            assertEquals(listOf(null, SESSION_ID), sessionIds)
        }

    @Test
    fun testConnectionReturnsErrorOnUnauthorized() =
        runTest {
            val mockEngine =
                MockEngine { _ ->
                    respond(
                        content = "401: Unauthorized",
                        status = HttpStatusCode.Unauthorized,
                    )
                }

            val result = TransmissionClient(fakeClient, httpClient(mockEngine)).testConnection()

            assertTrue(result is NetworkResult.Error, "Expected Error but got $result")
            assertEquals(HttpStatusCode.Unauthorized.value, result.code)
        }
}
