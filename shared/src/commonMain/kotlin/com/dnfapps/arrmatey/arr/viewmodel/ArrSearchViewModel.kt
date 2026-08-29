package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.SearchAudiobook
import com.dnfapps.arrmatey.arr.state.ArrLibrary
import com.dnfapps.arrmatey.arr.usecase.GetActivityTasksUseCase
import com.dnfapps.arrmatey.arr.usecase.GetLibraryUseCase
import com.dnfapps.arrmatey.arr.usecase.GetLookupResultsUseCase
import com.dnfapps.arrmatey.arr.usecase.PerformLookupUseCase
import com.dnfapps.arrmatey.compose.utils.SortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.extensions.mergeWithLibrary
import com.dnfapps.arrmatey.extensions.orderedSortedWith
import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ArrSearchViewModel(
    private val instanceType: InstanceType,
    private val instanceId: Long? = null,
    private val getLookupResultsUseCase: GetLookupResultsUseCase,
    private val getLibraryUseCase: GetLibraryUseCase,
    private val performLookupUseCase: PerformLookupUseCase,
    private val preferencesStore: PreferencesStore,
    getActivityTasksUseCase: GetActivityTasksUseCase,
) : ViewModel() {
    private val _sortBy = MutableStateFlow(SortBy.Relevance)
    val sortBy: StateFlow<SortBy> = _sortBy.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.Asc)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    val activeMediaIds: StateFlow<Set<Long>> =
        getActivityTasksUseCase()
            .map { tasks ->
                tasks
                    .filter { task ->
                        (instanceId == null || task.instanceId == instanceId) && task.type == instanceType
                    }.mapNotNull { it.mediaId }
                    .toSet()
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptySet(),
            )

    fun isItemActive(mediaId: Long): Boolean = activeMediaIds.value.contains(mediaId)

    private val _lookupUiState = MutableStateFlow<ArrLibrary>(ArrLibrary.Initial)
    val lookupUiState: StateFlow<ArrLibrary> = _lookupUiState.asStateFlow()

    val searchShowBanners: StateFlow<Boolean> =
        preferencesStore.searchShowBanners
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    init {
        observeLookupResults()
    }

    private fun observeLookupResults() {
        viewModelScope.launch {
            val libraryFlow =
                if (instanceId != null) {
                    getLibraryUseCase(instanceId)
                } else {
                    getLibraryUseCase.byType(instanceType)
                }

            combine(
                getLookupResultsUseCase(instanceType, instanceId),
                libraryFlow,
                _sortBy,
                _sortOrder,
            ) { state, library, sortBy, sortOrder ->
                when (state) {
                    is ArrLibrary.Success -> {
                        val libraryItems = (library as? ArrLibrary.Success)?.items ?: emptyList()
                        val mergedItems = state.items.mergeWithLibrary(libraryItems)

                        val comparator: Comparator<ArrMedia>? =
                            when (sortBy) {
                                SortBy.Year -> compareBy { it.year }
                                SortBy.Rating -> compareBy { it.ratingScore() }
                                else -> null
                            }
                        val sortedList =
                            comparator?.let { comparator ->
                                mergedItems.orderedSortedWith(sortOrder, comparator)
                            } ?: mergedItems

                        val finalList =
                            if (
                                instanceType == InstanceType.Listenarr && library is ArrLibrary.Success
                            ) {
                                val existingAsins =
                                    library.items
                                        .filterIsInstance<Audiobook>()
                                        .mapTo(HashSet()) { it.asin }
                                sortedList.filterNot { item ->
                                    item is SearchAudiobook && item.asin in existingAsins
                                }
                            } else {
                                sortedList
                            }

                        ArrLibrary.Success(items = finalList, preferences = state.preferences)
                    }

                    else -> state
                }
            }.collect { state ->
                _lookupUiState.value = state
            }
        }
    }

    fun performLookup(query: String) {
        viewModelScope.launch {
            performLookupUseCase(instanceType, query, instanceId)
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
            performLookupUseCase.clear(instanceType, instanceId)
        }
    }

    override fun onCleared() {
        clearLookup()
        super.onCleared()
    }
}
