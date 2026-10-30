package com.dnfapps.arrmatey.mediaserver.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.compose.utils.SortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder
import com.dnfapps.arrmatey.mediaserver.model.MediaServerIntegration
import com.dnfapps.arrmatey.mediaserver.model.MediaServerType
import com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaItem
import com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaLibrary
import com.dnfapps.arrmatey.mediaserver.model.WatchedFilter
import com.dnfapps.arrmatey.mediaserver.repository.MediaServerManager
import com.dnfapps.arrmatey.mediaserver.repository.MediaServerRepository
import com.dnfapps.arrmatey.ui.theme.ViewType
import com.dnfapps.networking.NetworkResult
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MediaServerLibraryViewModel(
    private val mediaServerManager: MediaServerManager,
) : ViewModel() {

    val configuredServers: StateFlow<List<MediaServerIntegration>> =
        mediaServerManager.observeIntegrations()
            .map { list -> list.filter { it.enabled } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList(),
            )

    private val _selectedServer = MutableStateFlow<MediaServerIntegration?>(null)
    val selectedServer: StateFlow<MediaServerIntegration?> = _selectedServer.asStateFlow()

    private val _libraries = MutableStateFlow<List<UnifiedMediaLibrary>>(emptyList())
    val libraries: StateFlow<List<UnifiedMediaLibrary>> = _libraries.asStateFlow()

    private val _selectedLibrary = MutableStateFlow<UnifiedMediaLibrary?>(null)
    val selectedLibrary: StateFlow<UnifiedMediaLibrary?> = _selectedLibrary.asStateFlow()

    private val _rawItems = MutableStateFlow<List<UnifiedMediaItem>>(emptyList())
    val rawItems: StateFlow<List<UnifiedMediaItem>> = _rawItems.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _viewType = MutableStateFlow(ViewType.Grid)
    val viewType: StateFlow<ViewType> = _viewType.asStateFlow()

    private val _sortBy = MutableStateFlow(SortBy.Title)
    val sortBy: StateFlow<SortBy> = _sortBy.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.Asc)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _watchedFilter = MutableStateFlow(WatchedFilter.ALL)
    val watchedFilter: StateFlow<WatchedFilter> = _watchedFilter.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val filteredItems: StateFlow<List<UnifiedMediaItem>> =
        combine(
            _rawItems,
            _searchQuery,
            _watchedFilter,
            _sortBy,
            _sortOrder,
        ) { items, query, filter, sort, order ->
            var list = items

            // 1. Search Query Filter
            if (query.isNotBlank()) {
                list = list.filter {
                    it.title.contains(query, ignoreCase = true) ||
                        it.seriesTitle?.contains(query, ignoreCase = true) == true
                }
            }

            // 2. Watched Filter
            list = when (filter) {
                WatchedFilter.ALL -> list
                WatchedFilter.UNWATCHED -> list.filter { !it.played }
                WatchedFilter.WATCHED -> list.filter { it.played }
            }

            // 3. Sorting
            val comparator = when (sort) {
                SortBy.Title -> compareBy<UnifiedMediaItem> { it.title.lowercase() }
                SortBy.Year -> compareBy<UnifiedMediaItem> { it.year ?: 0 }
                SortBy.Added -> compareBy<UnifiedMediaItem> { it.lastPlayedDate ?: 0L }
                SortBy.Rating -> compareBy<UnifiedMediaItem> { it.rating ?: 0.0 }
                else -> compareBy<UnifiedMediaItem> { it.title.lowercase() }
            }

            if (order == SortOrder.Desc) {
                list.sortedWith(comparator.reversed())
            } else {
                list.sortedWith(comparator)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    init {
        viewModelScope.launch {
            configuredServers.collect { servers ->
                val current = _selectedServer.value
                if (current == null || servers.none { it.id == current.id }) {
                    servers.firstOrNull()?.let { selectServer(it) } ?: run {
                        _selectedServer.value = null
                        _libraries.value = emptyList()
                        _selectedLibrary.value = null
                        _rawItems.value = emptyList()
                    }
                }
            }
        }
    }

    fun selectServer(server: MediaServerIntegration) {
        _selectedServer.value = server
        _selectedLibrary.value = null
        loadLibrariesAndContents(server, null)
    }

    fun selectServerByType(type: MediaServerType) {
        configuredServers.value.firstOrNull { it.serverType == type }?.let {
            selectServer(it)
        }
    }

    fun selectLibrary(library: UnifiedMediaLibrary?) {
        _selectedLibrary.value = library
        val server = _selectedServer.value ?: return
        loadContents(server, library)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setViewType(viewType: ViewType) {
        _viewType.value = viewType
    }

    fun toggleViewType() {
        _viewType.value = if (_viewType.value == ViewType.Grid) ViewType.List else ViewType.Grid
    }

    fun setSortBy(sortBy: SortBy) {
        _sortBy.value = sortBy
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun toggleSortOrder() {
        _sortOrder.value = if (_sortOrder.value == SortOrder.Asc) SortOrder.Desc else SortOrder.Asc
    }

    fun setWatchedFilter(filter: WatchedFilter) {
        _watchedFilter.value = filter
    }

    fun refresh() {
        val server = _selectedServer.value ?: return
        loadLibrariesAndContents(server, _selectedLibrary.value)
    }

    fun toggleWatched(item: UnifiedMediaItem) {
        val repo = mediaServerManager.getRepository(item.serverType) ?: return
        viewModelScope.launch {
            val newPlayed = !item.played
            when (repo.markWatched(item.id, newPlayed)) {
                is NetworkResult.Success -> {
                    _rawItems.value = _rawItems.value.map {
                        if (it.id == item.id) it.copy(played = newPlayed) else it
                    }
                }
                is NetworkResult.Error -> {}
                is NetworkResult.Loading -> {}
            }
        }
    }

    private fun loadLibrariesAndContents(
        server: MediaServerIntegration,
        targetLibrary: UnifiedMediaLibrary?,
    ) {
        viewModelScope.launch {
            val repo = mediaServerManager.getOrCreateRepository(server)
            _isLoading.value = true
            _error.value = null

            when (val libResult = repo.getLibraries()) {
                is NetworkResult.Success -> {
                    val libs = libResult.data
                    _libraries.value = libs
                    val libraryToLoad = targetLibrary?.takeIf { libs.any { l -> l.id == it.id } }
                        ?: libs.firstOrNull()
                    _selectedLibrary.value = libraryToLoad
                    if (libraryToLoad != null) {
                        loadContentsInternal(repo, libraryToLoad)
                    } else {
                        _rawItems.value = emptyList()
                        _isLoading.value = false
                    }
                }
                is NetworkResult.Error -> {
                    _error.value = libResult.message ?: "Failed to load libraries"
                    _isLoading.value = false
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    private fun loadContents(
        server: MediaServerIntegration,
        library: UnifiedMediaLibrary?,
    ) {
        viewModelScope.launch {
            val repo = mediaServerManager.getOrCreateRepository(server)
            _isLoading.value = true
            _error.value = null

            if (library != null) {
                loadContentsInternal(repo, library)
            } else {
                // Load all libraries
                loadAllLibrariesContentsInternal(repo, _libraries.value)
            }
        }
    }

    private suspend fun loadContentsInternal(
        repo: MediaServerRepository,
        library: UnifiedMediaLibrary,
    ) {
        when (val result = repo.getLibraryContents(library.id, page = 0, pageSize = 200)) {
            is NetworkResult.Success -> {
                _rawItems.value = result.data
                _isLoading.value = false
            }
            is NetworkResult.Error -> {
                _error.value = result.message ?: "Failed to load library items"
                _isLoading.value = false
            }
            is NetworkResult.Loading -> {}
        }
    }

    private suspend fun loadAllLibrariesContentsInternal(
        repo: MediaServerRepository,
        libraries: List<UnifiedMediaLibrary>,
    ) = coroutineScope {
        try {
            val deferredList = libraries.map { lib ->
                async { repo.getLibraryContents(lib.id, page = 0, pageSize = 100) }
            }
            val results = deferredList.awaitAll()
            val allItems = mutableListOf<UnifiedMediaItem>()
            results.forEach { res ->
                if (res is NetworkResult.Success) {
                    allItems.addAll(res.data)
                }
            }
            _rawItems.value = allItems
        } catch (e: Exception) {
            _error.value = e.message ?: "Error loading media items"
        } finally {
            _isLoading.value = false
        }
    }
}
