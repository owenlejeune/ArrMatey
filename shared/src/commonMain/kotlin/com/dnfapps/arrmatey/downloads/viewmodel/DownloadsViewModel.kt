package com.dnfapps.arrmatey.downloads.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.downloads.api.model.DownloadQueueItem
import com.dnfapps.arrmatey.downloads.usecase.GetDownloadQueueUseCase
import com.dnfapps.arrmatey.downloads.usecase.PerformDownloadActionUseCase
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.usecase.ObserveSelectedInstanceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class DownloadsViewModel(
    private val getDownloadQueueUseCase: GetDownloadQueueUseCase,
    private val performDownloadActionUseCase: PerformDownloadActionUseCase,
    private val observeSelectedInstanceUseCase: ObserveSelectedInstanceUseCase
) : ViewModel() {

    private val _queueState = MutableStateFlow<NetworkResult<List<DownloadQueueItem>>>(NetworkResult.Loading)
    val queueState: StateFlow<NetworkResult<List<DownloadQueueItem>>> = _queueState.asStateFlow()

    private var selectedInstanceId: Long? = null

    init {
        observeSelectedInstance()
    }

    private fun observeSelectedInstance() {
        viewModelScope.launch {
            // We need to decide which type to observe or handle both
            // For now let's just pick QBittorrent as default for testing
            observeSelectedInstanceUseCase(InstanceType.QBittorrent)
                .filterNotNull()
                .collectLatest { instance ->
                    selectedInstanceId = instance.id
                    refresh()
                }
        }
    }

    fun refresh() {
        val id = selectedInstanceId ?: return
        viewModelScope.launch {
            getDownloadQueueUseCase(id).collect {
                _queueState.value = it
            }
        }
    }

    fun pauseItem(itemId: String) {
        val id = selectedInstanceId ?: return
        viewModelScope.launch {
            performDownloadActionUseCase.pause(id, itemId)
            refresh()
        }
    }

    fun resumeItem(itemId: String) {
        val id = selectedInstanceId ?: return
        viewModelScope.launch {
            performDownloadActionUseCase.resume(id, itemId)
            refresh()
        }
    }
}
