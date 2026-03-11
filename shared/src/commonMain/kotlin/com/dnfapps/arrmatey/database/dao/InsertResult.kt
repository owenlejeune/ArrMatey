package com.dnfapps.arrmatey.database.dao

sealed interface InsertResult {
    data class Success(val id: Long): InsertResult
    data class Conflict(val fields: List<ConflictField>): InsertResult
    data class Error(val message: String): InsertResult
}

enum class ConflictField {
    InstanceUrl,
    InstanceLabel,
    DownloadClientUrl,
    DownloadClientLabel
}
