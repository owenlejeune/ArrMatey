package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.ProwlarrIndexer
import com.dnfapps.arrmatey.arr.usecase.GetProwlarrIndexersUseCase
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.instances.usecase.ObserveSelectedInstanceUseCase
import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class ProwlarrIndexersViewModel(
    private val getProwlarrIndexersUseCase: GetProwlarrIndexersUseCase,
    private val observeSelectedInstanceUseCase: ObserveSelectedInstanceUseCase
): ViewModel() {

    private val _indexers = MutableStateFlow<NetworkResult<List<ProwlarrIndexer>>>(NetworkResult.Loading)
    val indexers: StateFlow<NetworkResult<List<ProwlarrIndexer>>> = _indexers.asStateFlow()

    private var selectedInstanceId: Long? = null

    init {
        observeSelectedInstance()
    }

    private fun observeSelectedInstance() {
        viewModelScope.launch {
            observeSelectedInstanceUseCase(InstanceType.Prowlarr)
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
            _indexers.value = NetworkResult.Loading
            _indexers.value = getProwlarrIndexersUseCase(id)
        }
    }
}
