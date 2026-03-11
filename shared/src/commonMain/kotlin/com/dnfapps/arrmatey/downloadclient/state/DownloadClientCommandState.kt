package com.dnfapps.arrmatey.downloadclient.state

sealed interface DownloadClientCommandState {
    object Initial: DownloadClientCommandState
    object Loading: DownloadClientCommandState
    object Success: DownloadClientCommandState
    data class Error(
        val code: Int? = null,
        val message: String? = null,
        val cause: Throwable? = null
    ): DownloadClientCommandState
}
