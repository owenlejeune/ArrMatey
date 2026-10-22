package com.dnfapps.arrmatey.downloadclient.api

import com.dnfapps.arrmatey.arr.api.model.ArrDiskSpace
import com.dnfapps.arrmatey.downloadclient.api.model.TransmissionRpcRequest
import com.dnfapps.arrmatey.downloadclient.api.model.TransmissionRpcResponse
import com.dnfapps.arrmatey.downloadclient.api.model.TransmissionSessionStats
import com.dnfapps.arrmatey.downloadclient.api.model.TransmissionTorrent
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.model.DownloadClientSystemOverview
import com.dnfapps.arrmatey.downloadclient.model.DownloadItem
import com.dnfapps.arrmatey.downloadclient.model.DownloadItemStatus
import com.dnfapps.arrmatey.downloadclient.model.DownloadTransferInfo
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.safeCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.basicAuth
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

private const val HEADER_SESSION_ID = "X-Transmission-Session-Id"

class TransmissionClient(
    private val downloadClient: DownloadClient,
    private val httpClient: HttpClient,
) : DownloadClientApi {
    private var sessionId: String = ""

    private val json =
        Json {
            ignoreUnknownKeys = true
        }

    override suspend fun testConnection(): NetworkResult<Unit> = when (val result = executeTransmissionRequest<JsonObject>("session-stats")) {
        is NetworkResult.Success -> result.data.toUnitResult()
        is NetworkResult.Error -> result
        is NetworkResult.Loading -> result
    }

    override suspend fun getDownloads(): NetworkResult<List<DownloadItem>> {
        val arguments =
            buildJsonObject {
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
                    },
                )
            }

        return when (
            val result =
                executeTransmissionRequest<JsonObject>(
                    method = "torrent-get",
                    arguments = arguments,
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

    override suspend fun pauseDownload(ids: List<String>): NetworkResult<Unit> = executeTorrentAction(
        method = "torrent-stop",
        ids = ids,
    )

    override suspend fun resumeDownload(ids: List<String>): NetworkResult<Unit> = executeTorrentAction(
        method = "torrent-start",
        ids = ids,
    )

    override suspend fun deleteDownload(
        ids: List<String>,
        deleteFiles: Boolean,
    ): NetworkResult<Unit> = when (
        val result =
            executeTransmissionRequest<JsonObject>(
                method = "torrent-remove",
                arguments =
                buildJsonObject {
                    put(
                        "ids",
                        buildJsonArray {
                            ids.forEach { id ->
                                add(id.toTransmissionId())
                            }
                        },
                    )
                    put("delete-local-data", JsonPrimitive(deleteFiles))
                },
            )
    ) {
        is NetworkResult.Success -> result.data.toUnitResult()
        is NetworkResult.Error -> result
        is NetworkResult.Loading -> result
    }

    override suspend fun getTransferInfo(): NetworkResult<DownloadTransferInfo> = when (val result = executeTransmissionRequest<JsonObject>("session-stats")) {
        is NetworkResult.Success -> {
            when (val rpcResult = result.data.argumentsResultOrError()) {
                is NetworkResult.Success -> {
                    val sessionStats = json.decodeFromJsonElement<TransmissionSessionStats>(rpcResult.data)
                    NetworkResult.Success(
                        DownloadTransferInfo(
                            client = downloadClient,
                            downloadSpeed = sessionStats.downloadSpeed,
                            uploadSpeed = sessionStats.uploadSpeed,
                        ),
                    )
                }
                is NetworkResult.Error -> rpcResult
                is NetworkResult.Loading -> rpcResult
            }
        }
        is NetworkResult.Error -> result
        is NetworkResult.Loading -> result
    }

    override suspend fun getSystemOverview(): NetworkResult<DownloadClientSystemOverview> {
        val sessionGetResult = executeTransmissionRequest<JsonObject>("session-get")
        val sessionArgs = ((sessionGetResult as? NetworkResult.Success)?.data?.argumentsResultOrError() as? NetworkResult.Success)?.data

        val version = sessionArgs?.get("version")?.jsonPrimitive?.contentOrNull
        val rpcVersion = sessionArgs?.get("rpc-version")?.jsonPrimitive?.longOrNull?.toString()
        val downloadDir = sessionArgs?.get("download-dir")?.jsonPrimitive?.contentOrNull
        val freeSpaceBytes = sessionArgs?.get("download-dir-free-space")?.jsonPrimitive?.longOrNull ?: 0L

        val diskSpaces = if (freeSpaceBytes > 0L) {
            listOf(ArrDiskSpace(path = downloadDir, label = "Download Directory", freeSpace = freeSpaceBytes, totalSpace = 0L))
        } else {
            emptyList()
        }

        val sessionStatsResult = executeTransmissionRequest<JsonObject>("session-stats")
        val statsArgs = ((sessionStatsResult as? NetworkResult.Success)?.data?.argumentsResultOrError() as? NetworkResult.Success)?.data

        val downloadSpeed = statsArgs?.get("downloadSpeed")?.jsonPrimitive?.longOrNull ?: 0L
        val uploadSpeed = statsArgs?.get("uploadSpeed")?.jsonPrimitive?.longOrNull ?: 0L
        val activeCount = statsArgs?.get("activeTorrentCount")?.jsonPrimitive?.intOrNull ?: 0
        val cumulativeStats = statsArgs?.get("cumulative-stats")?.jsonObject
        val alltimeDl = cumulativeStats?.get("downloadedBytes")?.jsonPrimitive?.longOrNull
        val alltimeUl = cumulativeStats?.get("uploadedBytes")?.jsonPrimitive?.longOrNull

        val transferInfo = DownloadTransferInfo(
            client = downloadClient,
            downloadSpeed = downloadSpeed,
            uploadSpeed = uploadSpeed,
        )

        return NetworkResult.Success(
            DownloadClientSystemOverview(
                version = version,
                webApiVersion = rpcVersion?.let { "RPC v$it" },
                downloadDir = downloadDir,
                diskSpaces = diskSpaces,
                transferInfo = transferInfo,
                activeDownloadsCount = activeCount,
                alltimeDownloaded = alltimeDl,
                alltimeUploaded = alltimeUl,
            ),
        )
    }

    private suspend fun executeTorrentAction(
        method: String,
        ids: List<String>,
    ): NetworkResult<Unit> = when (
        val result =
            executeTransmissionRequest<JsonObject>(
                method = method,
                arguments =
                buildJsonObject {
                    put(
                        "ids",
                        buildJsonArray {
                            ids.forEach { id ->
                                add(id.toTransmissionId())
                            }
                        },
                    )
                },
            )
    ) {
        is NetworkResult.Success -> result.data.toUnitResult()
        is NetworkResult.Error -> result
        is NetworkResult.Loading -> result
    }

    private suspend inline fun <reified T> executeTransmissionRequest(
        method: String,
        arguments: JsonObject? = null,
    ): NetworkResult<TransmissionRpcResponse<T>> {
        val request =
            TransmissionRpcRequest(
                method = method,
                arguments = arguments,
            )

        return httpClient.safeCall {
            // A missing or stale session id returns 409 with the id to echo back, thrown when expectSuccess is enabled.
            val firstResponse =
                try {
                    postRpc(request)
                } catch (e: ClientRequestException) {
                    if (e.response.status != HttpStatusCode.Conflict) throw e
                    e.response
                }

            val response =
                if (firstResponse.status == HttpStatusCode.Conflict) {
                    sessionId = firstResponse.headers[HEADER_SESSION_ID].orEmpty()
                    postRpc(request)
                } else {
                    firstResponse
                }

            response.body<TransmissionRpcResponse<T>>()
        }
    }

    private suspend fun HttpClient.postRpc(request: TransmissionRpcRequest): HttpResponse = post("transmission/rpc") {
        contentType(ContentType.Application.Json)
        basicAuth(downloadClient.username.value, downloadClient.password.value)
        if (sessionId.isNotEmpty()) {
            header(HEADER_SESSION_ID, sessionId)
        }
        setBody(request)
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

        val rpcArguments =
            arguments
                ?: return NetworkResult.Error(message = "Missing Transmission RPC arguments")

        return NetworkResult.Success(rpcArguments)
    }

    private fun <T> TransmissionRpcResponse<T>.toUnitResult(): NetworkResult<Unit> = if (result == "success") {
        NetworkResult.Success(Unit)
    } else {
        NetworkResult.Error(message = "Transmission RPC error: $result")
    }

    private fun TransmissionTorrent.toDownloadItem(client: DownloadClient): DownloadItem {
        val coercedProgress = percentDone.coerceIn(0.0, 1.0)
        return DownloadItem(
            client = client,
            id = id.toString(),
            name = name,
            size = totalSize,
            downloaded = (totalSize.toDouble() * coercedProgress).toLong(),
            progress = coercedProgress,
            downloadSpeed = rateDownload,
            uploadSpeed = rateUpload,
            eta = eta,
            status = status.toDownloadStatus(),
            category = downloadDir,
            addedOn = addedDate,
        )
    }

    private fun Int.toDownloadStatus(): DownloadItemStatus = when (this) {
        0 -> DownloadItemStatus.DownloadingPaused
        1 -> DownloadItemStatus.Queued
        2 -> DownloadItemStatus.Queued
        3 -> DownloadItemStatus.Queued
        4 -> DownloadItemStatus.Downloading
        5 -> DownloadItemStatus.Queued
        6 -> DownloadItemStatus.Uploading
        else -> DownloadItemStatus.Unknown
    }
}
