package com.dnfapps.arrmatey.downloadclient.api

import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.client.safePost
import com.dnfapps.arrmatey.downloadclient.api.model.DelugeJsonRpcRequest
import com.dnfapps.arrmatey.downloadclient.api.model.DelugeJsonRpcResponse
import com.dnfapps.arrmatey.downloadclient.api.model.DelugeSessionStatus
import com.dnfapps.arrmatey.downloadclient.api.model.DelugeTorrentData
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.model.DownloadItem
import com.dnfapps.arrmatey.downloadclient.model.DownloadItemStatus
import com.dnfapps.arrmatey.downloadclient.model.DownloadTransferInfo
import io.ktor.client.HttpClient
import io.ktor.http.contentType
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

class DelugeClient(
    private val downloadClient: DownloadClient,
    private val httpClient: HttpClient
): DownloadClientApi {

    private var authenticated: Boolean = false
    private var requestId: Int = 0

    override suspend fun testConnection(): NetworkResult<Unit> {
        return ensureAuthenticated()
    }

    override suspend fun getDownloads(): NetworkResult<List<DownloadItem>> {
        return when (val authResult = ensureAuthenticated()) {
            is NetworkResult.Success -> {
                when (
                    val torrentsResult = callDeluge<Map<String, DelugeTorrentData>>(
                        method = "core.get_torrents_status",
                        params = listOf(
                            buildJsonObject {},
                            buildJsonArray {
                                add(JsonPrimitive("name"))
                                add(JsonPrimitive("total_size"))
                                add(JsonPrimitive("progress"))
                                add(JsonPrimitive("download_payload_rate"))
                                add(JsonPrimitive("upload_payload_rate"))
                                add(JsonPrimitive("eta"))
                                add(JsonPrimitive("state"))
                                add(JsonPrimitive("label"))
                                add(JsonPrimitive("time_added"))
                                add(JsonPrimitive("hash"))
                            }
                        )
                    )
                ) {
                    is NetworkResult.Success -> {
                        when (val rpcResult = torrentsResult.data.resultOrError()) {
                            is NetworkResult.Success -> {
                                NetworkResult.Success(rpcResult.data.values.map { it.toDownloadItem() })
                            }
                            is NetworkResult.Error -> rpcResult
                            is NetworkResult.Loading -> rpcResult
                            else -> NetworkResult.Error(message = "Unexpected Deluge torrents state")
                        }
                    }
                    is NetworkResult.Error -> torrentsResult
                    is NetworkResult.Loading -> torrentsResult
                    else -> NetworkResult.Error(message = "Unexpected Deluge request state")
                }
            }
            is NetworkResult.Error -> authResult
            is NetworkResult.Loading -> NetworkResult.Loading
            else -> NetworkResult.Error(message = "Unexpected authentication state")
        }
    }

    override suspend fun pauseDownload(id: String): NetworkResult<Unit> {
        return when (val authResult = ensureAuthenticated()) {
            is NetworkResult.Success -> {
                executeTorrentAction(
                    method = "core.pause_torrent",
                    id = id
                )
            }
            is NetworkResult.Error -> authResult
            is NetworkResult.Loading -> NetworkResult.Loading
            else -> NetworkResult.Error(message = "Unexpected authentication state")
        }
    }

    override suspend fun resumeDownload(id: String): NetworkResult<Unit> {
        return when (val authResult = ensureAuthenticated()) {
            is NetworkResult.Success -> {
                executeTorrentAction(
                    method = "core.resume_torrent",
                    id = id
                )
            }
            is NetworkResult.Error -> authResult
            is NetworkResult.Loading -> NetworkResult.Loading
            else -> NetworkResult.Error(message = "Unexpected authentication state")
        }
    }

    override suspend fun deleteDownload(id: String, deleteFiles: Boolean): NetworkResult<Unit> {
        return when (val authResult = ensureAuthenticated()) {
            is NetworkResult.Success -> {
                when (
                    val result = callDeluge<JsonElement>(
                        method = "core.remove_torrent",
                        params = listOf(JsonPrimitive(id), JsonPrimitive(deleteFiles))
                    )
                ) {
                    is NetworkResult.Success -> result.data.toUnitResult()
                    is NetworkResult.Error -> result
                    is NetworkResult.Loading -> result
                    else -> NetworkResult.Error(message = "Unexpected Deluge delete state")
                }
            }
            is NetworkResult.Error -> authResult
            is NetworkResult.Loading -> NetworkResult.Loading
            else -> NetworkResult.Error(message = "Unexpected authentication state")
        }
    }

    override suspend fun getTransferInfo(): NetworkResult<DownloadTransferInfo> {
        return when (val authResult = ensureAuthenticated()) {
            is NetworkResult.Success -> {
                when (
                    val result = callDeluge<DelugeSessionStatus>(
                        method = "core.get_session_status",
                        params = listOf(
                            buildJsonArray {
                                add(JsonPrimitive("download_rate"))
                                add(JsonPrimitive("upload_rate"))
                            }
                        )
                    )
                ) {
                    is NetworkResult.Success -> {
                        when (val rpcResult = result.data.resultOrError()) {
                            is NetworkResult.Success -> {
                                NetworkResult.Success(
                                    DownloadTransferInfo(
                                        client = downloadClient,
                                        downloadSpeed = rpcResult.data.downloadRate,
                                        uploadSpeed = rpcResult.data.uploadRate
                                    )
                                )
                            }
                            is NetworkResult.Error -> rpcResult
                            is NetworkResult.Loading -> rpcResult
                            else -> NetworkResult.Error(message = "Unexpected Deluge transfer state")
                        }
                    }
                    is NetworkResult.Error -> result
                    is NetworkResult.Loading -> result
                    else -> NetworkResult.Error(message = "Unexpected Deluge transfer state")
                }
            }
            is NetworkResult.Error -> authResult
            is NetworkResult.Loading -> NetworkResult.Loading
            else -> NetworkResult.Error(message = "Unexpected authentication state")
        }
    }

    private suspend fun ensureAuthenticated(): NetworkResult<Unit> {
        if (authenticated) return NetworkResult.Success(Unit)

        val loginResult = callDeluge<Boolean>(
            method = "auth.login",
            params = listOf(JsonPrimitive(downloadClient.password))
        )

        return when (loginResult) {
            is NetworkResult.Success -> {
                when (val rpcResult = loginResult.data.resultOrError()) {
                    is NetworkResult.Success -> {
                        if (rpcResult.data) {
                            authenticated = true
                            NetworkResult.Success(Unit)
                        } else {
                            authenticated = false
                            NetworkResult.Error(message = "Deluge authentication failed")
                        }
                    }
                    is NetworkResult.Error -> {
                        authenticated = false
                        rpcResult
                    }
                    is NetworkResult.Loading -> rpcResult
                    else -> NetworkResult.Error(message = "Unexpected Deluge auth state")
                }
            }
            is NetworkResult.Error -> {
                authenticated = false
                loginResult
            }
            is NetworkResult.Loading -> loginResult
            else -> NetworkResult.Error(message = "Unexpected Deluge login state")
        }
    }

    private suspend fun executeTorrentAction(method: String, id: String): NetworkResult<Unit> {
        return when (
            val result = callDeluge<JsonElement>(
                method = method,
                params = listOf(
                    buildJsonArray {
                        add(JsonPrimitive(id))
                    }
                )
            )
        ) {
            is NetworkResult.Success -> result.data.toUnitResult()
            is NetworkResult.Error -> result
            is NetworkResult.Loading -> result
            else -> NetworkResult.Error(message = "Unexpected Deluge torrent action state")
        }
    }

    private suspend inline fun <reified T> callDeluge(
        method: String,
        params: List<JsonElement> = emptyList()
    ): NetworkResult<DelugeJsonRpcResponse<T>> {
        return httpClient.safePost("json") {
            contentType(ContentType.Application.Json)
            setBody(
                DelugeJsonRpcRequest(
                    method = method,
                    params = params,
                    id = nextRequestId()
                )
            )
        }
    }

    private fun <T> DelugeJsonRpcResponse<T>.resultOrError(): NetworkResult<T> {
        if (error != null && error != JsonNull) {
            return NetworkResult.Error(message = "Deluge RPC error: $error")
        }

        val rpcResult = result
            ?: return NetworkResult.Error(message = "Missing Deluge RPC result")

        return NetworkResult.Success(rpcResult)
    }

    private fun DelugeJsonRpcResponse<JsonElement>.toUnitResult(): NetworkResult<Unit> {
        return if (error != null && error != JsonNull) {
            NetworkResult.Error(message = "Deluge RPC error: $error")
        } else {
            NetworkResult.Success(Unit)
        }
    }

    private fun DelugeTorrentData.toDownloadItem(): DownloadItem {
        return DownloadItem(
            client = downloadClient,
            id = hash,
            name = name,
            size = totalSize,
            progress = (progress / 100.0).coerceIn(0.0, 1.0),
            downloadSpeed = downloadPayloadRate,
            uploadSpeed = uploadPayloadRate,
            eta = eta,
            status = state.toDownloadStatus(),
            category = label,
            addedOn = timeAdded
        )
    }

    private fun String.toDownloadStatus(): DownloadItemStatus {
        return when {
            contains("paused", ignoreCase = true) -> DownloadItemStatus.Paused
            contains("queued", ignoreCase = true) -> DownloadItemStatus.Queued
            contains("error", ignoreCase = true) -> DownloadItemStatus.Failed
            contains("seeding", ignoreCase = true) -> DownloadItemStatus.Seeding
            contains("finished", ignoreCase = true) -> DownloadItemStatus.Completed
            contains("downloading", ignoreCase = true) -> DownloadItemStatus.Downloading
            contains("checking", ignoreCase = true) -> DownloadItemStatus.Queued
            contains("allocating", ignoreCase = true) -> DownloadItemStatus.Queued
            else -> DownloadItemStatus.Queued
        }
    }

    private fun nextRequestId(): Int {
        requestId += 1
        return requestId
    }
}
