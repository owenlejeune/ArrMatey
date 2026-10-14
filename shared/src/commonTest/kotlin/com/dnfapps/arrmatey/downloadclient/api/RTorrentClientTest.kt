package com.dnfapps.arrmatey.downloadclient.api

import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.downloadclient.api.model.XmlRpc
import com.dnfapps.arrmatey.downloadclient.api.model.XmlRpcValue
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.model.DownloadClientType
import com.dnfapps.arrmatey.downloadclient.model.DownloadItemStatus
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

class RTorrentClientTest {
    private val fakeClient = DownloadClient(
        id = 1,
        type = DownloadClientType.RTorrent,
        label = "Test rTorrent",
        url = "http://localhost:8080/RPC2",
        username = EncryptedString("rtuser"),
        password = EncryptedString("rtpass"),
    )

    private fun httpClient(mockEngine: MockEngine) = HttpClient(mockEngine) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    @Test
    fun testConnection_success() = runTest {
        val mockEngine = MockEngine { request ->
            val auth = request.headers["Authorization"]
            assertTrue(auth != null && auth.startsWith("Basic "))
            respond(
                content = """
                    <?xml version="1.0" encoding="utf-8"?>
                    <methodResponse>
                        <params>
                            <param>
                                <value><string>0.9.8</string></value>
                            </param>
                        </params>
                    </methodResponse>
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", "text/xml"),
            )
        }

        val client = RTorrentClient(fakeClient, httpClient(mockEngine))
        val result = client.testConnection()

        assertTrue(result is NetworkResult.Success, "Expected Success but got $result")
    }

    @Test
    fun testConnection_authFailure_401() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = "401 Unauthorized",
                status = HttpStatusCode.Unauthorized,
            )
        }

        val client = RTorrentClient(fakeClient, httpClient(mockEngine))
        val result = client.testConnection()

        assertTrue(result is NetworkResult.Error, "Expected Error but got $result")
        assertTrue(result.message?.contains("credentials") == true || result.message?.contains("401") == true)
    }

    @Test
    fun testConnection_xmlRpcFault() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = """
                    <?xml version="1.0" encoding="utf-8"?>
                    <methodResponse>
                        <fault>
                            <value>
                                <struct>
                                    <member>
                                        <name>faultCode</name>
                                        <value><i4>-501</i4></value>
                                    </member>
                                    <member>
                                        <name>faultString</name>
                                        <value><string>Method not found</string></value>
                                    </member>
                                </struct>
                            </value>
                        </fault>
                    </methodResponse>
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", "text/xml"),
            )
        }

        val client = RTorrentClient(fakeClient, httpClient(mockEngine))
        val result = client.testConnection()

        assertTrue(result is NetworkResult.Error, "Expected Error but got $result")
        assertTrue(result.message?.contains("Method not found") == true)
    }

    @Test
    fun testConnection_nonXmlResponse() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = "<html><body>502 Bad Gateway</body></html>",
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", "text/html"),
            )
        }

        val client = RTorrentClient(fakeClient, httpClient(mockEngine))
        val result = client.testConnection()

        assertTrue(result is NetworkResult.Error, "Expected Error but got $result")
        assertTrue(result.message?.contains("did not return XML-RPC") == true)
    }

    @Test
    fun getDownloads_success() = runTest {
        val mockEngine = MockEngine { request ->
            val body = (request.body as? TextContent)?.text.orEmpty()
            assertTrue(body.contains("d.multicall2"))
            respond(
                content = """
                    <?xml version="1.0" encoding="utf-8"?>
                    <methodResponse>
                        <params>
                            <param>
                                <value>
                                    <array>
                                        <data>
                                            <value>
                                                <array>
                                                    <data>
                                                        <value><string>HASH123</string></value>
                                                        <value><string>Linux ISO</string></value>
                                                        <value><i8>1000000</i8></value>
                                                        <value><i8>500000</i8></value>
                                                        <value><i8>500000</i8></value>
                                                        <value><i8>50000</i8></value>
                                                        <value><i8>10000</i8></value>
                                                        <value><i8>200000</i8></value>
                                                        <value><i8>400</i8></value>
                                                        <value><boolean>1</boolean></value>
                                                        <value><boolean>1</boolean></value>
                                                        <value><boolean>0</boolean></value>
                                                        <value><i8>0</i8></value>
                                                        <value><string></string></value>
                                                        <value><i8>5</i8></value>
                                                        <value><i8>10</i8></value>
                                                        <value><string>/downloads</string></value>
                                                        <value><string>ISO</string></value>
                                                        <value><i8>2</i8></value>
                                                        <value><i8>1</i8></value>
                                                    </data>
                                                </array>
                                            </value>
                                        </data>
                                    </array>
                                </value>
                            </param>
                        </params>
                    </methodResponse>
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", "text/xml"),
            )
        }

        val client = RTorrentClient(fakeClient, httpClient(mockEngine))
        val result = client.getDownloads()

        assertTrue(result is NetworkResult.Success, "Expected Success but got $result")
        val items = result.data
        assertEquals(1, items.size)
        val item = items.first()
        assertEquals("HASH123", item.id)
        assertEquals("Linux ISO", item.name)
        assertEquals(1000000L, item.size)
        assertEquals(500000L, item.downloaded)
        assertEquals(0.5, item.progress)
        assertEquals(50000L, item.downloadSpeed)
        assertEquals(10000L, item.uploadSpeed)
        assertEquals(10L, item.eta) // 500000 left / 50000 rate = 10s
        assertEquals("ISO", item.category)
        assertEquals(DownloadItemStatus.Downloading, item.status)
    }

    @Test
    fun pauseDownload_singleAndMulti() = runTest {
        val requests = mutableListOf<String>()
        val mockEngine = MockEngine { request ->
            val body = (request.body as? TextContent)?.text.orEmpty()
            requests.add(body)
            respond(
                content = """
                    <?xml version="1.0" encoding="utf-8"?>
                    <methodResponse>
                        <params><param><value><i4>0</i4></value></param></params>
                    </methodResponse>
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", "text/xml"),
            )
        }

        val client = RTorrentClient(fakeClient, httpClient(mockEngine))
        client.pauseDownload(listOf("HASH1"))
        client.pauseDownload(listOf("HASH1", "HASH2"))

        assertEquals(2, requests.size)
        assertTrue(requests[0].contains("d.stop"))
        assertTrue(requests[0].contains("HASH1"))
        assertTrue(requests[1].contains("system.multicall"))
        assertTrue(requests[1].contains("HASH1") && requests[1].contains("HASH2"))
    }

    @Test
    fun resumeDownload_singleAndMulti() = runTest {
        val requests = mutableListOf<String>()
        val mockEngine = MockEngine { request ->
            val body = (request.body as? TextContent)?.text.orEmpty()
            requests.add(body)
            respond(
                content = """
                    <?xml version="1.0" encoding="utf-8"?>
                    <methodResponse>
                        <params><param><value><i4>0</i4></value></param></params>
                    </methodResponse>
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", "text/xml"),
            )
        }

        val client = RTorrentClient(fakeClient, httpClient(mockEngine))
        client.resumeDownload(listOf("HASH1"))
        client.resumeDownload(listOf("HASH1", "HASH2"))

        assertEquals(2, requests.size)
        assertTrue(requests[0].contains("d.start"))
        assertTrue(requests[1].contains("system.multicall"))
    }

    @Test
    fun deleteDownload_callsErase() = runTest {
        var recordedBody = ""
        val mockEngine = MockEngine { request ->
            recordedBody = (request.body as? TextContent)?.text.orEmpty()
            respond(
                content = """
                    <?xml version="1.0" encoding="utf-8"?>
                    <methodResponse>
                        <params><param><value><i4>0</i4></value></param></params>
                    </methodResponse>
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", "text/xml"),
            )
        }

        val client = RTorrentClient(fakeClient, httpClient(mockEngine))
        val result = client.deleteDownload(listOf("HASH_DEL"), deleteFiles = true)

        assertTrue(result is NetworkResult.Success)
        assertTrue(recordedBody.contains("d.erase"))
        assertTrue(recordedBody.contains("HASH_DEL"))
    }

    @Test
    fun getTransferInfo_success() = runTest {
        val mockEngine = MockEngine { request ->
            val body = (request.body as? TextContent)?.text.orEmpty()
            assertTrue(body.contains("system.multicall"))
            respond(
                content = """
                    <?xml version="1.0" encoding="utf-8"?>
                    <methodResponse>
                        <params>
                            <param>
                                <value>
                                    <array>
                                        <data>
                                            <value><array><data><value><string>0.9.8</string></value></data></array></value>
                                            <value><array><data><value><i8>123456</i8></value></data></array></value>
                                            <value><array><data><value><i8>654321</i8></value></data></array></value>
                                            <value><array><data><value><i8>0</i8></value></data></array></value>
                                            <value><array><data><value><i8>0</i8></value></data></array></value>
                                            <value><array><data><value><i8>55555</i8></value></data></array></value>
                                        </data>
                                    </array>
                                </value>
                            </param>
                        </params>
                    </methodResponse>
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", "text/xml"),
            )
        }

        val client = RTorrentClient(fakeClient, httpClient(mockEngine))
        val result = client.getTransferInfo()

        assertTrue(result is NetworkResult.Success, "Expected Success but got $result")
        assertEquals(123456L, result.data.downloadSpeed)
        assertEquals(654321L, result.data.uploadSpeed)
    }

    @Test
    fun xmlRpc_builderAndParser_roundTrip() {
        val call = XmlRpc.buildCall(
            "test.method",
            listOf(
                "str",
                42,
                1234567890123L,
                true,
                3.14,
                listOf("item1", 99),
                mapOf("key1" to "val1", "key2" to 10),
            ),
        )
        assertTrue(call.contains("<methodName>test.method</methodName>"))
        assertTrue(call.contains("<string>str</string>"))
        assertTrue(call.contains("<i4>42</i4>"))
        assertTrue(call.contains("<i8>1234567890123</i8>"))
        assertTrue(call.contains("<boolean>1</boolean>"))
        assertTrue(call.contains("<double>3.14</double>"))
        assertTrue(call.contains("<struct>"))
        assertTrue(call.contains("<name>key1</name>"))

        val xmlResponse = """
            <?xml version="1.0" encoding="utf-8"?>
            <methodResponse>
                <params>
                    <param>
                        <value>
                            <struct>
                                <member>
                                    <name>name</name>
                                    <value><string>ArrMatey</string></value>
                                </member>
                                <member>
                                    <name>active</name>
                                    <value><boolean>1</boolean></value>
                                </member>
                                <member>
                                    <name>count</name>
                                    <value><i4>7</i4></value>
                                </member>
                            </struct>
                        </value>
                    </param>
                </params>
            </methodResponse>
        """.trimIndent()

        val parsed = XmlRpc.parseResponse(xmlResponse)
        assertTrue(parsed is XmlRpcValue.StructValue)
        val map = parsed.asMap()
        assertEquals("ArrMatey", map["name"]?.asString())
        assertEquals(true, map["active"]?.asBoolean())
        assertEquals(7, map["count"]?.asInt())
    }

    @Test
    fun getDetail_successWithFilesTrackersAndPeers() = runTest {
        val mockEngine = MockEngine { request ->
            val body = (request.body as? TextContent)?.text.orEmpty()
            when {
                body.contains("f.multicall") -> respond(
                    content = """
                        <?xml version="1.0" encoding="utf-8"?>
                        <methodResponse><params><param><value><array><data><value><array><data>
                            <value><string>folder/sample.mkv</string></value>
                            <value><i8>104857600</i8></value>
                            <value><i8>50</i8></value>
                            <value><i8>100</i8></value>
                            <value><i4>1</i4></value>
                        </data></array></value></data></array></value></param></params></methodResponse>
                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type", "text/xml"),
                )
                body.contains("t.multicall") -> respond(
                    content = """
                        <?xml version="1.0" encoding="utf-8"?>
                        <methodResponse><params><param><value><array><data><value><array><data>
                            <value><string>udp://tracker.openbittorrent.com:80</string></value>
                            <value><i4>0</i4></value>
                            <value><i4>1</i4></value>
                        </data></array></value></data></array></value></param></params></methodResponse>
                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type", "text/xml"),
                )
                body.contains("p.multicall") -> respond(
                    content = """
                        <?xml version="1.0" encoding="utf-8"?>
                        <methodResponse><params><param><value><array><data><value><array><data>
                            <value><string>192.168.1.50</string></value>
                            <value><string>qBittorrent/4.5.2</string></value>
                            <value><i8>10240</i8></value>
                            <value><i8>2048</i8></value>
                            <value><i8>75</i8></value>
                            <value><i4>1</i4></value>
                        </data></array></value></data></array></value></param></params></methodResponse>
                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type", "text/xml"),
                )
                else -> respond(
                    content = "unknown",
                    status = HttpStatusCode.BadRequest,
                )
            }
        }

        val client = RTorrentClient(fakeClient, httpClient(mockEngine))
        val result = client.getDetail("HASH_DETAIL")

        assertTrue(result is NetworkResult.Success)
        val detail = result.data
        assertEquals(1, detail.files.size)
        assertEquals("sample.mkv", detail.files[0].displayName)
        assertEquals(0.5, detail.files[0].progress)
        assertEquals("Normal", detail.files[0].priorityLabel)

        assertEquals(1, detail.trackers.size)
        assertEquals("udp://tracker.openbittorrent.com:80", detail.trackers[0].url)
        assertTrue(detail.trackers[0].isEnabled)

        assertEquals(1, detail.peers.size)
        assertEquals("192.168.1.50", detail.peers[0].address)
        assertEquals(0.75, detail.peers[0].progress)
    }

    @Test
    fun getDetail_stoppedPeersFaultIgnoredQuietly() = runTest {
        val mockEngine = MockEngine { request ->
            val body = (request.body as? TextContent)?.text.orEmpty()
            when {
                body.contains("f.multicall") -> respond(
                    content = """
                        <?xml version="1.0" encoding="utf-8"?>
                        <methodResponse><params><param><value><array><data></data></array></value></param></params></methodResponse>
                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type", "text/xml"),
                )
                body.contains("t.multicall") -> respond(
                    content = """
                        <?xml version="1.0" encoding="utf-8"?>
                        <methodResponse><params><param><value><array><data></data></array></value></param></params></methodResponse>
                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type", "text/xml"),
                )
                body.contains("p.multicall") -> respond(
                    content = """
                        <?xml version="1.0" encoding="utf-8"?>
                        <methodResponse><fault><value><struct>
                            <member><name>faultCode</name><value><i4>-501</i4></value></member>
                            <member><name>faultString</name><value><string>Torrent is stopped</string></value></member>
                        </struct></value></fault></methodResponse>
                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type", "text/xml"),
                )
                else -> respond(content = "unknown", status = HttpStatusCode.BadRequest)
            }
        }

        val client = RTorrentClient(fakeClient, httpClient(mockEngine))
        val result = client.getDetail("STOPPED_HASH")

        assertTrue(result is NetworkResult.Success)
        assertTrue(result.data.peers.isEmpty())
    }

    @Test
    fun close_callsDClose() = runTest {
        var recordedBody = ""
        val mockEngine = MockEngine { request ->
            recordedBody = (request.body as? TextContent)?.text.orEmpty()
            respond(
                content = """
                    <?xml version="1.0" encoding="utf-8"?>
                    <methodResponse><params><param><value><i4>0</i4></value></param></params></methodResponse>
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", "text/xml"),
            )
        }

        val client = RTorrentClient(fakeClient, httpClient(mockEngine))
        val result = client.close("HASH_CLOSE")

        assertTrue(result is NetworkResult.Success)
        assertTrue(recordedBody.contains("d.close"))
        assertTrue(recordedBody.contains("HASH_CLOSE"))
    }

    @Test
    fun setFilePriority_callsPrioritySetAndUpdatePriorities() = runTest {
        val requests = mutableListOf<String>()
        val mockEngine = MockEngine { request ->
            val body = (request.body as? TextContent)?.text.orEmpty()
            requests.add(body)
            respond(
                content = """
                    <?xml version="1.0" encoding="utf-8"?>
                    <methodResponse><params><param><value><i4>0</i4></value></param></params></methodResponse>
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", "text/xml"),
            )
        }

        val client = RTorrentClient(fakeClient, httpClient(mockEngine))
        val result = client.setFilePriority("HASH_PRIO", index = 2, priority = 0)

        assertTrue(result is NetworkResult.Success)
        assertEquals(2, requests.size)
        assertTrue(requests[0].contains("f.priority.set"))
        assertTrue(requests[0].contains("HASH_PRIO:f2"))
        assertTrue(requests[1].contains("d.update_priorities"))
        assertTrue(requests[1].contains("HASH_PRIO"))
    }

    @Test
    fun addUrl_magnetWithDirectory() = runTest {
        var recordedBody = ""
        val mockEngine = MockEngine { request ->
            recordedBody = (request.body as? TextContent)?.text.orEmpty()
            respond(
                content = """
                    <?xml version="1.0" encoding="utf-8"?>
                    <methodResponse><params><param><value><i4>0</i4></value></param></params></methodResponse>
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", "text/xml"),
            )
        }

        val client = RTorrentClient(fakeClient, httpClient(mockEngine))
        val result = client.addUrl("magnet:?xt=urn:btih:TEST", start = true, directory = "/media/downloads")

        assertTrue(result is NetworkResult.Success)
        assertTrue(recordedBody.contains("load.start_verbose"))
        assertTrue(recordedBody.contains("magnet:?xt=urn:btih:TEST"))
        assertTrue(recordedBody.contains("d.directory.set=&quot;/media/downloads&quot;"))
    }

    @Test
    fun addFile_rawTorrentBytesWithDirectory() = runTest {
        var recordedBody = ""
        val mockEngine = MockEngine { request ->
            recordedBody = (request.body as? TextContent)?.text.orEmpty()
            respond(
                content = """
                    <?xml version="1.0" encoding="utf-8"?>
                    <methodResponse><params><param><value><i4>0</i4></value></param></params></methodResponse>
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", "text/xml"),
            )
        }

        val client = RTorrentClient(fakeClient, httpClient(mockEngine))
        val result = client.addFile(byteArrayOf(1, 2, 3, 4, 5), start = false, directory = "/downloads")

        assertTrue(result is NetworkResult.Success)
        assertTrue(recordedBody.contains("load.raw_verbose"))
        assertTrue(recordedBody.contains("<base64>"))
        assertTrue(recordedBody.contains("d.directory.set=&quot;/downloads&quot;"))
    }
}
