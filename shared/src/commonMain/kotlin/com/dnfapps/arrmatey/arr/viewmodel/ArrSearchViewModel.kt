package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.SearchAudiobook
import com.dnfapps.arrmatey.arr.state.ArrLibrary
import com.dnfapps.arrmatey.arr.usecase.GetLibraryUseCase
import com.dnfapps.arrmatey.arr.usecase.GetLookupResultsUseCase
import com.dnfapps.arrmatey.arr.usecase.PerformLookupUseCase
import com.dnfapps.arrmatey.compose.utils.SortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder
import com.dnfapps.arrmatey.extensions.orderedSortedWith
import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ArrSearchViewModel(
    private val instanceType: InstanceType,
    private val getLookupResultsUseCase: GetLookupResultsUseCase,
    private val getLibraryUseCase: GetLibraryUseCase,
    private val performLookupUseCase: PerformLookupUseCase
): ViewModel() {

    private val _sortBy = MutableStateFlow(SortBy.Relevance)
    val sortBy: StateFlow<SortBy> = _sortBy.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.Asc)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _lookupUiState = MutableStateFlow<ArrLibrary>(ArrLibrary.Initial)
    val lookupUiState: StateFlow<ArrLibrary> = _lookupUiState.asStateFlow()

    init {
        observeLookupResults()
    }

    private fun observeLookupResults() {
        viewModelScope.launch {
            combine(
                getLookupResultsUseCase(instanceType),
                getLibraryUseCase.byType(instanceType),
                _sortBy,
                _sortOrder
            ) { state, library, sortBy, sortOrder ->
                when (state) {
                    is ArrLibrary.Success -> {
                        val comparator: Comparator<ArrMedia>? = when (sortBy) {
                            SortBy.Year -> compareBy { it.year }
                            SortBy.Rating -> compareBy { it.ratingScore() }
                            else -> null
                        }
                        val sortedList = comparator?.let { comparator ->
                            state.items.orderedSortedWith(sortOrder, comparator)
                        } ?: state.items

                        val finalList = if (
                            instanceType == InstanceType.Listenarr && library is ArrLibrary.Success
                        ) {
                            val existingAsins = library.items
                                .filterIsInstance<Audiobook>()
                                .mapTo(HashSet()) { it.asin }
                            sortedList.filterNot { item ->
                                item is SearchAudiobook && item.asin in existingAsins
                            }
                        } else { sortedList }

                        ArrLibrary.Success(items = finalList, preferences = state.preferences)
                    }
                    else -> state
                }
            }
            .collect { state ->
                _lookupUiState.value = state
            }
        }
    }

    fun performLookup(query: String) {
        viewModelScope.launch {
            performLookupUseCase(instanceType, query)
        }
    }

    fun setSortBy(sortBy: SortBy) {
        _sortBy.value = sortBy
    }

    fun setSortOrder(sortOrder: SortOrder) {
        _sortOrder.value = sortOrder
    }

    fun clearLookup() {
        viewModelScope.launch {
            performLookupUseCase.clear(instanceType)
        }
    }

    override fun onCleared() {
        clearLookup()
        super.onCleared()
    }
}