package com.dnfapps.arrmatey.downloads.api.client

import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.downloads.api.model.DownloadClientStatus
import com.dnfapps.arrmatey.downloads.api.model.DownloadQueueItem
import com.dnfapps.arrmatey.instances.model.Instance
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.Serializable

class SABnzbdClient(
    private val instance: Instance,
    private val httpClient: HttpClient
) : DownloadClient {

    private val apiBase = "${instance.url}/api"

    @Serializable
    private data class SABQueueResponse(
        val queue: SABQueue
    )

    @Serializable
    private data class SABQueue(
        val slots: List<SABSlot>,
        val kbpersec: Double,
        val paused: Boolean
    )

    @Serializable
    private data class SABSlot(
        val nzo_id: String,
        val filename: String,
        val mb: String,
        val mbleft: String,
        val percentage: String,
        val status: String,
        val timeleft: String
    )

    override suspend fun getQueue(): NetworkResult<List<DownloadQueueItem>> = try {
        val response: SABQueueResponse = httpClient.get(apiBase) {
            parameter("mode", "queue")
            parameter("output", "json")
            parameter("apikey", instance.apiKey)
        }.body()
        
        NetworkResult.Success(response.queue.slots.map {
            val totalMb = it.mb.toDoubleOrNull() ?: 0.0
            val leftMb = it.mbleft.toDoubleOrNull() ?: 0.0
            val progress = it.percentage.toDoubleOrNull()?.div(100.0) ?: 0.0
            
            DownloadQueueItem(
                id = it.nzo_id,
                name = it.filename,
                size = (totalMb * 1024 * 1024).toLong(),
                progress = progress,
                status = it.status,
                speed = 0, // speed is global in SAB
                eta = null, // simplified for now
                seeds = null,
                peers = null,
                category = null
            )
        })
    } catch (e: Exception) {
        NetworkResult.Error(message = e.message)
    }

    override suspend fun getStatus(): NetworkResult<DownloadClientStatus> = try {
        val response: SABQueueResponse = httpClient.get(apiBase) {
            parameter("mode", "queue")
            parameter("output", "json")
            parameter("apikey", instance.apiKey)
        }.body()
        
        NetworkResult.Success(DownloadClientStatus(
            dlSpeed = (response.queue.kbpersec * 1024).toLong(),
            upSpeed = 0,
            isPaused = response.queue.paused
        ))
    } catch (e: Exception) {
        NetworkResult.Error(message = e.message)
    }

    override suspend fun pause(id: String): NetworkResult<Unit> = try {
        httpClient.get(apiBase) {
            parameter("mode", "queue")
            parameter("name", "pause")
            parameter("value", id)
            parameter("apikey", instance.apiKey)
        }
        NetworkResult.Success(Unit)
    } catch (e: Exception) {
        NetworkResult.Error(message = e.message)
    }

    override suspend fun resume(id: String): NetworkResult<Unit> = try {
        httpClient.get(apiBase) {
            parameter("mode", "queue")
            parameter("name", "resume")
            parameter("value", id)
            parameter("apikey", instance.apiKey)
        }
        NetworkResult.Success(Unit)
    } catch (e: Exception) {
        NetworkResult.Error(message = e.message)
    }

    override suspend fun delete(id: String, deleteFiles: Boolean): NetworkResult<Unit> = try {
        httpClient.get(apiBase) {
            parameter("mode", "queue")
            parameter("name", "delete")
            parameter("value", id)
            if (deleteFiles) parameter("del_files", "1")
            parameter("apikey", instance.apiKey)
        }
        NetworkResult.Success(Unit)
    } catch (e: Exception) {
        NetworkResult.Error(message = e.message)
    }

    override suspend fun pauseAll(): NetworkResult<Unit> = try {
        httpClient.get(apiBase) {
            parameter("mode", "pause")
            parameter("apikey", instance.apiKey)
        }
        NetworkResult.Success(Unit)
    } catch (e: Exception) {
        NetworkResult.Error(message = e.message)
    }

    override suspend fun resumeAll(): NetworkResult<Unit> = try {
        httpClient.get(apiBase) {
            parameter("mode", "resume")
            parameter("apikey", instance.apiKey)
        }
        NetworkResult.Success(Unit)
    } catch (e: Exception) {
        NetworkResult.Error(message = e.message)
    }
}
