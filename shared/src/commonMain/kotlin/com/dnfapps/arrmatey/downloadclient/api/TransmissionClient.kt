package com.dnfapps.arrmatey.downloadclient.api

import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.client.safeCall
import com.dnfapps.arrmatey.downloadclient.api.model.TransmissionRpcRequest
import com.dnfapps.arrmatey.downloadclient.api.model.TransmissionRpcResponse
import com.dnfapps.arrmatey.downloadclient.api.model.TransmissionSessionStats
import com.dnfapps.arrmatey.downloadclient.api.model.TransmissionTorrent
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.model.DownloadItem
import com.dnfapps.arrmatey.downloadclient.model.DownloadItemStatus
import com.dnfapps.arrmatey.downloadclient.model.DownloadTransferInfo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.http.contentType
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement

private const val HEADER_SESSION_ID = "X-Transmission-Session-Id"

class TransmissionClient(
    private val downloadClient: DownloadClient,
    private val httpClient: HttpClient
): DownloadClientApi {

    private var sessionId: String = ""

    private val json = Json {
        ignoreUnknownKeys = true
    }

    override suspend fun testConnection(): NetworkResult<Unit> {
        return when (val result = executeTransmissionRequest<JsonObject>("session-stats")) {
            is NetworkResult.Success -> result.data.toUnitResult()
            is NetworkResult.Error -> result
            is NetworkResult.Loading -> result
        }
    }

    override suspend fun getDownloads(): NetworkResult<List<DownloadItem>> {
        val arguments = buildJsonObject {
            put(
                "fields",
                buildJsonArray {
                    add(JsonPrimitive("id"))
                    add(JsonPrimitive("name"))
                    add(JsonPrimitive("totalSize"))
                    add(JsonPrimitive("percentDone"))
                    add(JsonPrimitive("rateDownload"))
                    add(JsonPrimitive("rateUpload"))
                    add(JsonPrimitive("eta"))
                    add(JsonPrimitive("status"))
                    add(JsonPrimitive("downloadDir"))
                    add(JsonPrimitive("addedDate"))
                    add(JsonPrimitive("hashString"))
                }
            )
        }

        return when (
            val result = executeTransmissionRequest<JsonObject>(
                method = "torrent-get",
                arguments = arguments
            )
        ) {
            is NetworkResult.Success -> {
                when (val rpcResult = result.data.argumentsResultOrError()) {
                    is NetworkResult.Success -> {
                        val torrentsElement = rpcResult.data["torrents"] ?: buildJsonArray {}
                        val torrents = json.decodeFromJsonElement<List<TransmissionTorrent>>(torrentsElement)
                        NetworkResult.Success(torrents.map { it.toDownloadItem(downloadClient) })
                    }
                    is NetworkResult.Error -> rpcResult
                    is NetworkResult.Loading -> rpcResult
                }
            }
            is NetworkResult.Error -> result
            is NetworkResult.Loading -> result
        }
    }

    override suspend fun pauseDownload(id: String): NetworkResult<Unit> {
        return executeTorrentAction(
            method = "torrent-stop",
            id = id
        )
    }

    override suspend fun resumeDownload(id: String): NetworkResult<Unit> {
        return executeTorrentAction(
            method = "torrent-start",
            id = id
        )
    }

    override suspend fun deleteDownload(id: String, deleteFiles: Boolean): NetworkResult<Unit> {
        return when (
            val result = executeTransmissionRequest<JsonObject>(
                method = "torrent-remove",
                arguments = buildJsonObject {
                    put(
                        "ids",
                        buildJsonArray {
                            add(id.toTransmissionId())
                        }
                    )
                    put("delete-local-data", JsonPrimitive(deleteFiles))
                }
            )
        ) {
            is NetworkResult.Success -> result.data.toUnitResult()
            is NetworkResult.Error -> result
            is NetworkResult.Loading -> result
        }
    }

    override suspend fun getTransferInfo(): NetworkResult<DownloadTransferInfo> {
        return when (val result = executeTransmissionRequest<JsonObject>("session-stats")) {
            is NetworkResult.Success -> {
                when (val rpcResult = result.data.argumentsResultOrError()) {
                    is NetworkResult.Success -> {
                        val sessionStats = json.decodeFromJsonElement<TransmissionSessionStats>(rpcResult.data)
                        NetworkResult.Success(
                            DownloadTransferInfo(
                                client = downloadClient,
                                downloadSpeed = sessionStats.downloadSpeed,
                                uploadSpeed = sessionStats.uploadSpeed
                            )
                        )
                    }
                    is NetworkResult.Error -> rpcResult
                    is NetworkResult.Loading -> rpcResult
                }
            }
            is NetworkResult.Error -> result
            is NetworkResult.Loading -> result
        }
    }

    private suspend fun executeTorrentAction(method: String, id: String): NetworkResult<Unit> {
        return when (
            val result = executeTransmissionRequest<JsonObject>(
                method = method,
                arguments = buildJsonObject {
                    put(
                        "ids",
                        buildJsonArray {
                            add(id.toTransmissionId())
                        }
                    )
                }
            )
        ) {
            is NetworkResult.Success -> result.data.toUnitResult()
            is NetworkResult.Error -> result
            is NetworkResult.Loading -> result
            else -> NetworkResult.Error(message = "Unexpected Transmission action state")
        }
    }

    private suspend inline fun <reified T> executeTransmissionRequest(
        method: String,
        arguments: JsonObject? = null
    ): NetworkResult<TransmissionRpcResponse<T>> {
        val request = TransmissionRpcRequest(
            method = method,
            arguments = arguments
        )

        return httpClient.safeCall {
            val firstResponse = post("transmission/rpc") {
                contentType(ContentType.Application.Json)
                basicAuth(downloadClient.username, downloadClient.password)
                if (sessionId.isNotEmpty()) {
                    header(HEADER_SESSION_ID, sessionId)
                }
                setBody(request)
            }

            val response = if (firstResponse.status.value == 409) {
                sessionId = firstResponse.headers[HEADER_SESSION_ID].orEmpty()
                post("transmission/rpc") {
                    contentType(ContentType.Application.Json)
                    basicAuth(downloadClient.username, downloadClient.password)
                    if (sessionId.isNotEmpty()) {
                        header(HEADER_SESSION_ID, sessionId)
                    }
                    setBody(request)
                }
            } else {
                firstResponse
            }

            response.body<TransmissionRpcResponse<T>>()
        }
    }

    private fun String.toTransmissionId(): JsonPrimitive {
        val intId = toIntOrNull()
        return if (intId != null) {
            JsonPrimitive(intId)
        } else {
            JsonPrimitive(this)
        }
    }

    private fun <T> TransmissionRpcResponse<T>.argumentsResultOrError(): NetworkResult<T> {
        if (result != "success") {
            return NetworkResult.Error(message = "Transmission RPC error: $result")
        }

        val rpcArguments = arguments
            ?: return NetworkResult.Error(message = "Missing Transmission RPC arguments")

        return NetworkResult.Success(rpcArguments)
    }

    private fun <T> TransmissionRpcResponse<T>.toUnitResult(): NetworkResult<Unit> {
        return if (result == "success") {
            NetworkResult.Success(Unit)
        } else {
            NetworkResult.Error(message = "Transmission RPC error: $result")
        }
    }

    private fun TransmissionTorrent.toDownloadItem(client: DownloadClient): DownloadItem {
        return DownloadItem(
            client = client,
            id = id.toString(),
            name = name,
            size = totalSize,
            progress = percentDone.coerceIn(0.0, 1.0),
            downloadSpeed = rateDownload,
            uploadSpeed = rateUpload,
            eta = eta,
            status = status.toDownloadStatus(),
            category = downloadDir,
            addedOn = addedDate
        )
    }

    private fun Int.toDownloadStatus(): DownloadItemStatus {
        return when (this) {
            0 -> DownloadItemStatus.Paused
            1 -> DownloadItemStatus.Queued
            2 -> DownloadItemStatus.Queued
            3 -> DownloadItemStatus.Queued
            4 -> DownloadItemStatus.Downloading
            5 -> DownloadItemStatus.Queued
            6 -> DownloadItemStatus.Seeding
            else -> DownloadItemStatus.Queued
        }
    }
}
