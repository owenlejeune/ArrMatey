package com.dnfapps.arrmatey.discover.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.CalendarItem
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.discover.model.CLEAN_TITLE_REGEX
import com.dnfapps.arrmatey.discover.model.DiscoverLibraryIndex
import com.dnfapps.arrmatey.discover.model.SearchFilter
import com.dnfapps.arrmatey.discover.model.SearchResult
import com.dnfapps.arrmatey.discover.usecase.GlobalSearchUseCase
import com.dnfapps.arrmatey.extensions.mergeWithLibrary
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.mediaserver.repository.MediaServerManager
import com.dnfapps.arrmatey.seerr.api.model.RequestType
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class UnifiedSearchViewModel(
    private val globalSearchUseCase: GlobalSearchUseCase,
    private val preferencesStore: PreferencesStore,
    private val instanceManager: InstanceManager,
    private val mediaServerManager: MediaServerManager? = null,
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchState = MutableStateFlow<List<SearchResult>>(emptyList())
    private var searchJob: Job? = null

    private val librariesByInstance: StateFlow<Map<Long, List<ArrMedia>>> =
        instanceManager
            .observeArrLibrariesByInstanceId()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyMap(),
            )

    val searchState: StateFlow<List<SearchResult>> =
        combine(_searchState, librariesByInstance) { results, libraryMap ->
            val allLibraries = libraryMap.values.flatten()
            val libraryIndex = DiscoverLibraryIndex(allLibraries)
            results.map { result ->
                when (result) {
                    is SearchResult.ArrMediaResult -> {
                        val instanceLib = result.instanceId?.let { libraryMap[it] } ?: emptyList()
                        if (instanceLib.isNotEmpty()) {
                            val merged = listOf(result.media).mergeWithLibrary(instanceLib).first()
                            result.copy(media = merged)
                        } else {
                            result
                        }
                    }

                    is SearchResult.SeerrMediaResult -> {
                        val tmdbId = result.result.id
                        val cleanTitle = (result.result.title ?: result.result.name)?.replace(CLEAN_TITLE_REGEX, "")?.lowercase()
                        val match =
                            if (result.result.mediaType == RequestType.Movie) {
                                libraryIndex.findMovie(tmdbId = tmdbId, cleanTitle = cleanTitle)
                            } else if (result.result.mediaType == RequestType.Tv) {
                                libraryIndex.findSeries(tmdbId = tmdbId, cleanTitle = cleanTitle)
                            } else {
                                null
                            }

                        if (match != null) {
                            val instanceId =
                                (match as? ArrMovie)?.instanceId
                                    ?: (match as? CalendarItem)?.instanceId
                                    ?: libraryMap.entries.firstOrNull { (_, items) ->
                                        items.any { it.id == match.id }
                                    }?.key
                            SearchResult.ArrMediaResult(
                                media = match,
                                instanceId = instanceId,
                                originalRank = result.originalRank,
                                instanceTypeOverride = if (result.result.mediaType == RequestType.Movie) InstanceType.Radarr else InstanceType.Sonarr,
                            )
                        } else {
                            result
                        }
                    }

                    is SearchResult.SeerrPersonResult -> result
                    is SearchResult.MediaServerResult -> result
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    private val _selectedFilter = MutableStateFlow<SearchFilter?>(null)
    val selectedFilter: StateFlow<SearchFilter?> = _selectedFilter.asStateFlow()

    val availableFilters: StateFlow<List<SearchFilter>> =
        combine(
            instanceManager.observeAllInstances(),
            mediaServerManager?.activeRepositories ?: MutableStateFlow(emptyList()),
        ) { instances, mediaRepos ->
            val searchableTypes = InstanceType.arrs() + InstanceType.Seerr
            val instanceFilters = instances
                .map { it.type }
                .distinct()
                .filter { it in searchableTypes }
                .sorted()
                .map { SearchFilter.Instance(it) }

            val mediaServerFilters = mediaRepos
                .map { it.serverType }
                .distinct()
                .map { SearchFilter.MediaServer(it) }

            instanceFilters + mediaServerFilters
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    val filteredSearchState: StateFlow<List<SearchResult>> =
        combine(searchState, _selectedFilter) { results, filter ->
            if (filter == null) {
                results
            } else {
                results.filter { filter.matches(it) }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    fun selectFilter(filter: SearchFilter?) {
        _selectedFilter.value = filter
    }

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
                        _selectedFilter.value = null
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
                _selectedFilter.value = null
            }
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        _searchQuery.value = ""
        _searchState.value = emptyList()
        _selectedFilter.value = null
        _isSearching.value = false
    }
}
