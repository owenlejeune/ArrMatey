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
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DelugeClientTest {
    private fun client(password: String = "secret") = DownloadClient(
        id = 1,
        type = DownloadClientType.Deluge,
        label = "deluge",
        url = "http://localhost:8112",
        password = EncryptedString(password),
    )

    private fun httpClient(mockEngine: MockEngine) = HttpClient(mockEngine) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    @Test
    fun testConnectionAlwaysReAuthenticates() = runTest {
        // testConnection resets the auth flag so an edited password takes effect immediately.
        val methods = mutableListOf<String>()
        val mockEngine =
            MockEngine { request ->
                val body = (request.body as? TextContent)?.text.orEmpty()
                if ("\"method\":\"auth.login\"" in body) methods.add("auth.login")
                respond(
                    content = """{"id":1,"result":true,"error":null}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type", "application/json"),
                )
            }

        val deluge = DelugeClient(client(), httpClient(mockEngine))
        deluge.testConnection()
        deluge.testConnection()

        assertEquals(listOf("auth.login", "auth.login"), methods)
    }

    @Test
    fun testConnectionReturnsErrorWhenLoginRejected() = runTest {
        val mockEngine =
            MockEngine { _ ->
                respond(
                    content = """{"id":1,"result":false,"error":null}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type", "application/json"),
                )
            }

        val result = DelugeClient(client(), httpClient(mockEngine)).testConnection()

        assertTrue(result is NetworkResult.Error)
        assertEquals(401, result.code)
    }

    @Test
    fun getTransferInfoParsesFloatingPointRatesCorrectly() = runTest {
        val mockEngine =
            MockEngine { request ->
                val body = (request.body as? TextContent)?.text.orEmpty()
                if ("\"method\":\"auth.login\"" in body) {
                    respond(
                        content = """{"id":1,"result":true,"error":null}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", "application/json"),
                    )
                } else {
                    respond(
                        content = """{"id":2,"result":{"download_rate":102450.5,"upload_rate":0.0},"error":null}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", "application/json"),
                    )
                }
            }

        val deluge = DelugeClient(client(), httpClient(mockEngine))
        val result = deluge.getTransferInfo()

        assertTrue(result is NetworkResult.Success)
        assertEquals(102450L, result.data.downloadSpeed)
        assertEquals(0L, result.data.uploadSpeed)
    }

    @Test
    fun getDownloadsParsesTorrentsCorrectly() = runTest {
        val mockEngine =
            MockEngine { request ->
                val body = (request.body as? TextContent)?.text.orEmpty()
                if ("\"method\":\"auth.login\"" in body) {
                    respond(
                        content = """{"id":1,"result":true,"error":null}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", "application/json"),
                    )
                } else {
                    respond(
                        content = """
                        {
                            "id": 2,
                            "result": {
                                "hash123": {
                                    "name": "Ubuntu.iso",
                                    "total_size": 2147483648,
                                    "progress": 50.0,
                                    "download_payload_rate": 512000.0,
                                    "upload_payload_rate": 0.0,
                                    "eta": 3600,
                                    "state": "Downloading",
                                    "label": "linux",
                                    "time_added": 1700000000,
                                    "hash": "hash123"
                                }
                            },
                            "error": null
                        }
                        """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", "application/json"),
                    )
                }
            }

        val deluge = DelugeClient(client(), httpClient(mockEngine))
        val result = deluge.getDownloads()

        assertTrue(result is NetworkResult.Success)
        assertEquals(1, result.data.size)
        val item = result.data.first()
        assertEquals("Ubuntu.iso", item.name)
        assertEquals(0.5, item.progress)
        assertEquals(512000L, item.downloadSpeed)
    }
}
