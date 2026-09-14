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
    private fun client(password: String = "secret") =
        DownloadClient(
            id = 1,
            type = DownloadClientType.Deluge,
            label = "deluge",
            url = "http://localhost:8112",
            password = EncryptedString(password),
        )

    private fun httpClient(mockEngine: MockEngine) =
        HttpClient(mockEngine) {
            expectSuccess = true
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

    @Test
    fun testConnectionAlwaysReAuthenticates() =
        runTest {
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
    fun testConnectionReturnsErrorWhenLoginRejected() =
        runTest {
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
}
