package com.dnfapps.arrmatey.downloads.api.client

import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.client.safeGet
import com.dnfapps.arrmatey.client.safePost
import com.dnfapps.arrmatey.downloads.api.model.DownloadClientStatus
import com.dnfapps.arrmatey.downloads.api.model.DownloadQueueItem
import com.dnfapps.arrmatey.instances.model.Instance
import io.ktor.client.HttpClient
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class QBittorrentClient(
    private val instance: Instance,
    private val httpClient: HttpClient
) : DownloadClient {

    private val apiBase = "${instance.url}/api/v2"

    override suspend fun getQueue(): NetworkResult<List<DownloadQueueItem>> =
        httpClient.safeGet<List<QBitTorrentInfo>>("$apiBase/torrents/info")
            .map { response ->
                response.map {
                    DownloadQueueItem(
                        id = it.hash,
                        name = it.name,
                        size = it.size,
                        progress = it.progress,
                        status = it.state,
                        speed = it.dlspeed,
                        eta = it.eta,
                        seeds = it.num_seeds,
                        peers = it.num_leechs,
                        category = it.category
                    )
                }
            }

    override suspend fun getStatus(): NetworkResult<DownloadClientStatus> =
        httpClient.safeGet<QBitMainData>("$apiBase/sync/maindata")
            .map { response ->
                DownloadClientStatus(
                    dlSpeed = response.server_state.dl_info_speed,
                    upSpeed = response.server_state.up_info_speed,
                    isPaused = response.server_state.pause_requests
                )
            }

    override suspend fun pause(id: String): NetworkResult<Unit> =
        httpClient.safePost<String>("$apiBase/torrents/pause") {
            setBody("hashes=$id")
            contentType(ContentType.Application.FormUrlEncoded)
        }.map { Unit }

    override suspend fun resume(id: String): NetworkResult<Unit> =
        httpClient.safePost<String>("$apiBase/torrents/resume") {
            setBody("hashes=$id")
            contentType(ContentType.Application.FormUrlEncoded)
        }.map { Unit }

    override suspend fun delete(id: String, deleteFiles: Boolean): NetworkResult<Unit> =
        httpClient.safePost<String>("$apiBase/torrents/delete") {
            setBody("hashes=$id&deleteFiles=$deleteFiles")
            contentType(ContentType.Application.FormUrlEncoded)
        }.map { Unit }

    override suspend fun pauseAll(): NetworkResult<Unit> =
        httpClient.safePost<String>("$apiBase/torrents/pause") {
            setBody("hashes=all")
            contentType(ContentType.Application.FormUrlEncoded)
        }.map { Unit }

    override suspend fun resumeAll(): NetworkResult<Unit> =
        httpClient.safePost<String>("$apiBase/torrents/resume") {
            setBody("hashes=all")
            contentType(ContentType.Application.FormUrlEncoded)
        }.map { Unit }
}
