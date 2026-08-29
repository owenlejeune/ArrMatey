package com.dnfapps.arrmatey.arr.state

import com.dnfapps.networking.ErrorType

enum class HttpErrorType {
    Http,
    Network,
    Timeout,
    Unexpected,
    ;

    companion object {
        internal fun fromErrorType(type: ErrorType) =
            when (type) {
                ErrorType.Http -> Http
                ErrorType.Network -> Network
                ErrorType.Timeout -> Timeout
                ErrorType.Unexpected -> Unexpected
            }
    }
}

internal fun ErrorType.toHttpError(): HttpErrorType = HttpErrorType.fromErrorType(this)
