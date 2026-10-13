package com.dnfapps.arrmatey.arr.state

import com.dnfapps.networking.ErrorType

enum class HttpErrorType {
    Http,
    Network,
    Timeout,
    Unexpected,
    ;

    companion object {
        fun fromErrorType(type: ErrorType) = when (type) {
            ErrorType.Http -> Http
            ErrorType.Network -> Network
            ErrorType.Timeout -> Timeout
            ErrorType.Unexpected -> Unexpected
        }
    }
}

fun ErrorType.toHttpError(): HttpErrorType = HttpErrorType.fromErrorType(this)
