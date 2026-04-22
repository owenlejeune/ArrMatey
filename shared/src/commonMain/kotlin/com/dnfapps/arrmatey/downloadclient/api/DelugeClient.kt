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
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonObject

class DelugeClient(
    private val downloadClient: DownloadClient,
    private val httpClient: HttpClient
): DownloadClientApi {

    private var authenticated: Boolean = false
    private var requestId: Int = 0

    override suspend fun testConnection(): NetworkResult<Unit> {
        // Reset authentication state for test
        authenticated = false
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
                        }
                    }
                    is NetworkResult.Error -> torrentsResult
                    is NetworkResult.Loading -> torrentsResult
                }
            }
            is NetworkResult.Error -> authResult
            is NetworkResult.Loading -> NetworkResult.Loading
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
                }
            }
            is NetworkResult.Error -> authResult
            is NetworkResult.Loading -> NetworkResult.Loading
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
                        }
                    }
                    is NetworkResult.Error -> result
                    is NetworkResult.Loading -> result
                }
            }
            is NetworkResult.Error -> authResult
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }

    private suspend fun ensureAuthenticated(): NetworkResult<Unit> {
        if (authenticated) return NetworkResult.Success(Unit)

        // Deluge requires an empty password string if no password is set
        val password = downloadClient.password.ifBlank { "" }

        val loginResult = callDeluge<Boolean>(
            method = "auth.login",
            params = listOf(JsonPrimitive(password))
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
                            NetworkResult.Error(
                                message = "Deluge authentication failed. Please check your password.",
                                code = 401
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        authenticated = false
                        NetworkResult.Error(
                            message = "Deluge authentication error: ${rpcResult.message}",
                            code = rpcResult.code
                        )
                    }
                    is NetworkResult.Loading -> rpcResult
                }
            }
            is NetworkResult.Error -> {
                authenticated = false
                NetworkResult.Error(
                    message = "Failed to connect to Deluge: ${loginResult.message}",
                    code = loginResult.code
                )
            }
            is NetworkResult.Loading -> loginResult
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
        }
    }

    private suspend inline fun <reified T> callDeluge(
        method: String,
        params: List<JsonElement> = emptyList()
    ): NetworkResult<DelugeJsonRpcResponse<T>> {
        // Try multiple possible endpoints
        val endpoints = listOf("json", "api/json", "/json")

        var lastError: NetworkResult.Error? = null

        for (endpoint in endpoints) {
            val result = httpClient.safePost<DelugeJsonRpcResponse<T>>(endpoint) {
                contentType(ContentType.Application.Json)
                setBody(
                    DelugeJsonRpcRequest(
                        method = method,
                        params = params,
                        id = nextRequestId()
                    )
                )
            }

            when (result) {
                is NetworkResult.Success -> return result
                is NetworkResult.Error -> {
                    // If we get a 404, try the next endpoint
                    if (result.code == 404) {
                        lastError = result
                        continue
                    }
                    // For other errors, return immediately
                    return result
                }
                else -> return result
            }
        }

        // If all endpoints failed with 404, return the last error
        return lastError ?: NetworkResult.Error(
            message = "Could not find Deluge JSON-RPC endpoint"
        )
    }

    private fun <T> DelugeJsonRpcResponse<T>.resultOrError(): NetworkResult<T> {
        // Check for RPC error
        if (error != null && error != JsonNull) {
            val errorMessage = try {
                when {
                    error is JsonPrimitive && error.isString ->
                        error.jsonPrimitive.content
                    error is JsonPrimitive ->
                        error.toString()
                    error.jsonObject.containsKey("message") ->
                        error.jsonObject["message"]?.jsonPrimitive?.content ?: "Unknown error"
                    else ->
                        error.toString()
                }
            } catch (e: Exception) {
                error.toString()
            }

            return NetworkResult.Error(
                message = "Deluge RPC error: $errorMessage"
            )
        }

        val rpcResult = result
            ?: return NetworkResult.Error(message = "Missing Deluge RPC result")

        return NetworkResult.Success(rpcResult)
    }

    private fun DelugeJsonRpcResponse<JsonElement>.toUnitResult(): NetworkResult<Unit> {
        return if (error != null && error != JsonNull) {
            val errorMessage = try {
                when {
                    error is JsonPrimitive && error.isString ->
                        error.jsonPrimitive.content
                    else ->
                        error.toString()
                }
            } catch (e: Exception) {
                error.toString()
            }

            NetworkResult.Error(message = "Deluge RPC error: $errorMessage")
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
            contains("moving", ignoreCase = true) -> DownloadItemStatus.Queued
            else -> DownloadItemStatus.Queued
        }
    }

    private fun nextRequestId(): Int {
        requestId += 1
        return requestId
    }
}