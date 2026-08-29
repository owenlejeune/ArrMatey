package com.dnfapps.networking

import platform.Foundation.NSError
import platform.Foundation.NSURLErrorDomain
import platform.Foundation.NSURLErrorNotConnectedToInternet
import platform.Foundation.NSURLErrorTimedOut

actual fun Throwable.isNoConnectionError(): Boolean {
    val nsError = this as? NSError ?: return false
    return nsError.domain == NSURLErrorDomain &&
        nsError.code == NSURLErrorNotConnectedToInternet
}

actual fun Throwable.isTimeoutError(): Boolean {
    val nsError = this as? NSError ?: return false
    return nsError.domain == NSURLErrorDomain &&
        nsError.code == NSURLErrorTimedOut
}
