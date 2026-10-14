package com.dnfapps.arrmatey.downloadclient.api

import com.dnfapps.arrmatey.downloadclient.api.model.RTorrentDetail
import com.dnfapps.arrmatey.downloadclient.api.model.RTorrentFile
import com.dnfapps.arrmatey.downloadclient.api.model.RTorrentGlobal
import com.dnfapps.arrmatey.downloadclient.api.model.RTorrentPeer
import com.dnfapps.arrmatey.downloadclient.api.model.RTorrentTorrent
import com.dnfapps.arrmatey.downloadclient.api.model.RTorrentTracker
import com.dnfapps.arrmatey.downloadclient.api.model.XmlRpc
import com.dnfapps.arrmatey.downloadclient.api.model.XmlRpcFault
import com.dnfapps.arrmatey.downloadclient.api.model.XmlRpcValue
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.model.DownloadItem
import com.dnfapps.arrmatey.downloadclient.model.DownloadItemStatus
import com.dnfapps.arrmatey.downloadclient.model.DownloadTransferInfo
import com.dnfapps.networking.NetworkResult
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.basicAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

class RTorrentClient(
    private val downloadClient: DownloadClient,
    private val httpClient: HttpClient,
) : DownloadClientApi {

    override suspend fun testConnection(): NetworkResult<Unit> = when (val result = call(METHOD_CLIENT_VERSION)) {
        is NetworkResult.Success -> NetworkResult.Success(Unit)
        is NetworkResult.Error -> result
        is NetworkResult.Loading -> result
    }

    override suspend fun getDownloads(): NetworkResult<List<DownloadItem>> {
        val params = listOf("", DEFAULT_VIEW) + RTorrentTorrent.FIELDS

        return when (val result = call(METHOD_MULTICALL2, params)) {
            is NetworkResult.Success -> {
                val list = result.data.asList()
                val torrents = list.mapNotNull { item ->
                    if (item is XmlRpcValue.ArrayValue) {
                        RTorrentTorrent.fromRow(item.values).toDownloadItem()
                    } else {
                        null
                    }
                }
                NetworkResult.Success(torrents)
            }

            is NetworkResult.Error -> result
            is NetworkResult.Loading -> result
        }
    }

    override suspend fun pauseDownload(ids: List<String>): NetworkResult<Unit> {
        if (ids.isEmpty()) return NetworkResult.Success(Unit)
        return executeTorrentAction(METHOD_STOP, ids)
    }

    override suspend fun resumeDownload(ids: List<String>): NetworkResult<Unit> {
        if (ids.isEmpty()) return NetworkResult.Success(Unit)
        return executeTorrentAction(METHOD_START, ids)
    }

    override suspend fun deleteDownload(ids: List<String>, deleteFiles: Boolean): NetworkResult<Unit> {
        if (ids.isEmpty()) return NetworkResult.Success(Unit)
        return executeTorrentAction(METHOD_ERASE, ids)
    }

    override suspend fun getTransferInfo(): NetworkResult<DownloadTransferInfo> = when (val result = getGlobal()) {
        is NetworkResult.Success -> NetworkResult.Success(
            DownloadTransferInfo(
                client = downloadClient,
                downloadSpeed = result.data.downRate,
                uploadSpeed = result.data.upRate,
            ),
        )

        is NetworkResult.Error -> result
        is NetworkResult.Loading -> result
    }

    suspend fun getGlobal(): NetworkResult<RTorrentGlobal> {
        val commands = RTorrentGlobal.COMMANDS.map { cmd ->
            mapOf("methodName" to cmd, "params" to emptyList<Any>())
        }
        return when (val result = call(METHOD_SYSTEM_MULTICALL, listOf(commands))) {
            is NetworkResult.Success -> {
                val rows = result.data.asList()
                NetworkResult.Success(RTorrentGlobal.fromRows(rows))
            }

            is NetworkResult.Error -> result
            is NetworkResult.Loading -> result
        }
    }

    suspend fun recheck(hash: String): NetworkResult<Unit> = when (val result = call(METHOD_CHECK_HASH, listOf(hash))) {
        is NetworkResult.Success -> NetworkResult.Success(Unit)
        is NetworkResult.Error -> result
        is NetworkResult.Loading -> result
    }

    suspend fun reannounce(hash: String): NetworkResult<Unit> = when (val result = call(METHOD_TRACKER_ANNOUNCE, listOf(hash))) {
        is NetworkResult.Success -> NetworkResult.Success(Unit)
        is NetworkResult.Error -> result
        is NetworkResult.Loading -> result
    }

    suspend fun setPriority(hash: String, priority: Int): NetworkResult<Unit> = when (val result = call(METHOD_PRIORITY_SET, listOf(hash, priority))) {
        is NetworkResult.Success -> NetworkResult.Success(Unit)
        is NetworkResult.Error -> result
        is NetworkResult.Loading -> result
    }

    suspend fun setDownLimit(bytesPerSec: Long): NetworkResult<Unit> = when (val result = call(METHOD_THROTTLE_DOWN_SET, listOf("", bytesPerSec))) {
        is NetworkResult.Success -> NetworkResult.Success(Unit)
        is NetworkResult.Error -> result
        is NetworkResult.Loading -> result
    }

    suspend fun setUpLimit(bytesPerSec: Long): NetworkResult<Unit> = when (val result = call(METHOD_THROTTLE_UP_SET, listOf("", bytesPerSec))) {
        is NetworkResult.Success -> NetworkResult.Success(Unit)
        is NetworkResult.Error -> result
        is NetworkResult.Loading -> result
    }

    suspend fun close(hash: String): NetworkResult<Unit> = when (val result = call(METHOD_CLOSE, listOf(hash))) {
        is NetworkResult.Success -> NetworkResult.Success(Unit)
        is NetworkResult.Error -> result
        is NetworkResult.Loading -> result
    }

    suspend fun getDetail(hash: String): NetworkResult<RTorrentDetail> {
        val filesResult = rows(METHOD_F_MULTICALL, listOf(hash, "") + RTorrentFile.FIELDS) {
            RTorrentFile.fromRow(it)
        }
        if (filesResult is NetworkResult.Error) return filesResult
        val files = (filesResult as? NetworkResult.Success)?.data ?: emptyList()

        val trackersResult = rows(METHOD_T_MULTICALL, listOf(hash, "") + RTorrentTracker.FIELDS) {
            RTorrentTracker.fromRow(it)
        }
        if (trackersResult is NetworkResult.Error) return trackersResult
        val trackers = (trackersResult as? NetworkResult.Success)?.data ?: emptyList()

        // A stopped torrent has no peers and rTorrent faults rather than returning an empty list on some builds, so fail quietly
        val peersResult = rows(METHOD_P_MULTICALL, listOf(hash, "") + RTorrentPeer.FIELDS) {
            RTorrentPeer.fromRow(it)
        }
        val peers = (peersResult as? NetworkResult.Success)?.data ?: emptyList()

        return NetworkResult.Success(RTorrentDetail(files = files, trackers = trackers, peers = peers))
    }

    suspend fun setFilePriority(hash: String, index: Int, priority: Int): NetworkResult<Unit> {
        val setResult = call(METHOD_F_PRIORITY_SET, listOf("$hash:f$index", priority))
        if (setResult is NetworkResult.Error) return setResult
        return when (val updateResult = call(METHOD_UPDATE_PRIORITIES, listOf(hash))) {
            is NetworkResult.Success -> NetworkResult.Success(Unit)
            is NetworkResult.Error -> updateResult
            is NetworkResult.Loading -> updateResult
        }
    }

    suspend fun addUrl(
        urlOrMagnet: String,
        start: Boolean = true,
        directory: String? = null,
    ): NetworkResult<Unit> {
        val value = urlOrMagnet.trim()
        val dirCommands = directoryCommands(directory)
        val method = if (start) METHOD_LOAD_START_VERBOSE else METHOD_LOAD_VERBOSE
        return when (val result = call(method, listOf("", value) + dirCommands)) {
            is NetworkResult.Success -> NetworkResult.Success(Unit)
            is NetworkResult.Error -> result
            is NetworkResult.Loading -> result
        }
    }

    suspend fun addFile(
        bytes: ByteArray,
        start: Boolean = true,
        directory: String? = null,
    ): NetworkResult<Unit> {
        val dirCommands = directoryCommands(directory)
        val method = if (start) METHOD_LOAD_RAW_START_VERBOSE else METHOD_LOAD_RAW_VERBOSE
        return when (val result = call(method, listOf("", bytes) + dirCommands)) {
            is NetworkResult.Success -> NetworkResult.Success(Unit)
            is NetworkResult.Error -> result
            is NetworkResult.Loading -> result
        }
    }

    private fun directoryCommands(directory: String?): List<String> {
        val dir = directory?.trim().orEmpty()
        return if (dir.isEmpty()) emptyList() else listOf("d.directory.set=\"$dir\"")
    }

    private suspend fun <T> rows(
        method: String,
        params: List<Any?>,
        build: (List<XmlRpcValue>) -> T,
    ): NetworkResult<List<T>> = when (val result = call(method, params)) {
        is NetworkResult.Success -> {
            val list = result.data.asList().mapNotNull { item ->
                if (item is XmlRpcValue.ArrayValue) build(item.values) else null
            }
            NetworkResult.Success(list)
        }

        is NetworkResult.Error -> result
        is NetworkResult.Loading -> result
    }

    private suspend fun executeTorrentAction(method: String, ids: List<String>): NetworkResult<Unit> {
        if (ids.size == 1) {
            return when (val result = call(method, listOf(ids.first()))) {
                is NetworkResult.Success -> NetworkResult.Success(Unit)
                is NetworkResult.Error -> result
                is NetworkResult.Loading -> result
            }
        }
        val calls = ids.map { id ->
            mapOf("methodName" to method, "params" to listOf(id))
        }
        return when (val result = call(METHOD_SYSTEM_MULTICALL, listOf(calls))) {
            is NetworkResult.Success -> NetworkResult.Success(Unit)
            is NetworkResult.Error -> result
            is NetworkResult.Loading -> result
        }
    }

    private suspend fun call(method: String, params: List<Any?> = emptyList()): NetworkResult<XmlRpcValue> {
        return try {
            val response: HttpResponse = httpClient.post("") {
                contentType(ContentType.Text.Xml)
                if (downloadClient.username.value.isNotEmpty() || downloadClient.password.value.isNotEmpty()) {
                    basicAuth(downloadClient.username.value, downloadClient.password.value)
                }
                setBody(XmlRpc.buildCall(method, params))
            }

            val status = response.status.value
            if (status == 401 || status == 403) {
                return NetworkResult.Error(
                    code = status,
                    message = "rTorrent rejected the credentials (HTTP $status). Its XML-RPC has no login of its own, so this comes from the proxy in front of it: check the username and password, or leave both empty if it is not protected.",
                )
            }
            if (response.status != HttpStatusCode.OK) {
                return NetworkResult.Error(
                    code = status,
                    message = "rTorrent returned HTTP $status.",
                )
            }

            val body = response.bodyAsText()
            val parsed = XmlRpc.parseResponse(body)
            NetworkResult.Success(parsed)
        } catch (e: ClientRequestException) {
            val status = e.response.status.value
            if (status == 401 || status == 403) {
                NetworkResult.Error(
                    code = status,
                    message = "rTorrent rejected the credentials (HTTP $status). Its XML-RPC has no login of its own, so this comes from the proxy in front of it: check the username and password, or leave both empty if it is not protected.",
                )
            } else {
                NetworkResult.Error(
                    code = status,
                    message = "rTorrent returned HTTP $status.",
                )
            }
        } catch (e: ServerResponseException) {
            val status = e.response.status.value
            NetworkResult.Error(
                code = status,
                message = "rTorrent returned HTTP $status.",
            )
        } catch (e: ResponseException) {
            val status = e.response.status.value
            NetworkResult.Error(
                code = status,
                message = "rTorrent returned HTTP $status.",
            )
        } catch (e: XmlRpcFault) {
            NetworkResult.Error(
                code = e.faultCode,
                message = "rTorrent: ${e.faultString}",
            )
        } catch (e: IllegalArgumentException) {
            NetworkResult.Error(
                message = "rTorrent did not return XML-RPC (${e.message}). Point the URL at the SCGI/XML-RPC endpoint - often port 8000, or /RPC2 when ruTorrent is in front - rather than at the web UI.",
            )
        } catch (e: Exception) {
            NetworkResult.Error(
                message = e.message ?: "Unknown error",
            )
        }
    }

    private fun RTorrentTorrent.toDownloadItem(): DownloadItem = DownloadItem(
        client = downloadClient,
        id = hash,
        name = name,
        size = sizeBytes,
        downloaded = completedBytes,
        progress = progress,
        downloadSpeed = downRate,
        uploadSpeed = upRate,
        eta = eta,
        status = DownloadItemStatus.from(this),
        category = custom1.ifBlank { directory },
        addedOn = 0L,
    )

    companion object {
        const val DEFAULT_VIEW = "main"

        const val METHOD_CLIENT_VERSION = "system.client_version"
        const val METHOD_MULTICALL2 = "d.multicall2"
        const val METHOD_SYSTEM_MULTICALL = "system.multicall"
        const val METHOD_START = "d.start"
        const val METHOD_STOP = "d.stop"
        const val METHOD_CLOSE = "d.close"
        const val METHOD_ERASE = "d.erase"
        const val METHOD_CHECK_HASH = "d.check_hash"
        const val METHOD_TRACKER_ANNOUNCE = "d.tracker_announce"
        const val METHOD_PRIORITY_SET = "d.priority.set"
        const val METHOD_THROTTLE_DOWN_SET = "throttle.global_down.max_rate.set"
        const val METHOD_THROTTLE_UP_SET = "throttle.global_up.max_rate.set"
        const val METHOD_F_MULTICALL = "f.multicall"
        const val METHOD_T_MULTICALL = "t.multicall"
        const val METHOD_P_MULTICALL = "p.multicall"
        const val METHOD_F_PRIORITY_SET = "f.priority.set"
        const val METHOD_UPDATE_PRIORITIES = "d.update_priorities"
        const val METHOD_LOAD_START_VERBOSE = "load.start_verbose"
        const val METHOD_LOAD_VERBOSE = "load.verbose"
        const val METHOD_LOAD_RAW_START_VERBOSE = "load.raw_start_verbose"
        const val METHOD_LOAD_RAW_VERBOSE = "load.raw_verbose"
    }
}
