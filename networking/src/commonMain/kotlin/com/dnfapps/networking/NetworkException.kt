package com.dnfapps.networking

class NetworkException(
    val code: Int? = null,
    override val message: String? = null,
    override val cause: Throwable? = null,
    val errorType: ErrorType,
) : Exception(message, cause)
