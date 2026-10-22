package com.dnfapps.arrmatey.downloadclient.api

import com.dnfapps.arrmatey.arr.api.model.ArrDiskSpace
import com.dnfapps.arrmatey.compose.utils.toSeconds
import com.dnfapps.arrmatey.downloadclient.api.model.SABnzbdHistoryResponse
import com.dnfapps.arrmatey.downloadclient.api.model.SABnzbdQueueResponse
import com.dnfapps.arrmatey.downloadclient.api.model.SABnzbdStatusResponse
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.model.DownloadClientSystemOverview
import com.dnfapps.arrmatey.downloadclient.model.DownloadItem
import com.dnfapps.arrmatey.downloadclient.model.DownloadItemStatus
import com.dnfapps.arrmatey.downloadclient.model.DownloadTransferInfo
import com.dnfapps.arrmatey.instances.model.UpdateInfo
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.safeGet
import com.dnfapps.networking.safePost
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.max

class SABnzbdClient(
    private val downloadClient: DownloadClient,
    private val httpClient: HttpClient,
) : DownloadClientApi {
    override suspend fun testConnection(): NetworkResult<Unit> = httpClient
        .safeGet<SABnzbdQueueResponse>("api") {
            parameter("mode", "queue")
            parameter("apikey", downloadClient.apiKey.value)
            parameter("output", "json")
        }.map { }

    override suspend fun getDownloads(): NetworkResult<List<DownloadItem>> {
        val queueResult =
            httpClient.safeGet<SABnzbdQueueResponse>("api") {
                parameter("mode", "queue")
                parameter("apikey", downloadClient.apiKey.value)
                parameter("output", "json")
            }

        return when (queueResult) {
            is NetworkResult.Success -> {
                val queueItems =
                    queueResult.data.queue.slots.map { slot ->
                        val totalMb = slot.mb.toDoubleOrNull() ?: 0.0
                        val leftMb = slot.mbLeft.toDoubleOrNull() ?: 0.0
                        val totalBytes = (max(totalMb, 0.0) * 1024 * 1024).toLong()
                        val progress =
                            if (totalMb <= 0.0) {
                                0.0
                            } else {
                                ((totalMb - leftMb) / totalMb).coerceIn(0.0, 1.0)
                            }

                        DownloadItem(
                            client = downloadClient,
                            id = slot.nzoId,
                            name = slot.filename,
                            size = totalBytes,
                            progress = progress,
                            downloaded = (totalBytes.toDouble() * progress).toLong(),
                            downloadSpeed = 0,
                            uploadSpeed = 0,
                            eta = slot.timeLeft.toSeconds(),
                            etaString = slot.timeLeft,
                            status = DownloadItemStatus.from(slot.status),
                            category = slot.category,
                            addedOn = slot.added,
                        )
                    }
                NetworkResult.Success(queueItems)
            }
            is NetworkResult.Error -> queueResult
            is NetworkResult.Loading -> queueResult
        }
    }

    override suspend fun pauseDownload(ids: List<String>): NetworkResult<Unit> {
        var lastError: NetworkResult.Error? = null
        for (id in ids) {
            val result =
                httpClient
                    .safePost<SABnzbdStatusResponse>("api") {
                        parameter("mode", "pause")
                        parameter("value", id)
                        parameter("apikey", downloadClient.apiKey.value)
                        parameter("output", "json")
                    }.map { }
            if (result is NetworkResult.Error) {
                lastError = result
            }
        }
        return lastError ?: NetworkResult.Success(Unit)
    }

    override suspend fun resumeDownload(ids: List<String>): NetworkResult<Unit> {
        var lastError: NetworkResult.Error? = null
        for (id in ids) {
            val result =
                httpClient
                    .safePost<SABnzbdStatusResponse>("api") {
                        parameter("mode", "resume")
                        parameter("value", id)
                        parameter("apikey", downloadClient.apiKey.value)
                        parameter("output", "json")
                    }.map { }
            if (result is NetworkResult.Error) {
                lastError = result
            }
        }
        return lastError ?: NetworkResult.Success(Unit)
    }

    override suspend fun deleteDownload(
        ids: List<String>,
        deleteFiles: Boolean,
    ): NetworkResult<Unit> {
        var lastError: NetworkResult.Error? = null
        for (id in ids) {
            val result =
                httpClient.safePost<SABnzbdStatusResponse>("api") {
                    parameter("mode", "queue")
                    parameter("name", "delete")
                    parameter("value", id)
                    parameter("del_files", if (deleteFiles) "1" else "0")
                    parameter("apikey", downloadClient.apiKey.value)
                    parameter("output", "json")
                }
            if (result is NetworkResult.Error) {
                lastError = result
            }
        }
        return lastError ?: NetworkResult.Success(Unit)
    }

    override suspend fun getTransferInfo(): NetworkResult<DownloadTransferInfo> {
        val queueResult =
            httpClient.safeGet<SABnzbdQueueResponse>("api") {
                parameter("mode", "queue")
                parameter("apikey", downloadClient.apiKey.value)
                parameter("output", "json")
            }

        return when (queueResult) {
            is NetworkResult.Success -> {
                val speedKb =
                    queueResult.data.queue.kbPerSec
                        .toDoubleOrNull() ?: 0.0
                NetworkResult.Success(
                    DownloadTransferInfo(
                        client = downloadClient,
                        downloadSpeed = (speedKb * 1024).toLong(),
                        uploadSpeed = 0,
                    ),
                )
            }
            is NetworkResult.Error -> queueResult
            is NetworkResult.Loading -> queueResult
        }
    }

    override suspend fun getSystemOverview(): NetworkResult<DownloadClientSystemOverview> {
        val queueResult = httpClient.safeGet<JsonObject>("api") {
            parameter("mode", "queue")
            parameter("apikey", downloadClient.apiKey.value)
            parameter("output", "json")
        }

        val queueObj = (queueResult as? NetworkResult.Success)?.data?.get("queue")?.jsonObject
        val version = queueObj?.get("version")?.jsonPrimitive?.contentOrNull
        val speedKb = queueObj?.get("kbpersec")?.jsonPrimitive?.contentOrNull?.toDoubleOrNull() ?: 0.0
        val downloadSpeed = (speedKb * 1024).toLong()

        val diskSpaces = buildList {
            val free1 = queueObj?.get("diskspace1")?.jsonPrimitive?.contentOrNull?.toDoubleOrNull()
            val total1 = queueObj?.get("diskspacetotal1")?.jsonPrimitive?.contentOrNull?.toDoubleOrNull()
            if (free1 != null) {
                add(
                    ArrDiskSpace(
                        path = "Complete",
                        label = "Complete Folder",
                        freeSpace = (free1 * 1024 * 1024 * 1024).toLong(),
                        totalSpace = ((total1 ?: 0.0) * 1024 * 1024 * 1024).toLong(),
                    ),
                )
            }
            val free2 = queueObj?.get("diskspace2")?.jsonPrimitive?.contentOrNull?.toDoubleOrNull()
            val total2 = queueObj?.get("diskspacetotal2")?.jsonPrimitive?.contentOrNull?.toDoubleOrNull()
            if (free2 != null && free2 != free1) {
                add(
                    ArrDiskSpace(
                        path = "Incomplete",
                        label = "Incomplete Folder",
                        freeSpace = (free2 * 1024 * 1024 * 1024).toLong(),
                        totalSpace = ((total2 ?: 0.0) * 1024 * 1024 * 1024).toLong(),
                    ),
                )
            }
        }

        val slots = queueObj?.get("slots")?.jsonArray
        val activeCount = slots?.size ?: 0

        val transferInfo = DownloadTransferInfo(
            client = downloadClient,
            downloadSpeed = downloadSpeed,
            uploadSpeed = 0L,
        )

        val versionResult = httpClient.safeGet<JsonObject>("api") {
            parameter("mode", "version")
            parameter("apikey", downloadClient.apiKey.value)
            parameter("output", "json")
        }
        var updateInfo: UpdateInfo? = null
        if (versionResult is NetworkResult.Success) {
            val curVer = versionResult.data["version"]?.jsonPrimitive?.contentOrNull
            val newVer = versionResult.data["new_ver"]?.jsonPrimitive?.contentOrNull
            val newRelUrl = versionResult.data["new_rel_url"]?.jsonPrimitive?.contentOrNull
            if (!newVer.isNullOrBlank() && newVer != curVer) {
                updateInfo = UpdateInfo(
                    isAvailable = true,
                    currentVersion = curVer ?: version,
                    latestVersion = newVer,
                    releaseUrl = newRelUrl,
                )
            }
        }

        return NetworkResult.Success(
            DownloadClientSystemOverview(
                version = version,
                diskSpaces = diskSpaces,
                transferInfo = transferInfo,
                activeDownloadsCount = activeCount,
                updateInfo = updateInfo,
            ),
        )
    }

    suspend fun getHistory(): NetworkResult<SABnzbdHistoryResponse> = httpClient.safeGet("api") {
        parameter("mode", "history")
        parameter("apikey", downloadClient.apiKey.value)
        parameter("output", "json")
    }
}
