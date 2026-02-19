package com.dnfapps.arrmatey.downloads.api.client

import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.downloads.api.model.DownloadClientStatus
import com.dnfapps.arrmatey.downloads.api.model.DownloadQueueItem

interface DownloadClient {
    suspend fun getQueue(): NetworkResult<List<DownloadQueueItem>>
    suspend fun getStatus(): NetworkResult<DownloadClientStatus>
    suspend fun pause(id: String): NetworkResult<Unit>
    suspend fun resume(id: String): NetworkResult<Unit>
    suspend fun delete(id: String, deleteFiles: Boolean): NetworkResult<Unit>
    suspend fun pauseAll(): NetworkResult<Unit>
    suspend fun resumeAll(): NetworkResult<Unit>
}
