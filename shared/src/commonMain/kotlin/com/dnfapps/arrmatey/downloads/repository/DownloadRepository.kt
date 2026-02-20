package com.dnfapps.arrmatey.downloads.repository

import com.dnfapps.arrmatey.client.ErrorType
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.downloads.api.client.DownloadClient
import com.dnfapps.arrmatey.downloads.api.client.QBittorrentClient
import com.dnfapps.arrmatey.downloads.api.client.SABnzbdClient
import com.dnfapps.arrmatey.downloads.api.model.DownloadClientStatus
import com.dnfapps.arrmatey.downloads.state.DownloadsState
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DownloadRepository(
    private val instance: Instance,
    private val httpClient: HttpClient
) {
    private val client: DownloadClient = when (instance.type) {
        InstanceType.QBittorrent -> QBittorrentClient(instance, httpClient)
        InstanceType.Sabnzbd -> SABnzbdClient(instance, httpClient)
        else -> throw IllegalArgumentException("Unsupported download client type: ${instance.type}")
    }

    private val _queue = MutableStateFlow<DownloadsState>(DownloadsState.Loading)
    val queue: StateFlow<DownloadsState> = _queue.asStateFlow()

    private val _status = MutableStateFlow<NetworkResult<DownloadClientStatus>>(NetworkResult.Loading)
    val status: StateFlow<NetworkResult<DownloadClientStatus>> = _status.asStateFlow()

    suspend fun refresh() {
        _queue.value = DownloadsState.Loading
        _status.value = NetworkResult.Loading

        _queue.value = when (val queueResult = client.getQueue()) {
            is NetworkResult.Success -> DownloadsState.Success(queueResult.data)
            is NetworkResult.Error -> DownloadsState.Error(
                message = queueResult.message ?: "Failed to fetch queue",
                type = if (queueResult.code == null) ErrorType.Network else ErrorType.Http
            )
            is NetworkResult.Loading -> DownloadsState.Loading
        }
        _status.value = client.getStatus()
    }

    suspend fun pause(id: String) = client.pause(id)
    suspend fun resume(id: String) = client.resume(id)
    suspend fun delete(id: String, deleteFiles: Boolean) = client.delete(id, deleteFiles)
    suspend fun pauseAll() = client.pauseAll()
    suspend fun resumeAll() = client.resumeAll()
}
