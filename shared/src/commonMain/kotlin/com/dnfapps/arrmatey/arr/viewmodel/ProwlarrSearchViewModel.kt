package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.ProwlarrSearchResult
import com.dnfapps.arrmatey.arr.state.ProwlarrSearchState
import com.dnfapps.arrmatey.arr.usecase.GrabProwlarrReleaseUseCase
import com.dnfapps.arrmatey.arr.usecase.PerformProwlarrSearchUseCase
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.usecase.ObserveSelectedInstanceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class ProwlarrSearchViewModel(
    private val performProwlarrSearchUseCase: PerformProwlarrSearchUseCase,
    private val grabProwlarrReleaseUseCase: GrabProwlarrReleaseUseCase,
    private val observeSelectedInstanceUseCase: ObserveSelectedInstanceUseCase
): ViewModel() {

    private val _searchResults = MutableStateFlow<ProwlarrSearchState>(ProwlarrSearchState.Initial)
    val searchResults: StateFlow<ProwlarrSearchState> = _searchResults.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _grabStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val grabStatus: StateFlow<OperationStatus> = _grabStatus.asStateFlow()

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
                }
        }
    }

    fun performSearch(query: String) {
        val id = selectedInstanceId ?: return
        _searchQuery.value = query
        viewModelScope.launch {
            performProwlarrSearchUseCase(id, query).collect { state ->
                _searchResults.value = state
            }
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = ProwlarrSearchState.Initial
    }

    fun grabRelease(result: ProwlarrSearchResult) {
        val id = selectedInstanceId ?: return
        val guid = result.guid ?: return
        viewModelScope.launch {
            grabProwlarrReleaseUseCase(id, guid, result.indexerId).collect { status ->
                _grabStatus.value = status
            }
        }
    }

    fun resetGrabStatus() {
        _grabStatus.value = OperationStatus.Idle
    }
}
