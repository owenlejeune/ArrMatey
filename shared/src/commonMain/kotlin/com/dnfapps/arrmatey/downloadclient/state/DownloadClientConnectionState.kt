package com.dnfapps.arrmatey.downloadclient.state

sealed interface DownloadClientConnectionState {
    object Initial: DownloadClientConnectionState
    object Loading: DownloadClientConnectionState
    object Success: DownloadClientConnectionState
    data class Error(
        val code: Int? = null,
        val message: String? = null,
        val cause: Throwable? = null
    ): DownloadClientConnectionState
}
