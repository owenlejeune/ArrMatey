package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.state.IndexersState
import com.dnfapps.arrmatey.arr.usecase.GetProwlarrIndexersUseCase
import com.dnfapps.arrmatey.client.ErrorType
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.usecase.ObserveSelectedInstanceUseCase
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

    private val _indexers = MutableStateFlow<IndexersState>(IndexersState.Initial)
    val indexers: StateFlow<IndexersState> = _indexers.asStateFlow()

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
            _indexers.value = IndexersState.Loading
            _indexers.value = when (val result = getProwlarrIndexersUseCase(id)) {
                is NetworkResult.Success -> IndexersState.Success(result.data)
                is NetworkResult.Error -> IndexersState.Error(
                    message = result.message ?: "Failed to fetch indexers",
                    type = if (result.code == null) ErrorType.Network else ErrorType.Http
                )
                is NetworkResult.Loading -> IndexersState.Loading
            }
        }
    }
}
