package com.dnfapps.arrmatey.downloads.api.client

import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.downloads.api.model.DownloadClientStatus
import com.dnfapps.arrmatey.downloads.api.model.DownloadQueueItem
import com.dnfapps.arrmatey.instances.model.Instance
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

class QBittorrentClient(
    private val instance: Instance,
    private val httpClient: HttpClient
) : DownloadClient {

    private val apiBase = "${instance.url}/api/v2"

    @Serializable
    private data class QBitTorrentInfo(
        val hash: String,
        val name: String,
        val size: Long,
        val progress: Double,
        val state: String,
        val dlspeed: Long,
        val eta: Long,
        val num_seeds: Int,
        val num_leechs: Int,
        val category: String
    )

    @Serializable
    private data class QBitMainData(
        val server_state: QBitServerState
    )

    @Serializable
    private data class QBitServerState(
        val dl_info_speed: Long,
        val up_info_speed: Long,
        val pause_requests: Boolean
    )

    override suspend fun getQueue(): NetworkResult<List<DownloadQueueItem>> = try {
        val response: List<QBitTorrentInfo> = httpClient.get("$apiBase/torrents/info").body()
        NetworkResult.Success(response.map {
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
        })
    } catch (e: Exception) {
        NetworkResult.Error(message = e.message)
    }

    override suspend fun getStatus(): NetworkResult<DownloadClientStatus> = try {
        val response: QBitMainData = httpClient.get("$apiBase/sync/maindata").body()
        NetworkResult.Success(DownloadClientStatus(
            dlSpeed = response.server_state.dl_info_speed,
            upSpeed = response.server_state.up_info_speed,
            isPaused = response.server_state.pause_requests
        ))
    } catch (e: Exception) {
        NetworkResult.Error(message = e.message)
    }

    override suspend fun pause(id: String): NetworkResult<Unit> = try {
        httpClient.post("$apiBase/torrents/pause") {
            setBody("hashes=$id")
            contentType(ContentType.Application.FormUrlEncoded)
        }
        NetworkResult.Success(Unit)
    } catch (e: Exception) {
        NetworkResult.Error(message = e.message)
    }

    override suspend fun resume(id: String): NetworkResult<Unit> = try {
        httpClient.post("$apiBase/torrents/resume") {
            setBody("hashes=$id")
            contentType(ContentType.Application.FormUrlEncoded)
        }
        NetworkResult.Success(Unit)
    } catch (e: Exception) {
        NetworkResult.Error(message = e.message)
    }

    override suspend fun delete(id: String, deleteFiles: Boolean): NetworkResult<Unit> = try {
        httpClient.post("$apiBase/torrents/delete") {
            setBody("hashes=$id&deleteFiles=$deleteFiles")
            contentType(ContentType.Application.FormUrlEncoded)
        }
        NetworkResult.Success(Unit)
    } catch (e: Exception) {
        NetworkResult.Error(message = e.message)
    }

    override suspend fun pauseAll(): NetworkResult<Unit> = try {
        httpClient.post("$apiBase/torrents/pause") {
            setBody("hashes=all")
            contentType(ContentType.Application.FormUrlEncoded)
        }
        NetworkResult.Success(Unit)
    } catch (e: Exception) {
        NetworkResult.Error(message = e.message)
    }

    override suspend fun resumeAll(): NetworkResult<Unit> = try {
        httpClient.post("$apiBase/torrents/resume") {
            setBody("hashes=all")
            contentType(ContentType.Application.FormUrlEncoded)
        }
        NetworkResult.Success(Unit)
    } catch (e: Exception) {
        NetworkResult.Error(message = e.message)
    }
}
