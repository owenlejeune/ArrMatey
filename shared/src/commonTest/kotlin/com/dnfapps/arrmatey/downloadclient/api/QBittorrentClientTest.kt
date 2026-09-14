package com.dnfapps.arrmatey.downloadclient.api

import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.model.DownloadClientType
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

class QBittorrentClientTest {
    private fun client(
        username: String = "",
        password: String = "",
        apiKey: String = "",
    ) = DownloadClient(
        id = 1,
        type = DownloadClientType.QBittorrent,
        label = "qb",
        url = "http://localhost:8080",
        username = EncryptedString(username),
        password = EncryptedString(password),
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
    fun testConnectionWithApiKeySkipsLogin() =
        runTest {
            val paths = mutableListOf<String>()
            val mockEngine =
                MockEngine { request ->
                    paths.add(request.url.encodedPath)
                    respond(
                        content = "4.6.0",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "text/plain"),
                    )
                }

            val result = QBittorrentClient(client(apiKey = "key"), httpClient(mockEngine)).testConnection()

            assertTrue(result is NetworkResult.Success)
            assertEquals(listOf("/api/v2/app/version"), paths)
        }

    @Test
    fun testConnectionWithNoCredentialsSkipsLogin() =
        runTest {
            val paths = mutableListOf<String>()
            val mockEngine =
                MockEngine { request ->
                    paths.add(request.url.encodedPath)
                    respond(
                        content = "4.6.0",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "text/plain"),
                    )
                }

            val result = QBittorrentClient(client(), httpClient(mockEngine)).testConnection()

            assertTrue(result is NetworkResult.Success)
            assertEquals(listOf("/api/v2/app/version"), paths)
        }

    @Test
    fun testConnectionWithCredentialsLogsInFirst() =
        runTest {
            val paths = mutableListOf<String>()
            val mockEngine =
                MockEngine { request ->
                    paths.add(request.url.encodedPath)
                    respond(
                        content = "4.6.0",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "text/plain"),
                    )
                }

            val result =
                QBittorrentClient(
                    client(username = "u", password = "p"),
                    httpClient(mockEngine),
                ).testConnection()

            assertTrue(result is NetworkResult.Success)
            assertEquals(listOf("/api/v2/auth/login", "/api/v2/app/version"), paths)
        }

    @Test
    fun testGetDownloadsRetriesLoginOn401() =
        runTest {
            // Verifies the authenticated flag resets on 401 so an expired cookie recovers.
            val paths = mutableListOf<String>()
            var infoCalls = 0
            val mockEngine =
                MockEngine { request ->
                    val path = request.url.encodedPath
                    paths.add(path)
                    when (path) {
                        "/api/v2/auth/login" ->
                            respond(
                                content = "Ok.",
                                status = HttpStatusCode.OK,
                                headers = headersOf(HttpHeaders.ContentType, "text/plain"),
                            )
                        "/api/v2/torrents/info" -> {
                            infoCalls += 1
                            if (infoCalls == 1) {
                                respond(content = "Forbidden", status = HttpStatusCode.Unauthorized)
                            } else {
                                respond(
                                    content = "[]",
                                    status = HttpStatusCode.OK,
                                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                                )
                            }
                        }
                        else -> respond(content = "", status = HttpStatusCode.NotFound)
                    }
                }

            val qb = QBittorrentClient(client(username = "u", password = "p"), httpClient(mockEngine))
            val result = qb.getDownloads()

            assertTrue(result is NetworkResult.Success)
            assertEquals(emptyList(), result.data)
            assertEquals(
                listOf(
                    "/api/v2/auth/login",
                    "/api/v2/torrents/info",
                    "/api/v2/auth/login",
                    "/api/v2/torrents/info",
                ),
                paths,
            )
        }

    @Test
    fun testGetDownloadsReturnsErrorWhenReLoginFails() =
        runTest {
            var loginCalls = 0
            val mockEngine =
                MockEngine { request ->
                    when (request.url.encodedPath) {
                        "/api/v2/auth/login" -> {
                            loginCalls += 1
                            if (loginCalls == 1) {
                                respond(
                                    content = "Ok.",
                                    status = HttpStatusCode.OK,
                                    headers = headersOf(HttpHeaders.ContentType, "text/plain"),
                                )
                            } else {
                                respond(content = "Fails.", status = HttpStatusCode.Forbidden)
                            }
                        }
                        "/api/v2/torrents/info" ->
                            respond(content = "Unauthorized", status = HttpStatusCode.Unauthorized)
                        else -> respond(content = "", status = HttpStatusCode.NotFound)
                    }
                }

            val result =
                QBittorrentClient(client(username = "u", password = "p"), httpClient(mockEngine))
                    .getDownloads()

            assertTrue(result is NetworkResult.Error)
            assertEquals(HttpStatusCode.Forbidden.value, result.code)
        }
}
