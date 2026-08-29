package com.dnfapps.networking

import io.ktor.network.sockets.SocketTimeoutException
import io.ktor.util.network.UnresolvedAddressException
import okio.IOException
import java.net.ConnectException
import java.net.UnknownHostException
import java.net.SocketTimeoutException as JavaSocketTimeoutException

actual fun Throwable.isNoConnectionError(): Boolean =
    when (this) {
        is UnknownHostException,
        is ConnectException,
        is UnresolvedAddressException,
        is IOException,
        -> true
        else -> false
    }

actual fun Throwable.isTimeoutError(): Boolean =
    when (this) {
        is SocketTimeoutException,
        is JavaSocketTimeoutException,
        -> true
        else -> false
    }
