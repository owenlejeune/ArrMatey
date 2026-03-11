package com.dnfapps.arrmatey.downloadclient.database

sealed interface DownloadClientInsertResult {
    data class Success(val id: Long): DownloadClientInsertResult
    data class Conflict(val fields: List<DownloadClientConflictField>): DownloadClientInsertResult
    data class Error(val message: String): DownloadClientInsertResult
}
