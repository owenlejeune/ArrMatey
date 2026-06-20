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
                            append("username", downloadClient.username.value)
                            append("password", downloadClient.password.value)
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
            downloaded = downloaded,
            progress = progress,
            downloadSpeed = downloadSpeed,
            uploadSpeed = uploadSpeed,
            eta = eta,
            status = DownloadItemStatus.from(state),
            category = category,
            addedOn = addedOn,
            tags = tags.split(",").map { it.trim() }.filter { it.isNotBlank() }
        )
    }
}
