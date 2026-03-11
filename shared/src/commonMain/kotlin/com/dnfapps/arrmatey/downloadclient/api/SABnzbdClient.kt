package com.dnfapps.arrmatey.downloadclient.api

import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.client.safeGet
import com.dnfapps.arrmatey.client.safePost
import com.dnfapps.arrmatey.compose.utils.toSeconds
import com.dnfapps.arrmatey.downloadclient.api.model.SABnzbdHistoryResponse
import com.dnfapps.arrmatey.downloadclient.api.model.SABnzbdQueueResponse
import com.dnfapps.arrmatey.downloadclient.api.model.SABnzbdStatusResponse
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.model.DownloadItem
import com.dnfapps.arrmatey.downloadclient.model.DownloadItemStatus
import com.dnfapps.arrmatey.downloadclient.model.DownloadTransferInfo
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import kotlin.math.max

class SABnzbdClient(
    private val downloadClient: DownloadClient,
    private val httpClient: HttpClient
): DownloadClientApi {

    override suspend fun testConnection(): NetworkResult<Unit> {
        return httpClient.safeGet<SABnzbdQueueResponse>("api") {
            parameter("mode", "queue")
            parameter("apikey", downloadClient.apiKey)
            parameter("output", "json")
        }.map { Unit }
    }

    override suspend fun getDownloads(): NetworkResult<List<DownloadItem>> {
        val queueResult = httpClient.safeGet<SABnzbdQueueResponse>("api") {
            parameter("mode", "queue")
            parameter("apikey", downloadClient.apiKey)
            parameter("output", "json")
        }

        return when (queueResult) {
            is NetworkResult.Success -> {
                val queueItems = queueResult.data.queue.slots.map { slot ->
                    val totalMb = slot.mb.toDoubleOrNull() ?: 0.0
                    val leftMb = slot.mbLeft.toDoubleOrNull() ?: 0.0
                    val totalBytes = (max(totalMb, 0.0) * 1024 * 1024).toLong()
                    val progress = if (totalMb <= 0.0) {
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
                        downloadSpeed = 0,
                        uploadSpeed = 0,
                        eta = slot.timeLeft.toSeconds(),
                        etaString = slot.timeLeft,
                        status = slot.status.toDownloadStatus(),
                        category = slot.category,
                        addedOn = slot.added
                    )
                }
                NetworkResult.Success(queueItems)
            }
            is NetworkResult.Error -> queueResult
            is NetworkResult.Loading -> queueResult
        }
    }

    override suspend fun pauseDownload(id: String): NetworkResult<Unit> {
        return httpClient.safePost<SABnzbdStatusResponse>("api") {
            parameter("mode", "pause")
            parameter("value", id)
            parameter("apikey", downloadClient.apiKey)
            parameter("output", "json")
        }.map { Unit }
    }

    override suspend fun resumeDownload(id: String): NetworkResult<Unit> {
        return httpClient.safePost<SABnzbdStatusResponse>("api") {
            parameter("mode", "resume")
            parameter("value", id)
            parameter("apikey", downloadClient.apiKey)
            parameter("output", "json")
        }.map { Unit }
    }

    override suspend fun deleteDownload(id: String, deleteFiles: Boolean): NetworkResult<Unit> {
        return httpClient.safePost<SABnzbdStatusResponse>("api") {
            parameter("mode", "queue")
            parameter("name", "delete")
            parameter("value", id)
            parameter("del_files", if (deleteFiles) "1" else "0")
            parameter("apikey", downloadClient.apiKey)
            parameter("output", "json")
        }.map { Unit }
    }

    override suspend fun getTransferInfo(): NetworkResult<DownloadTransferInfo> {
        val queueResult = httpClient.safeGet<SABnzbdQueueResponse>("api") {
            parameter("mode", "queue")
            parameter("apikey", downloadClient.apiKey)
            parameter("output", "json")
        }

        return when (queueResult) {
            is NetworkResult.Success -> {
                val speedKb = queueResult.data.queue.kbPerSec.toDoubleOrNull() ?: 0.0
                NetworkResult.Success(
                    DownloadTransferInfo(
                        client = downloadClient,
                        downloadSpeed = (speedKb * 1024).toLong(),
                        uploadSpeed = 0
                    )
                )
            }
            is NetworkResult.Error -> queueResult
            is NetworkResult.Loading -> queueResult
        }
    }

    suspend fun getHistory(): NetworkResult<SABnzbdHistoryResponse> {
        return httpClient.safeGet("api") {
            parameter("mode", "history")
            parameter("apikey", downloadClient.apiKey)
            parameter("output", "json")
        }
    }

    private fun String.toDownloadStatus(): DownloadItemStatus {
        return when {
            contains("downloading", ignoreCase = true) -> DownloadItemStatus.Downloading
            contains("paused", ignoreCase = true) -> DownloadItemStatus.Paused
            contains("queued", ignoreCase = true) -> DownloadItemStatus.Queued
            contains("failed", ignoreCase = true) -> DownloadItemStatus.Failed
            contains("completed", ignoreCase = true) -> DownloadItemStatus.Completed
            contains("extract", ignoreCase = true) -> DownloadItemStatus.Queued
            else -> DownloadItemStatus.Queued
        }
    }
}
