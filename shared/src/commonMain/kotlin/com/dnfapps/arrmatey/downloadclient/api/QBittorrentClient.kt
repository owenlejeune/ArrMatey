package com.dnfapps.arrmatey.downloadclient.api

import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.safeCall
import com.dnfapps.networking.safeGet
import com.dnfapps.arrmatey.downloadclient.api.model.QBittorrentTorrent
import com.dnfapps.arrmatey.downloadclient.api.model.QBittorrentTransferInfoResponse
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.model.DownloadItem
import com.dnfapps.arrmatey.downloadclient.model.DownloadItemStatus
import com.dnfapps.arrmatey.downloadclient.model.DownloadTransferInfo
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Parameters

class QBittorrentClient(
    private val downloadClient: DownloadClient,
    private val httpClient: HttpClient
): DownloadClientApi {

    private var authenticated: Boolean = false

    override suspend fun testConnection(): NetworkResult<Unit> {
        // Probe a real endpoint: /auth/login is unused with API keys and can pass even when data calls are rejected.
        return when (val authResult = ensureAuthenticated()) {
            is NetworkResult.Success -> {
                httpClient.safeCall<Unit> {
                    get("api/v2/app/version")
                    Unit
                }
            }
            is NetworkResult.Error -> authResult
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }

    override suspend fun getDownloads(): NetworkResult<List<DownloadItem>> =
        authenticatedCall {
            httpClient.safeGet<List<QBittorrentTorrent>>("api/v2/torrents/info")
                .map { torrents -> torrents.map { it.toDownloadItem() } }
        }

    override suspend fun pauseDownload(ids: List<String>): NetworkResult<Unit> =
        authenticatedCall { postTorrentAction("api/v2/torrents/stop", ids) }

    override suspend fun resumeDownload(ids: List<String>): NetworkResult<Unit> =
        authenticatedCall { postTorrentAction("api/v2/torrents/start", ids) }

    override suspend fun deleteDownload(ids: List<String>, deleteFiles: Boolean): NetworkResult<Unit> =
        authenticatedCall {
            httpClient.safeCall {
                post("api/v2/torrents/delete") {
                    setBody(
                        FormDataContent(
                            Parameters.build {
                                append("hashes", ids.joinToString("|"))
                                append("deleteFiles", deleteFiles.toString())
                            }
                        )
                    )
                }
                Unit
            }
        }

    override suspend fun getTransferInfo(): NetworkResult<DownloadTransferInfo> =
        authenticatedCall {
            httpClient.safeGet<QBittorrentTransferInfoResponse>("api/v2/transfer/info")
                .map { info ->
                    DownloadTransferInfo(
                        client = downloadClient,
                        downloadSpeed = info.downloadSpeed,
                        uploadSpeed = info.uploadSpeed
                    )
                }
        }

    // Re-login and retry once on 401/403 so an expired session cookie recovers automatically.
    private suspend fun <T> authenticatedCall(
        block: suspend () -> NetworkResult<T>
    ): NetworkResult<T> {
        when (val auth = ensureAuthenticated()) {
            is NetworkResult.Error -> return auth
            is NetworkResult.Loading -> return NetworkResult.Loading
            is NetworkResult.Success -> Unit
        }
        val first = block()
        if (first is NetworkResult.Error && (first.code == 401 || first.code == 403)) {
            authenticated = false
            when (val reAuth = ensureAuthenticated()) {
                is NetworkResult.Error -> return reAuth
                is NetworkResult.Loading -> return NetworkResult.Loading
                is NetworkResult.Success -> return block()
            }
        }
        return first
    }

    private suspend fun ensureAuthenticated(): NetworkResult<Unit> {
        if (authenticated) return NetworkResult.Success(Unit)

        // API-key auth is stateless via Authorization: Bearer; /auth/login rejects keys and repeated failures IP-ban.
        if (downloadClient.apiKey.value.isNotEmpty()) {
            authenticated = true
            return NetworkResult.Success(Unit)
        }

        // Nothing to log in with; rely on WebUI subnet whitelist / disabled auth.
        if (downloadClient.username.value.isEmpty() && downloadClient.password.value.isEmpty()) {
            authenticated = true
            return NetworkResult.Success(Unit)
        }

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

    private suspend fun postTorrentAction(endpoint: String, hashes: List<String>): NetworkResult<Unit> {
        return httpClient.safeCall {
            post(endpoint) {
                setBody(
                    FormDataContent(
                        Parameters.build {
                            append("hashes", hashes.joinToString("|"))
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
