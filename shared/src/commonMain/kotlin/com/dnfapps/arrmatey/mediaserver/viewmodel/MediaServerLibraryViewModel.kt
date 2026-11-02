package com.dnfapps.arrmatey.mediaserver.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.compose.utils.SortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder
import com.dnfapps.arrmatey.datastore.InstancePreferenceStoreRepository
import com.dnfapps.arrmatey.datastore.InstancePreferences
import com.dnfapps.arrmatey.instances.usecase.UpdateAllPreferencesUseCase
import com.dnfapps.arrmatey.mediaserver.model.MediaServerIntegration
import com.dnfapps.arrmatey.mediaserver.model.MediaServerType
import com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaItem
import com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaLibrary
import com.dnfapps.arrmatey.mediaserver.model.WatchedFilter
import com.dnfapps.arrmatey.mediaserver.repository.MediaServerManager
import com.dnfapps.arrmatey.mediaserver.repository.MediaServerRepository
import com.dnfapps.arrmatey.ui.theme.ViewType
import com.dnfapps.arrmatey.utils.Blur
import com.dnfapps.arrmatey.utils.GridDensity
import com.dnfapps.arrmatey.utils.GridSpacing
import com.dnfapps.arrmatey.utils.PosterElevation
import com.dnfapps.arrmatey.utils.PosterRadius
import com.dnfapps.networking.NetworkResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MediaServerLibraryViewModel(
    private val mediaServerManager: MediaServerManager,
    private val instancePreferenceStoreRepository: InstancePreferenceStoreRepository? = null,
    private val updateAllPreferencesUseCase: UpdateAllPreferencesUseCase? = null,
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

    private val fallbackPreferences = MutableStateFlow(InstancePreferences())

    val preferences: StateFlow<InstancePreferences> =
        _selectedServer
            .flatMapLatest { server ->
                if (server != null && instancePreferenceStoreRepository != null) {
                    instancePreferenceStoreRepository.getMediaServerPreferences(server.id, SortBy.Title).observePreferences()
                } else {
                    fallbackPreferences
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = InstancePreferences(),
            )

    private val _libraries = MutableStateFlow<List<UnifiedMediaLibrary>>(emptyList())
    val libraries: StateFlow<List<UnifiedMediaLibrary>> = _libraries.asStateFlow()

    private val _selectedLibrary = MutableStateFlow<UnifiedMediaLibrary?>(null)
    val selectedLibrary: StateFlow<UnifiedMediaLibrary?> = _selectedLibrary.asStateFlow()

    private val _rawItems = MutableStateFlow<List<UnifiedMediaItem>>(emptyList())
    val rawItems: StateFlow<List<UnifiedMediaItem>> = _rawItems.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val viewType: StateFlow<ViewType> =
        preferences.map { it.viewType }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ViewType.Grid,
        )

    val sortBy: StateFlow<SortBy> =
        preferences.map { it.sortBy }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SortBy.Title,
        )

    val sortOrder: StateFlow<SortOrder> =
        preferences.map { it.sortOrder }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SortOrder.Asc,
        )

    private val _watchedFilter = MutableStateFlow(WatchedFilter.ALL)
    val watchedFilter: StateFlow<WatchedFilter> = _watchedFilter.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val filteredItems: StateFlow<List<UnifiedMediaItem>> =
        combine(
            _rawItems,
            _searchQuery,
            _watchedFilter,
            preferences,
        ) { items, query, filter, prefs ->
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
            val sort = prefs.sortBy
            val order = prefs.sortOrder
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
        viewModelScope.launch {
            updateAllPreferencesUseCase?.syncGlobalPreferencesToMediaServer(server.id)
        }
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

    fun updateViewType(viewType: ViewType) {
        safeSavePreference { it.copy(viewType = viewType) }
    }

    fun setViewType(viewType: ViewType) = updateViewType(viewType)

    fun toggleViewType() {
        val next = if (preferences.value.viewType == ViewType.Grid) ViewType.List else ViewType.Grid
        updateViewType(next)
    }

    fun updateShowFullDetails(showFullDetails: Boolean) {
        safeSavePreference { it.copy(showFullDetails = showFullDetails) }
    }

    fun updateShowOverlay(showOverlay: Boolean) {
        safeSavePreference { it.copy(showOverlay = showOverlay) }
    }

    fun updateShowBannerBackground(showBannerBackground: Boolean) {
        safeSavePreference { it.copy(showBannerBackground = showBannerBackground) }
    }

    fun updateIncludeOverview(includeOverview: Boolean) {
        safeSavePreference { it.copy(includeOverview = includeOverview) }
    }

    fun updateBannerBlur(bannerBlur: Blur) {
        safeSavePreference { it.copy(bannerBlur = bannerBlur) }
    }

    fun updateGridDensity(gridDensity: GridDensity) {
        safeSavePreference { it.copy(gridDensity = gridDensity) }
    }

    fun updateGridSpacing(gridSpacing: GridSpacing) {
        safeSavePreference { it.copy(gridSpacing = gridSpacing) }
    }

    fun updatePosterElevation(posterElevation: PosterElevation) {
        safeSavePreference { it.copy(posterElevation = posterElevation) }
    }

    fun updatePosterRadius(posterRadius: PosterRadius) {
        safeSavePreference { it.copy(posterRadius = posterRadius) }
    }

    fun updateApplyGlobally(applyGlobally: Boolean) {
        safeSavePreference { it.copy(applyGlobally = applyGlobally) }
    }

    fun updateSortBy(sortBy: SortBy) {
        safeSavePreference { it.copy(sortBy = sortBy) }
    }

    fun setSortBy(sortBy: SortBy) = updateSortBy(sortBy)

    fun updateSortOrder(order: SortOrder) {
        safeSavePreference { it.copy(sortOrder = order) }
    }

    fun setSortOrder(order: SortOrder) = updateSortOrder(order)

    fun toggleSortOrder() {
        val next = if (preferences.value.sortOrder == SortOrder.Asc) SortOrder.Desc else SortOrder.Asc
        updateSortOrder(next)
    }

    private fun safeSavePreference(transform: (InstancePreferences) -> InstancePreferences) {
        viewModelScope.launch {
            val server = _selectedServer.value
            val currentPrefs = preferences.value
            val updated = transform(currentPrefs)
            if (updated.applyGlobally && updateAllPreferencesUseCase != null) {
                updateAllPreferencesUseCase(updated)
            } else if (server != null && instancePreferenceStoreRepository != null) {
                instancePreferenceStoreRepository.getMediaServerPreferences(server.id).savePreferences(updated)
            } else {
                fallbackPreferences.value = updated
            }
        }
    }

    fun setWatchedFilter(filter: WatchedFilter) {
        _watchedFilter.value = filter
    }

    fun refresh() {
        val server = _selectedServer.value ?: return
        _isRefreshing.value = true
        loadLibrariesAndContents(server, _selectedLibrary.value, isRefresh = true)
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
        isRefresh: Boolean = false,
    ) {
        viewModelScope.launch {
            val repo = mediaServerManager.getOrCreateRepository(server)
            if (!isRefresh) {
                _isLoading.value = true
            }
            _error.value = null

            try {
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
                        }
                    }
                    is NetworkResult.Error -> {
                        _error.value = libResult.message ?: "Failed to load libraries"
                    }
                    is NetworkResult.Loading -> {}
                }
            } finally {
                _isLoading.value = false
                _isRefreshing.value = false
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

            try {
                if (library != null) {
                    loadContentsInternal(repo, library)
                } else {
                    // Load all libraries
                    loadAllLibrariesContentsInternal(repo, _libraries.value)
                }
            } finally {
                _isLoading.value = false
                _isRefreshing.value = false
            }
        }
    }

    private suspend fun loadContentsInternal(
        repo: MediaServerRepository,
        library: UnifiedMediaLibrary,
    ) {
        when (val result = repo.getAllLibraryContents(library.id)) {
            is NetworkResult.Success -> {
                _rawItems.value = result.data
            }
            is NetworkResult.Error -> {
                _error.value = result.message ?: "Failed to load library items"
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
                async { repo.getAllLibraryContents(lib.id) }
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
        }
    }
}
