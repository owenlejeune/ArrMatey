@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.dnfapps.arrmatey.utils

import kotlinx.cinterop.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Network.*
import platform.darwin.*

actual class NetworkConnectivityObserverFactory {
    actual fun create(): NetworkConnectivityObserver = IosNetworkConnectivityObserver()
}

@OptIn(ExperimentalForeignApi::class)
class IosNetworkConnectivityObserver : NetworkConnectivityObserver {
    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private var monitor: Any? = null

    override fun startObserving() {
        monitor = nw_path_monitor_create()

        nw_path_monitor_set_update_handler(monitor as NSObject) { pathObj: Any? ->
            val status = nw_path_get_status(pathObj as NSObject)
            _isConnected.value =
                when (status) {
                    nw_path_status_satisfied, nw_path_status_satisfiable -> true
                    else -> false
                }
        }

        nw_path_monitor_set_queue(
            monitor as NSObject,
            dispatch_get_global_queue(0, 0.toULong()), // Default priority, no flags
        )
        nw_path_monitor_start(monitor as NSObject)
    }

    override fun stopObserving() {
        monitor?.let { nw_path_monitor_cancel(it as NSObject) }
        monitor = null
    }
}
