package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.ProwlarrSearchResult
import com.dnfapps.arrmatey.arr.usecase.PerformProwlarrSearchUseCase
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.instances.usecase.ObserveSelectedInstanceUseCase
import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class ProwlarrSearchViewModel(
    private val performProwlarrSearchUseCase: PerformProwlarrSearchUseCase,
    private val observeSelectedInstanceUseCase: ObserveSelectedInstanceUseCase
): ViewModel() {

    private val _searchResults = MutableStateFlow<NetworkResult<List<ProwlarrSearchResult>>?>(null)
    val searchResults: StateFlow<NetworkResult<List<ProwlarrSearchResult>>?> = _searchResults.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

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
        if (query.isBlank()) {
            _searchResults.value = null
            return
        }
        
        _searchQuery.value = query
        viewModelScope.launch {
            _searchResults.value = NetworkResult.Loading
            _searchResults.value = performProwlarrSearchUseCase(id, query)
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = null
    }
}
