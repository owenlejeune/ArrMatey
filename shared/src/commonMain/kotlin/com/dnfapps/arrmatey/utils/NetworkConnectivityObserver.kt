package com.dnfapps.arrmatey.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface NetworkConnectivityObserver {
    val isConnected: StateFlow<Boolean>

    fun startObserving()

    fun stopObserving()
}

expect class NetworkConnectivityObserverFactory() {
    fun create(): NetworkConnectivityObserver
}

class NetworkConnectivityRepository : KoinComponent {
    private val networkObserver: NetworkConnectivityObserver by inject()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        startObserving()
    }

    fun startObserving() {
        scope.launch {
            networkObserver.isConnected
                .collect { connected ->
                    _isConnected.value = connected
                }
        }
    }

    fun stopObserving() {
        scope.cancel()
        networkObserver.stopObserving()
    }
}
