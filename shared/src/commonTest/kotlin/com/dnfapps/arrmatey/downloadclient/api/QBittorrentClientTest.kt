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
        showExternalIpAddress: Boolean = false,
    ) = DownloadClient(
        id = 1,
        type = DownloadClientType.QBittorrent,
        label = "qb",
        url = "http://localhost:8080",
        username = EncryptedString(username),
        password = EncryptedString(password),
        apiKey = EncryptedString(apiKey),
        showExternalIpAddress = showExternalIpAddress,
    )

    private fun httpClient(mockEngine: MockEngine) = HttpClient(mockEngine) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    @Test
    fun testConnectionWithApiKeySkipsLogin() = runTest {
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
    fun testConnectionWithNoCredentialsSkipsLogin() = runTest {
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
    fun testConnectionWithCredentialsLogsInFirst() = runTest {
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
    fun testGetDownloadsRetriesLoginOn401() = runTest {
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
    fun testGetDownloadsReturnsErrorWhenReLoginFails() = runTest {
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

    @Test
    fun testGetSyncMetadataParsesCorrectly() = runTest {
        val syncJson = """
            {
              "server_state": {
                "alltime_dl": 16275554954391,
                "alltime_ul": 4770773283060,
                "average_time_queue": 133,
                "connection_status": "connected",
                "dht_nodes": 363,
                "dl_info_data": 60064984472,
                "dl_info_speed": 102400,
                "dl_rate_limit": 0,
                "free_space_on_disk": 1349884481536,
                "global_ratio": "0.29",
                "last_external_address_v4": "158.173.3.101",
                "last_external_address_v6": "",
                "queued_io_jobs": 0,
                "queueing": true,
                "read_cache_hits": "0",
                "read_cache_overload": "0",
                "refresh_interval": 1500,
                "total_buffers_size": 0,
                "total_peer_connections": 4,
                "total_queued_size": 0,
                "total_wasted_session": 141539866,
                "up_info_data": 4887036680,
                "up_info_speed": 51200,
                "up_rate_limit": 0,
                "use_alt_speed_limits": false,
                "write_cache_overload": "0"
              }
            }
        """.trimIndent()

        val mockEngine =
            MockEngine { request ->
                assertEquals("/api/v2/sync/metadata", request.url.encodedPath)
                respond(
                    content = syncJson,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

        val qb = QBittorrentClient(client(apiKey = "key"), httpClient(mockEngine))
        val result = qb.getSyncMetadata()

        assertTrue(result is NetworkResult.Success)
        val serverState = result.data.serverState
        assertEquals("158.173.3.101", serverState?.lastExternalAddressV4)
        assertEquals(102400L, serverState?.downloadSpeed)
        assertEquals(51200L, serverState?.uploadSpeed)
        assertEquals("connected", serverState?.connectionStatus)
        assertEquals(363L, serverState?.dhtNodes)
    }

    @Test
    fun testGetTransferInfoWithShowExternalIpEnabled() = runTest {
        val syncJson = """
            {
              "server_state": {
                "dl_info_speed": 204800,
                "up_info_speed": 102400,
                "last_external_address_v4": "158.173.3.101"
              }
            }
        """.trimIndent()

        val mockEngine =
            MockEngine { request ->
                respond(
                    content = syncJson,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

        val qb = QBittorrentClient(client(apiKey = "key", showExternalIpAddress = true), httpClient(mockEngine))
        val result = qb.getTransferInfo()

        assertTrue(result is NetworkResult.Success)
        assertEquals(204800L, result.data.downloadSpeed)
        assertEquals(102400L, result.data.uploadSpeed)
        assertEquals("158.173.3.101", result.data.externalIp)
    }

    @Test
    fun testGetTransferInfoWithShowExternalIpDisabled() = runTest {
        val transferInfoJson = """
            {
              "dl_info_speed": 204800,
              "up_info_speed": 102400
            }
        """.trimIndent()

        val mockEngine =
            MockEngine { request ->
                assertEquals("/api/v2/transfer/info", request.url.encodedPath)
                respond(
                    content = transferInfoJson,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

        val qb = QBittorrentClient(client(apiKey = "key", showExternalIpAddress = false), httpClient(mockEngine))
        val result = qb.getTransferInfo()

        assertTrue(result is NetworkResult.Success)
        assertEquals(204800L, result.data.downloadSpeed)
        assertEquals(102400L, result.data.uploadSpeed)
        assertEquals(null, result.data.externalIp)
    }
}
