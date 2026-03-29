package com.dnfapps.arrmatey.client

sealed interface OperationStatus {
    object Idle: OperationStatus
    object InProgress: OperationStatus
    data class Success(
        val message: String? = null,
        val result: Any? = null
    ): OperationStatus
    data class Error(
        val code: Int? = null,
        val message: String? = null,
        val cause: Throwable? = null
    ): OperationStatus
}