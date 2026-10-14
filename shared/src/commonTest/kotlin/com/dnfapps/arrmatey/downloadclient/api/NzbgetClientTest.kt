package com.dnfapps.arrmatey.downloadclient.api

import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.model.DownloadClientType
import com.dnfapps.arrmatey.downloadclient.model.DownloadItemStatus
import com.dnfapps.networking.NetworkResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NzbgetClientTest {
    private val fakeClient = DownloadClient(
        id = 1,
        type = DownloadClientType.NZBGet,
        label = "Test NZBGet",
        url = "http://localhost:6789",
        username = EncryptedString("nzbget"),
        password = EncryptedString("tegbzn6789"),
    )

    private fun httpClient(mockEngine: MockEngine) = HttpClient(mockEngine) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                },
            )
        }
    }

    @Test
    fun testConnectionSuccess() = runTest {
        val mockEngine = MockEngine { request ->
            val auth = request.headers[HttpHeaders.Authorization]
            assertTrue(auth != null && auth.startsWith("Basic "), "Expected Basic auth header")
            respond(
                content = """{"version":"1.1","result":"24.2","id":1}""",
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", "application/json"),
            )
        }

        val result = NzbgetClient(fakeClient, httpClient(mockEngine)).testConnection()
        assertTrue(result is NetworkResult.Success, "Expected Success but got $result")
    }

    @Test
    fun testConnectionReturnsErrorOnUnauthorized() = runTest {
        val mockEngine = MockEngine {
            respond(content = "Unauthorized", status = HttpStatusCode.Unauthorized)
        }

        val result = NzbgetClient(fakeClient, httpClient(mockEngine)).testConnection()
        assertTrue(result is NetworkResult.Error, "Expected Error but got $result")
        assertEquals(HttpStatusCode.Unauthorized.value, result.code)
    }

    @Test
    fun getDownloadsParsesGroupsAndCalculatesProgressAndEta() = runTest {
        val mockEngine = MockEngine { request ->
            val body = request.body.toByteArray().decodeToString()
            val method = Json.parseToJsonElement(body).jsonObject["method"]?.jsonPrimitive?.content

            when (method) {
                "listgroups" -> {
                    val groupsJson = """
                        {
                            "version": "1.1",
                            "result": [
                                {
                                    "NZBID": 101,
                                    "NZBName": "Ubuntu.Linux.24.04.nzb",
                                    "Name": "Ubuntu Linux 24.04",
                                    "Category": "Linux",
                                    "FileSizeLo": 209715200,
                                    "FileSizeHi": 0,
                                    "RemainingSizeLo": 104857600,
                                    "RemainingSizeHi": 0,
                                    "Status": "DOWNLOADING",
                                    "Paused": false,
                                    "MaxPostTime": 1700000000
                                },
                                {
                                    "NZBID": 102,
                                    "NZBName": "Debian.12.nzb",
                                    "Name": "Debian 12",
                                    "Category": "Linux",
                                    "FileSizeLo": 104857600,
                                    "FileSizeHi": 0,
                                    "RemainingSizeLo": 104857600,
                                    "RemainingSizeHi": 0,
                                    "Status": "PAUSED",
                                    "Paused": true,
                                    "MaxPostTime": 1700000100
                                }
                            ],
                            "id": 1
                        }
                    """.trimIndent()
                    respond(
                        content = groupsJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", "application/json"),
                    )
                }
                "status" -> {
                    val statusJson = """
                        {
                            "version": "1.1",
                            "result": {
                                "DownloadRate": 10485760,
                                "DownloadRateLo": 10485760,
                                "DownloadRateHi": 0,
                                "RemainingSizeLo": 209715200,
                                "RemainingSizeHi": 0,
                                "DownloadPaused": false
                            },
                            "id": 2
                        }
                    """.trimIndent()
                    respond(
                        content = statusJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", "application/json"),
                    )
                }
                else -> respond(content = "Not found", status = HttpStatusCode.NotFound)
            }
        }

        val result = NzbgetClient(fakeClient, httpClient(mockEngine)).getDownloads()
        assertTrue(result is NetworkResult.Success, "Expected Success but got $result")

        val items = result.data
        assertEquals(2, items.size)

        val first = items[0]
        assertEquals("101", first.id)
        assertEquals("Ubuntu Linux 24.04", first.name)
        assertEquals(209715200L, first.size)
        assertEquals(104857600L, first.downloaded)
        assertEquals(0.5, first.progress, 0.001)
        assertEquals(10485760L, first.downloadSpeed)
        assertEquals(DownloadItemStatus.Downloading, first.status)
        assertEquals(10L, first.eta) // 104857600 / 10485760 = 10 sec

        val second = items[1]
        assertEquals("102", second.id)
        assertEquals("Debian 12", second.name)
        assertEquals(DownloadItemStatus.DownloadingPaused, second.status)
        assertEquals(0L, second.downloadSpeed)
        assertEquals(0L, second.eta)
    }

    @Test
    fun pauseDownloadSendsGroupPauseAction() = runTest {
        var capturedAction: String? = null
        var capturedIds: List<Int>? = null

        val mockEngine = MockEngine { request ->
            val body = request.body.toByteArray().decodeToString()
            val jsonBody = Json.parseToJsonElement(body).jsonObject
            val params = jsonBody["params"]?.jsonArray
            capturedAction = params?.get(0)?.jsonPrimitive?.content
            capturedIds = params?.get(3)?.jsonArray?.map { it.jsonPrimitive.content.toInt() }

            respond(
                content = """{"version":"1.1","result":true,"id":1}""",
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", "application/json"),
            )
        }

        val result = NzbgetClient(fakeClient, httpClient(mockEngine)).pauseDownload(listOf("42"))
        assertTrue(result is NetworkResult.Success)
        assertEquals("GroupPause", capturedAction)
        assertEquals(listOf(42), capturedIds)
    }

    @Test
    fun resumeDownloadSendsGroupResumeAction() = runTest {
        var capturedAction: String? = null
        var capturedIds: List<Int>? = null

        val mockEngine = MockEngine { request ->
            val body = request.body.toByteArray().decodeToString()
            val jsonBody = Json.parseToJsonElement(body).jsonObject
            val params = jsonBody["params"]?.jsonArray
            capturedAction = params?.get(0)?.jsonPrimitive?.content
            capturedIds = params?.get(3)?.jsonArray?.map { it.jsonPrimitive.content.toInt() }

            respond(
                content = """{"version":"1.1","result":true,"id":1}""",
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", "application/json"),
            )
        }

        val result = NzbgetClient(fakeClient, httpClient(mockEngine)).resumeDownload(listOf("42", "43"))
        assertTrue(result is NetworkResult.Success)
        assertEquals("GroupResume", capturedAction)
        assertEquals(listOf(42, 43), capturedIds)
    }

    @Test
    fun deleteDownloadSendsGroupDeleteWhenDeleteFilesTrue() = runTest {
        var capturedAction: String? = null

        val mockEngine = MockEngine { request ->
            val body = request.body.toByteArray().decodeToString()
            val jsonBody = Json.parseToJsonElement(body).jsonObject
            val params = jsonBody["params"]?.jsonArray
            capturedAction = params?.get(0)?.jsonPrimitive?.content

            respond(
                content = """{"version":"1.1","result":true,"id":1}""",
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", "application/json"),
            )
        }

        val result = NzbgetClient(fakeClient, httpClient(mockEngine)).deleteDownload(listOf("42"), deleteFiles = true)
        assertTrue(result is NetworkResult.Success)
        assertEquals("GroupDelete", capturedAction)
    }

    @Test
    fun deleteDownloadSendsGroupParkDeleteWhenDeleteFilesFalse() = runTest {
        var capturedAction: String? = null

        val mockEngine = MockEngine { request ->
            val body = request.body.toByteArray().decodeToString()
            val jsonBody = Json.parseToJsonElement(body).jsonObject
            val params = jsonBody["params"]?.jsonArray
            capturedAction = params?.get(0)?.jsonPrimitive?.content

            respond(
                content = """{"version":"1.1","result":true,"id":1}""",
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", "application/json"),
            )
        }

        val result = NzbgetClient(fakeClient, httpClient(mockEngine)).deleteDownload(listOf("42"), deleteFiles = false)
        assertTrue(result is NetworkResult.Success)
        assertEquals("GroupParkDelete", capturedAction)
    }

    @Test
    fun getTransferInfoExtractsSpeedFromStatus() = runTest {
        val mockEngine = MockEngine {
            val statusJson = """
                {
                    "version": "1.1",
                    "result": {
                        "DownloadRate": 5242880,
                        "DownloadRateLo": 5242880,
                        "DownloadRateHi": 0
                    },
                    "id": 1
                }
            """.trimIndent()
            respond(
                content = statusJson,
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", "application/json"),
            )
        }

        val result = NzbgetClient(fakeClient, httpClient(mockEngine)).getTransferInfo()
        assertTrue(result is NetworkResult.Success)
        assertEquals(5242880L, result.data.downloadSpeed)
        assertEquals(0L, result.data.uploadSpeed)
    }

    @Test
    fun testConnectionReturnsRpcErrorWhenPresent() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = """{"version":"1.1","error":{"code":-32601,"message":"Method not found"},"id":1}""",
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", "application/json"),
            )
        }

        val result = NzbgetClient(fakeClient, httpClient(mockEngine)).testConnection()
        assertTrue(result is NetworkResult.Error)
        assertTrue(result.message?.contains("Method not found") == true)
    }
}
