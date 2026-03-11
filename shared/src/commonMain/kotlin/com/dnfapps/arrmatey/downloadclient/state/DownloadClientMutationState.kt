package com.dnfapps.arrmatey.downloadclient.state

import com.dnfapps.arrmatey.downloadclient.database.DownloadClientConflictField

sealed interface DownloadClientMutationState {
    data object Initial : DownloadClientMutationState
    data object Testing : DownloadClientMutationState
    data class Success(val id: Long) : DownloadClientMutationState
    data class Conflict(val fields: List<DownloadClientConflictField>) : DownloadClientMutationState
    data class Error(val message: String) : DownloadClientMutationState
    data class ConnectionFailed(val message: String) : DownloadClientMutationState
}
