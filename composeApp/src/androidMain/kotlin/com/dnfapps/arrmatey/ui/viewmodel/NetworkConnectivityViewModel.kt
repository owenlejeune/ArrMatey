package com.dnfapps.arrmatey.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.utils.NetworkConnectivityRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NetworkConnectivityViewModel :
    ViewModel(),
    KoinComponent {
    private val networkConnectivityRepository: NetworkConnectivityRepository by inject()

    val isConnected =
        networkConnectivityRepository.isConnected
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

    init {
        networkConnectivityRepository.startObserving()
    }

    override fun onCleared() {
        super.onCleared()
        networkConnectivityRepository.stopObserving()
    }
}
