package com.dnfapps.arrmatey.downloads.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.downloads.state.DownloadsState
import com.dnfapps.arrmatey.downloads.usecase.GetDownloadsUseCase
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
    private val getDownloadsUseCase: GetDownloadsUseCase,
    private val performDownloadActionUseCase: PerformDownloadActionUseCase,
    private val observeSelectedInstanceUseCase: ObserveSelectedInstanceUseCase
) : ViewModel() {

    private val _queueState = MutableStateFlow<DownloadsState>(DownloadsState.Initial)
    val queueState: StateFlow<DownloadsState> = _queueState.asStateFlow()

    private var selectedInstanceId: Long? = null

    init {
        observeSelectedInstance()
    }

    private fun observeSelectedInstance() {
        viewModelScope.launch {
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
            getDownloadsUseCase(id).collect { _queueState.value = it }
        }
    }

    fun pauseItem(itemId: String) {
        val id = selectedInstanceId ?: return
        viewModelScope.launch {
            performDownloadActionUseCase.pause(id, itemId)
        }
    }

    fun resumeItem(itemId: String) {
        val id = selectedInstanceId ?: return
        viewModelScope.launch {
            performDownloadActionUseCase.resume(id, itemId)
        }
    }
}
