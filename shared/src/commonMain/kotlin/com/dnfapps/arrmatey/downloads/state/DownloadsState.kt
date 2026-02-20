package com.dnfapps.arrmatey.downloads.state

import com.dnfapps.arrmatey.client.ErrorType
import com.dnfapps.arrmatey.downloads.api.model.DownloadQueueItem

sealed interface DownloadsState {
    object Initial: DownloadsState
    object Loading: DownloadsState
    data class Success(val items: List<DownloadQueueItem>): DownloadsState
    data class Error(
        val message: String,
        val type: ErrorType = ErrorType.Http
    ): DownloadsState
}
