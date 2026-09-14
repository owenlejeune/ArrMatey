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

class SABnzbdClientTest {
    private val queueResponseBody = """{"queue":{"paused":false,"slots":[]}}"""

    private fun client(apiKey: String = "abc123") =
        DownloadClient(
            id = 1,
            type = DownloadClientType.SABnzbd,
            label = "sab",
            url = "http://localhost:8080",
            apiKey = EncryptedString(apiKey),
        )

    private fun httpClient(mockEngine: MockEngine) =
        HttpClient(mockEngine) {
            expectSuccess = true
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

    @Test
    fun testConnectionSendsCurrentApiKey() =
        runTest {
            var seenKey: String? = null
            val mockEngine =
                MockEngine { request ->
                    seenKey = request.url.parameters["apikey"]
                    respond(
                        content = queueResponseBody,
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", "application/json"),
                    )
                }

            val result = SABnzbdClient(client(apiKey = "abc123"), httpClient(mockEngine)).testConnection()

            assertTrue(result is NetworkResult.Success)
            assertEquals("abc123", seenKey)
        }

    @Test
    fun testConnectionEachCallPicksUpUpdatedApiKey() =
        runTest {
            // Stateless api-key auth — no cache to invalidate on edit.
            val seenKeys = mutableListOf<String?>()
            val mockEngine =
                MockEngine { request ->
                    seenKeys.add(request.url.parameters["apikey"])
                    respond(
                        content = queueResponseBody,
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", "application/json"),
                    )
                }

            val engine = httpClient(mockEngine)
            SABnzbdClient(client(apiKey = "old"), engine).testConnection()
            SABnzbdClient(client(apiKey = "new"), engine).testConnection()

            assertEquals(listOf<String?>("old", "new"), seenKeys)
        }

    @Test
    fun testConnectionReturnsErrorOnUnauthorized() =
        runTest {
            val mockEngine =
                MockEngine { _ ->
                    respond(content = "unauthorized", status = HttpStatusCode.Unauthorized)
                }

            val result = SABnzbdClient(client(), httpClient(mockEngine)).testConnection()

            assertTrue(result is NetworkResult.Error)
            assertEquals(HttpStatusCode.Unauthorized.value, result.code)
        }
}
