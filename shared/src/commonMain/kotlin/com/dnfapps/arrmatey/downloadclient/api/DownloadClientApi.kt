package com.dnfapps.arrmatey.downloadclient.api

import com.dnfapps.arrmatey.downloadclient.model.DownloadItem
import com.dnfapps.arrmatey.downloadclient.model.DownloadTransferInfo
import com.dnfapps.networking.NetworkResult

interface DownloadClientApi {
    suspend fun testConnection(): NetworkResult<Unit>

    suspend fun getDownloads(): NetworkResult<List<DownloadItem>>

    suspend fun pauseDownload(ids: List<String>): NetworkResult<Unit>

    suspend fun resumeDownload(ids: List<String>): NetworkResult<Unit>

    suspend fun deleteDownload(
        ids: List<String>,
        deleteFiles: Boolean,
    ): NetworkResult<Unit>

    suspend fun getTransferInfo(): NetworkResult<DownloadTransferInfo>
}
