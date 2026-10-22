package com.dnfapps.arrmatey.downloadclient.api

import com.dnfapps.arrmatey.arr.api.model.ArrDiskSpace
import com.dnfapps.arrmatey.compose.utils.toFormattedDuration
import com.dnfapps.arrmatey.downloadclient.api.model.NzbgetEditQueueAction
import com.dnfapps.arrmatey.downloadclient.api.model.NzbgetEditQueueParams
import com.dnfapps.arrmatey.downloadclient.api.model.NzbgetGroup
import com.dnfapps.arrmatey.downloadclient.api.model.NzbgetHistoryItem
import com.dnfapps.arrmatey.downloadclient.api.model.NzbgetJsonRpcRequest
import com.dnfapps.arrmatey.downloadclient.api.model.NzbgetJsonRpcResponse
import com.dnfapps.arrmatey.downloadclient.api.model.NzbgetStatus
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.model.DownloadClientSystemOverview
import com.dnfapps.arrmatey.downloadclient.model.DownloadItem
import com.dnfapps.arrmatey.downloadclient.model.DownloadItemStatus
import com.dnfapps.arrmatey.downloadclient.model.DownloadTransferInfo
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.safePost
import io.ktor.client.HttpClient
import io.ktor.client.request.basicAuth
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class NzbgetClient(
    private val downloadClient: DownloadClient,
    private val httpClient: HttpClient,
) : DownloadClientApi {
    private var requestId = 0

    private fun nextRequestId(): Int = ++requestId

    override suspend fun testConnection(): NetworkResult<Unit> = when (val result = callRpc<String>("version")) {
        is NetworkResult.Success -> NetworkResult.Success(Unit)
        is NetworkResult.Error -> result
        is NetworkResult.Loading -> result
    }

    override suspend fun getDownloads(): NetworkResult<List<DownloadItem>> = coroutineScope {
        val groupsDeferred = async { callRpc<List<NzbgetGroup>>("listgroups") }
        val statusDeferred = async { callRpc<NzbgetStatus>("status") }

        when (val groupsResult = groupsDeferred.await()) {
            is NetworkResult.Success -> {
                val statusResult = statusDeferred.await()
                val downloadSpeed = (statusResult as? NetworkResult.Success)?.data?.currentDownloadSpeed ?: 0L

                val items = groupsResult.data.map { group ->
                    val status = DownloadItemStatus.from(group.status, group.paused)
                    val isDownloading = status == DownloadItemStatus.Downloading
                    val itemSpeed = if (isDownloading) downloadSpeed else 0L
                    val eta = if (isDownloading && downloadSpeed > 0L) {
                        group.remainingSize / downloadSpeed
                    } else {
                        0L
                    }

                    DownloadItem(
                        client = downloadClient,
                        id = group.nzbId.toString(),
                        name = group.displayName,
                        size = group.totalSize,
                        downloaded = group.downloadedSize,
                        progress = group.progress,
                        downloadSpeed = itemSpeed,
                        uploadSpeed = 0L,
                        status = status,
                        category = group.category,
                        addedOn = if (group.maxPostTime > 0) group.maxPostTime else group.minPostTime,
                        eta = eta,
                        etaString = eta.toFormattedDuration(),
                    )
                }
                NetworkResult.Success(items)
            }
            is NetworkResult.Error -> groupsResult
            is NetworkResult.Loading -> groupsResult
        }
    }

    override suspend fun pauseDownload(ids: List<String>): NetworkResult<Unit> {
        val intIds = ids.mapNotNull { it.toIntOrNull() }
        if (intIds.isEmpty()) return NetworkResult.Success(Unit)
        val params = NzbgetEditQueueParams(action = NzbgetEditQueueAction.GroupPause, ids = intIds)
        return when (val result = callRpc<NzbgetEditQueueParams, Boolean>("editqueue", params)) {
            is NetworkResult.Success -> {
                if (result.data) {
                    NetworkResult.Success(Unit)
                } else {
                    NetworkResult.Error(message = ERROR_PAUSE_FAILED)
                }
            }
            is NetworkResult.Error -> result
            is NetworkResult.Loading -> result
        }
    }

    override suspend fun resumeDownload(ids: List<String>): NetworkResult<Unit> {
        val intIds = ids.mapNotNull { it.toIntOrNull() }
        if (intIds.isEmpty()) return NetworkResult.Success(Unit)
        val params = NzbgetEditQueueParams(action = NzbgetEditQueueAction.GroupResume, ids = intIds)
        return when (val result = callRpc<NzbgetEditQueueParams, Boolean>("editqueue", params)) {
            is NetworkResult.Success -> {
                if (result.data) {
                    NetworkResult.Success(Unit)
                } else {
                    NetworkResult.Error(message = ERROR_RESUME_FAILED)
                }
            }
            is NetworkResult.Error -> result
            is NetworkResult.Loading -> result
        }
    }

    override suspend fun deleteDownload(
        ids: List<String>,
        deleteFiles: Boolean,
    ): NetworkResult<Unit> {
        val intIds = ids.mapNotNull { it.toIntOrNull() }
        if (intIds.isEmpty()) return NetworkResult.Success(Unit)
        val action = if (deleteFiles) NzbgetEditQueueAction.GroupDelete else NzbgetEditQueueAction.GroupParkDelete
        val params = NzbgetEditQueueParams(action = action, ids = intIds)
        return when (val result = callRpc<NzbgetEditQueueParams, Boolean>("editqueue", params)) {
            is NetworkResult.Success -> {
                if (result.data) {
                    NetworkResult.Success(Unit)
                } else {
                    NetworkResult.Error(message = ERROR_DELETE_FAILED)
                }
            }
            is NetworkResult.Error -> result
            is NetworkResult.Loading -> result
        }
    }

    override suspend fun getTransferInfo(): NetworkResult<DownloadTransferInfo> = when (val result = callRpc<NzbgetStatus>("status")) {
        is NetworkResult.Success -> {
            NetworkResult.Success(
                DownloadTransferInfo(
                    client = downloadClient,
                    downloadSpeed = result.data.currentDownloadSpeed,
                    uploadSpeed = 0L,
                    externalIp = null,
                ),
            )
        }
        is NetworkResult.Error -> result
        is NetworkResult.Loading -> result
    }

    override suspend fun getSystemOverview(): NetworkResult<DownloadClientSystemOverview> = coroutineScope {
        val versionDeferred = async { callRpc<String>("version") }
        val statusDeferred = async { callRpc<NzbgetStatus>("status") }
        val downloadsDeferred = async { getDownloads() }

        val version = (versionDeferred.await() as? NetworkResult.Success)?.data?.trim()
        val status = (statusDeferred.await() as? NetworkResult.Success)?.data
        val downloads = (downloadsDeferred.await() as? NetworkResult.Success)?.data.orEmpty()
        val activeCount = downloads.count { it.status == DownloadItemStatus.Downloading }

        val freeSpaceBytes = (status?.freeDiskSpaceMB ?: 0L) * 1024L * 1024L
        val diskSpaces = if (freeSpaceBytes > 0L) {
            listOf(ArrDiskSpace(path = null, label = "Free Disk Space", freeSpace = freeSpaceBytes, totalSpace = 0L))
        } else {
            emptyList()
        }

        val transferInfo = DownloadTransferInfo(
            client = downloadClient,
            downloadSpeed = status?.currentDownloadSpeed ?: 0L,
            uploadSpeed = 0L,
        )

        NetworkResult.Success(
            DownloadClientSystemOverview(
                version = version,
                diskSpaces = diskSpaces,
                transferInfo = transferInfo,
                activeDownloadsCount = activeCount,
            ),
        )
    }

    suspend fun getHistory(hidden: Boolean = false): NetworkResult<List<NzbgetHistoryItem>> = callRpc("history", listOf(hidden))

    private suspend inline fun <reified T> callRpc(
        method: String,
    ): NetworkResult<T> = callRpc<List<String>, T>(method, emptyList())

    private suspend inline fun <reified P, reified T> callRpc(
        method: String,
        params: P,
    ): NetworkResult<T> {
        val endpoint = if (downloadClient.getEffectiveBaseUrl().trimEnd('/').endsWith(ENDPOINT_JSON_RPC)) "" else ENDPOINT_JSON_RPC
        val result = httpClient.safePost<NzbgetJsonRpcResponse<T>>(endpoint) {
            contentType(ContentType.Application.Json)
            val username = downloadClient.username.value.ifEmpty {
                if (downloadClient.password.value.isEmpty() && downloadClient.apiKey.value.isNotEmpty()) DEFAULT_USERNAME else ""
            }
            val password = downloadClient.password.value.ifEmpty { downloadClient.apiKey.value }
            if (username.isNotEmpty() || password.isNotEmpty()) {
                basicAuth(username, password)
            }
            setBody(
                NzbgetJsonRpcRequest(
                    method = method,
                    params = params,
                    id = nextRequestId(),
                ),
            )
        }

        return when (result) {
            is NetworkResult.Success -> result.data.resultOrError()
            is NetworkResult.Error -> result
            is NetworkResult.Loading -> result
        }
    }

    private fun <T> NzbgetJsonRpcResponse<T>.resultOrError(): NetworkResult<T> {
        if (error != null) {
            val message = error.message ?: "code ${error.code}"
            return NetworkResult.Error(message = "$ERROR_RPC_PREFIX$message")
        }

        return if (result != null) {
            NetworkResult.Success(result)
        } else {
            NetworkResult.Error(message = ERROR_NULL_RESULT)
        }
    }

    companion object {
        private const val DEFAULT_USERNAME = "nzbget"
        private const val ENDPOINT_JSON_RPC = "jsonrpc"
        private const val ERROR_PAUSE_FAILED = "NZBGet pause failed"
        private const val ERROR_RESUME_FAILED = "NZBGet resume failed"
        private const val ERROR_DELETE_FAILED = "NZBGet delete failed"
        private const val ERROR_RPC_PREFIX = "NZBGet RPC error: "
        private const val ERROR_NULL_RESULT = "NZBGet RPC returned null result"
    }
}
