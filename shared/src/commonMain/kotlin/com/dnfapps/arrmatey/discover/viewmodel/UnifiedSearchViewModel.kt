package com.dnfapps.arrmatey.discover.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.CalendarItem
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.discover.model.SearchResult
import com.dnfapps.arrmatey.discover.usecase.GlobalSearchUseCase
import com.dnfapps.arrmatey.extensions.mergeWithLibrary
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.networking.asSuccess
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class UnifiedSearchViewModel(
    private val globalSearchUseCase: GlobalSearchUseCase,
    private val preferencesStore: PreferencesStore,
    private val instanceManager: InstanceManager,
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchState = MutableStateFlow<List<SearchResult>>(emptyList())
    private var searchJob: Job? = null

    private val allLibraries: StateFlow<List<ArrMedia>> =
        instanceManager
            .observeAllArrLibraries()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    val searchState: StateFlow<List<SearchResult>> =
        combine(_searchState, allLibraries) { results, libraries ->
            results.map { result ->
                when (result) {
                    is SearchResult.ArrMediaResult -> {
                        val merged = listOf(result.media).mergeWithLibrary(libraries).first()
                        result.copy(media = merged)
                    }

                    is SearchResult.SeerrMediaResult -> {
                        val tmdbId = result.result.id
                        val cleanTitle = (result.result.title ?: result.result.name)?.replace(Regex("[^a-zA-Z0-9]"), "")?.lowercase()
                        val match = if (result.result.mediaType == RequestType.Movie) {
                            libraries.filterIsInstance<ArrMovie>()
                                .firstOrNull { (it.tmdbId != 0L && it.tmdbId == tmdbId) || (cleanTitle != null && it.cleanTitle?.equals(cleanTitle, ignoreCase = true) == true) }
                        } else if (result.result.mediaType == RequestType.Tv) {
                            libraries.filterIsInstance<ArrSeries>().firstOrNull {
                                (it.tmdbId != null && it.tmdbId != 0L && it.tmdbId == tmdbId) ||
                                    (cleanTitle != null && it.cleanTitle?.equals(cleanTitle, ignoreCase = true) == true)
                            }
                        } else null

                        if (match != null) {
                            val instanceId = (match as? ArrMovie)?.instanceId
                                ?: (match as? CalendarItem)?.instanceId
                                ?: instanceManager.getAllArrRepositories().firstOrNull { repo ->
                                    repo.library.value?.asSuccess()?.data?.any { it.id == match.id } == true
                                }?.instance?.id
                            SearchResult.ArrMediaResult(
                                media = match,
                                instanceId = instanceId,
                                originalRank = result.originalRank,
                            )
                        } else {
                            result
                        }
                    }

                    is SearchResult.SeerrPersonResult -> result
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    val searchShowBanners: StateFlow<Boolean> =
        preferencesStore.searchShowBanners
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

    init {
        observeSearchQuery()
    }

    private fun observeSearchQuery() {
        viewModelScope.launch {
            searchQuery
                .debounce(500.milliseconds)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isNotEmpty()) {
                        performSearch(query)
                    } else {
                        searchJob?.cancel()
                        _searchState.value = emptyList()
                        _isSearching.value = false
                    }
                }
        }
    }

    private fun performSearch(query: String) {
        searchJob?.cancel()
        searchJob =
            viewModelScope.launch {
                _isSearching.value = true
                _searchState.value = emptyList()
                globalSearchUseCase(query).collect { results ->
                    _searchState.value = results
                }
                _isSearching.value = false
            }
    }

    fun updateSearchQuery(query: String) {
        if (_searchQuery.value != query) {
            searchJob?.cancel()
            _isSearching.value = false
            _searchQuery.value = query
            if (query.isEmpty()) {
                _searchState.value = emptyList()
            }
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        _searchQuery.value = ""
        _searchState.value = emptyList()
        _isSearching.value = false
    }
}
