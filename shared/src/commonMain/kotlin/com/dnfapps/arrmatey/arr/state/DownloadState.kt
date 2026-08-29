package com.dnfapps.arrmatey.arr.state

sealed interface DownloadState {
    object Initial : DownloadState

    data class Loading(
        val guid: String,
    ) : DownloadState

    object Error : DownloadState

    object Success : DownloadState
}
