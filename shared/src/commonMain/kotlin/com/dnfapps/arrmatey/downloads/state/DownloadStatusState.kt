package com.dnfapps.arrmatey.downloads.state

import com.dnfapps.arrmatey.client.ErrorType
import com.dnfapps.arrmatey.downloads.api.model.DownloadClientStatus

sealed interface DownloadStatusState {
    object Initial : DownloadStatusState
    object Loading : DownloadStatusState
    data class Success(val status: DownloadClientStatus) : DownloadStatusState
    data class Error(
        val message: String,
        val type: ErrorType = ErrorType.Http
    ) : DownloadStatusState
}
