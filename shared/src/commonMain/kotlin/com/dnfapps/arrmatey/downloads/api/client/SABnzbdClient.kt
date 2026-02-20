package com.dnfapps.arrmatey.downloads.api.client

import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.client.safeGet
import com.dnfapps.arrmatey.downloads.api.model.DownloadClientStatus
import com.dnfapps.arrmatey.downloads.api.model.DownloadQueueItem
import com.dnfapps.arrmatey.instances.model.Instance
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter

class SABnzbdClient(
    private val instance: Instance,
    private val httpClient: HttpClient
) : DownloadClient {

    private val apiBase = "${instance.url}/api"

    override suspend fun getQueue(): NetworkResult<List<DownloadQueueItem>> =
        httpClient.safeGet<SABQueueResponse>(apiBase) {
            parameter("mode", "queue")
            parameter("output", "json")
            parameter("apikey", instance.apiKey)
        }.map { response ->
            response.queue.slots.map {
                val totalMb = it.mb.toDoubleOrNull() ?: 0.0
                val progress = it.percentage.toDoubleOrNull()?.div(100.0) ?: 0.0

                DownloadQueueItem(
                    id = it.nzo_id,
                    name = it.filename,
                    size = (totalMb * 1024 * 1024).toLong(),
                    progress = progress,
                    status = it.status,
                    speed = 0,
                    eta = null,
                    seeds = null,
                    peers = null,
                    category = null
                )
            }
        }

    override suspend fun getStatus(): NetworkResult<DownloadClientStatus> =
        httpClient.safeGet<SABQueueResponse>(apiBase) {
            parameter("mode", "queue")
            parameter("output", "json")
            parameter("apikey", instance.apiKey)
        }.map { response ->
            DownloadClientStatus(
                dlSpeed = (response.queue.kbpersec * 1024).toLong(),
                upSpeed = 0,
                isPaused = response.queue.paused
            )
        }

    override suspend fun pause(id: String): NetworkResult<Unit> =
        httpClient.safeGet<String>(apiBase) {
            parameter("mode", "queue")
            parameter("name", "pause")
            parameter("value", id)
            parameter("apikey", instance.apiKey)
        }.map { Unit }

    override suspend fun resume(id: String): NetworkResult<Unit> =
        httpClient.safeGet<String>(apiBase) {
            parameter("mode", "queue")
            parameter("name", "resume")
            parameter("value", id)
            parameter("apikey", instance.apiKey)
        }.map { Unit }

    override suspend fun delete(id: String, deleteFiles: Boolean): NetworkResult<Unit> =
        httpClient.safeGet<String>(apiBase) {
            parameter("mode", "queue")
            parameter("name", "delete")
            parameter("value", id)
            if (deleteFiles) parameter("del_files", "1")
            parameter("apikey", instance.apiKey)
        }.map { Unit }

    override suspend fun pauseAll(): NetworkResult<Unit> =
        httpClient.safeGet<String>(apiBase) {
            parameter("mode", "pause")
            parameter("apikey", instance.apiKey)
        }.map { Unit }

    override suspend fun resumeAll(): NetworkResult<Unit> =
        httpClient.safeGet<String>(apiBase) {
            parameter("mode", "resume")
            parameter("apikey", instance.apiKey)
        }.map { Unit }
}
