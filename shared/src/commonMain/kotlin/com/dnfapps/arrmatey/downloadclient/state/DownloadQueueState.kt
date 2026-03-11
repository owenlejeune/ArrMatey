package com.dnfapps.arrmatey.downloadclient.state

import com.dnfapps.arrmatey.downloadclient.model.DownloadItem
import com.dnfapps.arrmatey.downloadclient.model.DownloadTransferInfo

sealed interface DownloadQueueState {
    data object Initial : DownloadQueueState
    data object NoClient : DownloadQueueState
    data object Loading : DownloadQueueState
    data class Success(
        val items: List<DownloadItem>,
        val transferInfo: DownloadTransferInfo,
        val isRefreshing: Boolean = false
    ) : DownloadQueueState
    data class Error(
        val code: Int? = null,
        val message: String? = null,
        val cause: Throwable? = null,
        val canRetry: Boolean = true
    ) : DownloadQueueState
}
