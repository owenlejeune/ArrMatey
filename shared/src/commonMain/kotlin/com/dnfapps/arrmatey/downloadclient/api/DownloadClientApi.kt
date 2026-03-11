package com.dnfapps.arrmatey.downloadclient.api

import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.downloadclient.model.DownloadItem
import com.dnfapps.arrmatey.downloadclient.model.DownloadTransferInfo

interface DownloadClientApi {
    suspend fun testConnection(): NetworkResult<Unit>
    suspend fun getDownloads(): NetworkResult<List<DownloadItem>>
    suspend fun pauseDownload(id: String): NetworkResult<Unit>
    suspend fun resumeDownload(id: String): NetworkResult<Unit>
    suspend fun deleteDownload(id: String, deleteFiles: Boolean): NetworkResult<Unit>
    suspend fun getTransferInfo(): NetworkResult<DownloadTransferInfo>
}
