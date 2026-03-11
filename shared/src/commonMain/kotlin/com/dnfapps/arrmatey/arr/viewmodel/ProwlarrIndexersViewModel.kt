package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.IndexerStatus
import com.dnfapps.arrmatey.arr.api.model.ProwlarrIndexer
import com.dnfapps.arrmatey.arr.state.IndexersSortingState
import com.dnfapps.arrmatey.arr.state.ProwlarrIndexersState
import com.dnfapps.arrmatey.arr.usecase.GetProwlarrIndexersStatusUseCase
import com.dnfapps.arrmatey.arr.usecase.GetProwlarrIndexersUseCase
import com.dnfapps.arrmatey.compose.utils.SortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder
import com.dnfapps.arrmatey.extensions.orderedSortedWith
import com.dnfapps.arrmatey.instances.repository.ProwlarrInstanceRepository
import com.dnfapps.arrmatey.instances.usecase.GetProwlarrInstanceRepositoryUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ProwlarrIndexersViewModel(
    private val getProwlarrIndexersUseCase: GetProwlarrIndexersUseCase,
    private val getProwlarrInstanceRepositoryUseCase: GetProwlarrInstanceRepositoryUseCase,
    private val getProwlarrIndexersStatusUseCase: GetProwlarrIndexersStatusUseCase
): ViewModel() {

    private val _indexers = MutableStateFlow<ProwlarrIndexersState>(ProwlarrIndexersState.Initial)
    val indexers: StateFlow<ProwlarrIndexersState> = _indexers.asStateFlow()

    private val _indexerSortState = MutableStateFlow(IndexersSortingState())
    val indexerSortState: StateFlow<IndexersSortingState> = _indexerSortState.asStateFlow()

    private val _indexerStatus = MutableStateFlow<List<IndexerStatus>>(emptyList())
    val indexerStatus: StateFlow<List<IndexerStatus>> = _indexerStatus.asStateFlow()

    private var currentRepository: ProwlarrInstanceRepository? = null

    init {
        observeSelectedInstance()
    }

    private fun observeSelectedInstance() {
        viewModelScope.launch {
            getProwlarrInstanceRepositoryUseCase
                .observeSelected()
                .filterNotNull()
                .collectLatest { repository ->
                    currentRepository = repository
                    loadData(repository)
                }
        }
    }

    private fun loadData(repository: ProwlarrInstanceRepository) {
        viewModelScope.launch {
            getProwlarrIndexersUseCase(repository.instance.id)
                .combine(_indexerSortState) { state, filter ->
                    when (state) {
                        is ProwlarrIndexersState.Success -> {
                            sortSuccessState(state, filter)
                        }
                        else -> state
                    }
                }
                .collect { state ->
                    _indexers.value = state
                }
        }
        viewModelScope.launch {
            repository.indexerStatus.collect { status ->
                _indexerStatus.value = status
            }
        }
        viewModelScope.launch {
            getProwlarrIndexersStatusUseCase(repository.instance.id)
        }
    }


    fun refresh() {
       currentRepository?.let {
           loadData(it)
       }
    }

    private fun sortSuccessState(
        state: ProwlarrIndexersState.Success,
        sortingState: IndexersSortingState
    ): ProwlarrIndexersState.Success {
        val comparator: Comparator<ProwlarrIndexer> = when (sortingState.sortBy) {
            SortBy.Name -> compareBy { it.name?.lowercase() }
            SortBy.Added -> compareBy { it.added }
            SortBy.Protocol -> compareBy { it.protocol }
            SortBy.Priority -> compareBy { it.priority }
            SortBy.Privacy -> compareBy { it.privacy }
            else -> throw IllegalStateException("Sorting indexers with unsupported sort option ${sortingState.sortBy}")
        }
        return state.copy(
            items = state.items.orderedSortedWith(sortingState.sortOrder, comparator)
        )
    }

    fun updateSortOrder(sortOrder: SortOrder) {
        _indexerSortState.update {
            it.copy(sortOrder = sortOrder)
        }
    }

    fun updateSortBy(sortBy: SortBy) {
        _indexerSortState.update {
            it.copy(sortBy = sortBy)
        }
    }
}
