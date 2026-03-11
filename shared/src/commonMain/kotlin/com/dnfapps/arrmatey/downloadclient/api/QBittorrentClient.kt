package com.dnfapps.arrmatey.downloadclient.api

import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.client.safeCall
import com.dnfapps.arrmatey.client.safeGet
import com.dnfapps.arrmatey.downloadclient.api.model.QBittorrentTorrent
import com.dnfapps.arrmatey.downloadclient.api.model.QBittorrentTransferInfoResponse
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.model.DownloadItem
import com.dnfapps.arrmatey.downloadclient.model.DownloadItemStatus
import com.dnfapps.arrmatey.downloadclient.model.DownloadTransferInfo
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Parameters

class QBittorrentClient(
    private val downloadClient: DownloadClient,
    private val httpClient: HttpClient
): DownloadClientApi {

    private var authenticated: Boolean = false

    override suspend fun testConnection(): NetworkResult<Unit> {
        return ensureAuthenticated()
    }

    override suspend fun getDownloads(): NetworkResult<List<DownloadItem>> {
        return when (val authResult = ensureAuthenticated()) {
            is NetworkResult.Success -> {
                httpClient.safeGet<List<QBittorrentTorrent>>("api/v2/torrents/info")
                    .map { torrents -> torrents.map { it.toDownloadItem() } }
            }
            is NetworkResult.Error -> authResult
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }

    override suspend fun pauseDownload(id: String): NetworkResult<Unit> {
        return when (val authResult = ensureAuthenticated()) {
            is NetworkResult.Success -> postTorrentAction("api/v2/torrents/stop", id)
            is NetworkResult.Error -> authResult
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }

    override suspend fun resumeDownload(id: String): NetworkResult<Unit> {
        return when (val authResult = ensureAuthenticated()) {
            is NetworkResult.Success -> postTorrentAction("api/v2/torrents/start", id)
            is NetworkResult.Error -> authResult
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }

    override suspend fun deleteDownload(id: String, deleteFiles: Boolean): NetworkResult<Unit> {
        return when (val authResult = ensureAuthenticated()) {
            is NetworkResult.Success -> {
                httpClient.safeCall {
                    post("api/v2/torrents/delete") {
                        setBody(
                            FormDataContent(
                                Parameters.build {
                                    append("hashes", id)
                                    append("deleteFiles", deleteFiles.toString())
                                }
                            )
                        )
                    }
                    Unit
                }
            }
            is NetworkResult.Error -> authResult
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }

    override suspend fun getTransferInfo(): NetworkResult<DownloadTransferInfo> {
        return when (val authResult = ensureAuthenticated()) {
            is NetworkResult.Success -> {
                httpClient.safeGet<QBittorrentTransferInfoResponse>("api/v2/transfer/info")
                    .map { info ->
                        DownloadTransferInfo(
                            client = downloadClient,
                            downloadSpeed = info.downloadSpeed,
                            uploadSpeed = info.uploadSpeed
                        )
                    }
            }
            is NetworkResult.Error -> authResult
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }

    private suspend fun ensureAuthenticated(): NetworkResult<Unit> {
        if (authenticated) return NetworkResult.Success(Unit)

        val loginResult = httpClient.safeCall {
            post("api/v2/auth/login") {
                setBody(
                    FormDataContent(
                        Parameters.build {
                            append("username", downloadClient.username)
                            append("password", downloadClient.password)
                        }
                    )
                )
            }
            Unit
        }

        return when (loginResult) {
            is NetworkResult.Success -> {
                authenticated = true
                NetworkResult.Success(Unit)
            }
            is NetworkResult.Error -> {
                authenticated = false
                loginResult
            }
            is NetworkResult.Loading -> loginResult
        }
    }

    private suspend fun postTorrentAction(endpoint: String, hash: String): NetworkResult<Unit> {
        return httpClient.safeCall {
            post(endpoint) {
                setBody(
                    FormDataContent(
                        Parameters.build {
                            append("hashes", hash)
                        }
                    )
                )
            }
            Unit
        }
    }

    private fun QBittorrentTorrent.toDownloadItem(): DownloadItem {
        return DownloadItem(
            client = downloadClient,
            id = hash,
            name = name,
            size = size,
            progress = progress,
            downloadSpeed = downloadSpeed,
            uploadSpeed = uploadSpeed,
            eta = eta,
            status = state.toDownloadStatus(),
            category = category,
            addedOn = addedOn
        )
    }

    private fun String.toDownloadStatus(): DownloadItemStatus {
        return when {
            contains("paused", ignoreCase = true) -> DownloadItemStatus.Paused
            contains("stopped", ignoreCase = true) -> DownloadItemStatus.Paused
            contains("queued", ignoreCase = true) -> DownloadItemStatus.Queued
            contains("error", ignoreCase = true) -> DownloadItemStatus.Failed
            contains("missingfiles", ignoreCase = true) -> DownloadItemStatus.Failed
            contains("stalled", ignoreCase = true) -> DownloadItemStatus.Stalled
            contains("upload", ignoreCase = true) -> DownloadItemStatus.Seeding
            contains("downloading", ignoreCase = true) -> DownloadItemStatus.Downloading
            contains("metaDL", ignoreCase = true) -> DownloadItemStatus.Downloading
            contains("checking", ignoreCase = true) -> DownloadItemStatus.Queued
            contains("allocating", ignoreCase = true) -> DownloadItemStatus.Queued
            contains("moving", ignoreCase = true) -> DownloadItemStatus.Queued
            contains("queuedDL", ignoreCase = true) -> DownloadItemStatus.Queued
            contains("queuedUP", ignoreCase = true) -> DownloadItemStatus.Queued
            contains("forcedUP", ignoreCase = true) -> DownloadItemStatus.Seeding
            contains("uploading", ignoreCase = true) -> DownloadItemStatus.Seeding
            contains("completed", ignoreCase = true) -> DownloadItemStatus.Completed
            else -> DownloadItemStatus.Queued
        }
    }
}
