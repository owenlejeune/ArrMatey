package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.state.ArrLibrary
import com.dnfapps.arrmatey.arr.usecase.DeleteMediaUseCase
import com.dnfapps.arrmatey.arr.usecase.ExecuteArrCommandUseCase
import com.dnfapps.arrmatey.arr.usecase.GetActivityTasksUseCase
import com.dnfapps.arrmatey.arr.usecase.GetLibraryUseCase
import com.dnfapps.arrmatey.arr.usecase.PerformAutomaticSearchUseCase
import com.dnfapps.arrmatey.arr.usecase.PerformRefreshUseCase
import com.dnfapps.arrmatey.arr.usecase.ToggleMonitorUseCase
import com.dnfapps.arrmatey.arr.usecase.UpdateMediaUseCase
import com.dnfapps.arrmatey.compose.utils.FilterBy
import com.dnfapps.arrmatey.compose.utils.SortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder
import com.dnfapps.arrmatey.datastore.InstancePreferenceStoreRepository
import com.dnfapps.arrmatey.datastore.InstancePreferences
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceData
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.instances.usecase.GetBazarrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.instances.usecase.ObserveAllInstancesUseCase
import com.dnfapps.arrmatey.instances.usecase.UpdateAllPreferencesUseCase
import com.dnfapps.arrmatey.instances.usecase.UpdateInstancePreferencesUseCase
import com.dnfapps.arrmatey.mediaserver.model.MediaServerIntegration
import com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaItem
import com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaLibrary
import com.dnfapps.arrmatey.mediaserver.model.WatchedFilter
import com.dnfapps.arrmatey.mediaserver.repository.MediaServerManager
import com.dnfapps.arrmatey.mediaserver.repository.MediaServerRepository
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.arrmatey.ui.theme.ViewType
import com.dnfapps.arrmatey.utils.Blur
import com.dnfapps.arrmatey.utils.GridDensity
import com.dnfapps.arrmatey.utils.GridSpacing
import com.dnfapps.arrmatey.utils.MultiSelectState
import com.dnfapps.arrmatey.utils.PosterElevation
import com.dnfapps.arrmatey.utils.PosterRadius
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UnifiedLibraryTabTarget {
    val tabId: String
    val label: String

    data class Arr(val instance: Instance) : UnifiedLibraryTabTarget {
        override val tabId: String = "arr_${instance.id}"
        override val label: String = instance.label
    }

    data class MediaServer(val integration: MediaServerIntegration) : UnifiedLibraryTabTarget {
        override val tabId: String = "ms_${integration.id}"
        override val label: String = integration.label
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class UnifiedLibraryViewModel(
    private val observeAllInstancesUseCase: ObserveAllInstancesUseCase,
    private val instanceManager: InstanceManager,
    private val getLibraryUseCase: GetLibraryUseCase,
    private val instancePreferenceStoreRepository: InstancePreferenceStoreRepository,
    private val updateInstancePreferencesUseCase: UpdateInstancePreferencesUseCase,
    private val updateAllPreferencesUseCase: UpdateAllPreferencesUseCase,
    private val toggleMonitorUseCase: ToggleMonitorUseCase,
    private val performAutomaticSearchUseCase: PerformAutomaticSearchUseCase,
    private val performRefreshUseCase: PerformRefreshUseCase,
    private val updateMediaUseCase: UpdateMediaUseCase,
    private val deleteMediaUseCase: DeleteMediaUseCase,
    private val getBazarrInstanceRepositoryUseCase: GetBazarrInstanceRepositoryUseCase,
    private val executeArrCommandUseCase: ExecuteArrCommandUseCase,
    private val preferencesStore: PreferencesStore,
    getActivityTasksUseCase: GetActivityTasksUseCase,
    private val mediaServerManager: MediaServerManager? = null,
) : ViewModel() {
    val searchAllInstances: StateFlow<Boolean> =
        preferencesStore.unifiedLibrarySearchAllInstances
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = true,
            )

    val activeMediaIdsByInstance: StateFlow<Map<Long, Set<Long>>> =
        getActivityTasksUseCase()
            .map { tasks ->
                tasks
                    .groupBy { it.instanceId }
                    .mapNotNull { (instanceId, instanceTasks) ->
                        instanceId?.let { id ->
                            id to instanceTasks.mapNotNull { it.mediaId }.toSet()
                        }
                    }.toMap()
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyMap(),
            )

    fun isItemActive(
        instanceId: Long,
        mediaId: Long,
    ): Boolean = activeMediaIdsByInstance.value[instanceId]?.contains(mediaId) == true

    private val arrOrder = InstanceType.arrs()

    val arrInstances: StateFlow<List<Instance>> =
        observeAllInstancesUseCase()
            .map { all ->
                all
                    .filter { it.type in arrOrder }
                    .sortedWith(compareBy<Instance> { it.type }.thenBy { it.label })
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList(),
            )

    val configuredMediaServers: StateFlow<List<MediaServerIntegration>> =
        mediaServerManager?.observeIntegrations()
            ?.map { list -> list.filter { it.enabled } }
            ?.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList(),
            ) ?: MutableStateFlow(emptyList())

    val allTabs: StateFlow<List<UnifiedLibraryTabTarget>> =
        combine(arrInstances, configuredMediaServers) { arrs, servers ->
            val list = mutableListOf<UnifiedLibraryTabTarget>()
            arrs.forEach { list.add(UnifiedLibraryTabTarget.Arr(it)) }
            servers.forEach { list.add(UnifiedLibraryTabTarget.MediaServer(it)) }
            list
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    private val _selectedTab = MutableStateFlow<UnifiedLibraryTabTarget?>(null)
    val selectedTab: StateFlow<UnifiedLibraryTabTarget?> = _selectedTab.asStateFlow()

    private val _selectedMediaServer = MutableStateFlow<MediaServerIntegration?>(null)
    val selectedMediaServer: StateFlow<MediaServerIntegration?> = _selectedMediaServer.asStateFlow()

    private val _mediaServerLibraries = MutableStateFlow<List<UnifiedMediaLibrary>>(emptyList())
    val mediaServerLibraries: StateFlow<List<UnifiedMediaLibrary>> = _mediaServerLibraries.asStateFlow()

    private val _selectedMediaServerLibrary = MutableStateFlow<UnifiedMediaLibrary?>(null)
    val selectedMediaServerLibrary: StateFlow<UnifiedMediaLibrary?> = _selectedMediaServerLibrary.asStateFlow()

    private val _mediaServerRawItems = MutableStateFlow<List<UnifiedMediaItem>>(emptyList())
    val mediaServerRawItems: StateFlow<List<UnifiedMediaItem>> = _mediaServerRawItems.asStateFlow()

    private val _mediaServerIsLoading = MutableStateFlow(false)
    val mediaServerIsLoading: StateFlow<Boolean> = _mediaServerIsLoading.asStateFlow()

    private val _mediaServerWatchedFilter = MutableStateFlow(WatchedFilter.ALL)
    val mediaServerWatchedFilter: StateFlow<WatchedFilter> = _mediaServerWatchedFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val preferences: StateFlow<InstancePreferences> =
        _selectedTab
            .filterNotNull()
            .flatMapLatest { tab ->
                when (tab) {
                    is UnifiedLibraryTabTarget.Arr -> {
                        instancePreferenceStoreRepository.getInstancePreferences(tab.instance.id, SortBy.defaultFor(tab.instance.type)).observePreferences()
                    }
                    is UnifiedLibraryTabTarget.MediaServer -> {
                        instancePreferenceStoreRepository.getMediaServerPreferences(tab.integration.id, SortBy.Title).observePreferences()
                    }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = InstancePreferences(),
            )

    val mediaServerViewType: StateFlow<ViewType> =
        preferences.map { it.viewType }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ViewType.Grid,
        )

    val mediaServerSortBy: StateFlow<SortBy> =
        preferences.map { it.sortBy }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SortBy.Title,
        )

    val mediaServerSortOrder: StateFlow<SortOrder> =
        preferences.map { it.sortOrder }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SortOrder.Asc,
        )

    val mediaServerFilteredItems: StateFlow<List<UnifiedMediaItem>> =
        combine(
            _mediaServerRawItems,
            _searchQuery,
            _mediaServerWatchedFilter,
            preferences,
        ) { items, query, filter, prefs ->
            var list = items
            if (query.isNotBlank()) {
                list = list.filter {
                    it.title.contains(query, ignoreCase = true) ||
                        it.seriesTitle?.contains(query, ignoreCase = true) == true
                }
            }
            list = when (filter) {
                WatchedFilter.ALL -> list
                WatchedFilter.UNWATCHED -> list.filter { !it.played }
                WatchedFilter.WATCHED -> list.filter { it.played }
            }
            val sort = prefs.sortBy
            val order = prefs.sortOrder
            val comparator = when (sort) {
                SortBy.Title -> compareBy<UnifiedMediaItem> { it.title.lowercase() }
                SortBy.Year -> compareBy<UnifiedMediaItem> { it.year ?: 0 }
                SortBy.Added -> compareBy<UnifiedMediaItem> { it.lastPlayedDate ?: 0L }
                SortBy.Rating -> compareBy<UnifiedMediaItem> { it.rating ?: 0.0 }
                else -> compareBy<UnifiedMediaItem> { it.title.lowercase() }
            }
            if (order == SortOrder.Desc) list.sortedWith(comparator.reversed()) else list.sortedWith(comparator)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    private val _selectedInstance = MutableStateFlow<Instance?>(null)
    val selectedInstance: StateFlow<Instance?> = _selectedInstance.asStateFlow()

    val libraries: StateFlow<Map<Long, ArrLibrary>> =
        arrInstances
            .flatMapLatest { instances ->
                if (instances.isEmpty()) {
                    flowOf(emptyMap())
                } else {
                    val libraryFlows =
                        instances.map { instance ->
                            getLibraryUseCase(instance.id, _searchQuery).map { libraryState ->
                                instance.id to libraryState
                            }
                        }
                    combine(libraryFlows) { results ->
                        results.toMap()
                    }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyMap(),
            )

    val offlineInstanceIds: StateFlow<Set<Long>> =
        libraries
            .map { map ->
                map.filterValues { it is ArrLibrary.Error }.keys
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = emptySet(),
            )

    val selectionState = MultiSelectState<Long>()

    private val _deleteStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val deleteStatus: StateFlow<OperationStatus> = _deleteStatus.asStateFlow()

    private val _editItemStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val editItemStatus: StateFlow<OperationStatus> = _editItemStatus.asStateFlow()

    private val _lastSearchResult = MutableStateFlow<Boolean?>(null)
    val lastSearchResult: StateFlow<Boolean?> = _lastSearchResult.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val hasBazarr: StateFlow<Boolean> =
        getBazarrInstanceRepositoryUseCase
            .observeSelected()
            .map { it != null }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = false,
            )

    val selectedRepository: StateFlow<ArrInstanceRepository?> =
        _selectedInstance
            .map { instance ->
                instance?.let { instanceManager.getArrRepository(it.id) }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = null,
            )

    val instanceData: StateFlow<InstanceData?> =
        selectedRepository
            .flatMapLatest { repo ->
                if (repo == null) {
                    flowOf(null)
                } else {
                    combine(
                        repo.qualityProfiles,
                        repo.metadataProfiles,
                        repo.rootFolders,
                        repo.tags,
                        repo.customFilters,
                    ) { qp, mp, rf, tags, filters ->
                        InstanceData(
                            qualityProfiles = qp,
                            metadataProfiles = mp,
                            rootFolders = rf,
                            tags = tags,
                            customFilters = filters,
                        )
                    }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = null,
            )

    val currentLibraryState: StateFlow<ArrLibrary> =
        combine(
            _selectedInstance,
            libraries,
        ) { instance, libMap ->
            if (instance == null) {
                ArrLibrary.Initial
            } else {
                libMap[instance.id] ?: ArrLibrary.Initial
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ArrLibrary.Initial,
        )

    val selectedItem: StateFlow<ArrMedia?> =
        combine(
            selectionState.selectedItems,
            currentLibraryState,
        ) { selectedIds, state ->
            if (selectedIds.size == 1) {
                val id = selectedIds.first()
                (state as? ArrLibrary.Success)?.items?.find { it.id == id }
            } else {
                null
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    private var userHasSelectedTab = false

    init {
        viewModelScope.launch {
            allTabs.collect { tabs ->
                val current = _selectedTab.value
                val firstArr = tabs.firstOrNull { it is UnifiedLibraryTabTarget.Arr }
                if (current == null || tabs.none { it.tabId == current.tabId }) {
                    val defaultTab = firstArr ?: tabs.firstOrNull()
                    defaultTab?.let { selectTab(it, userInitiated = false) } ?: run {
                        _selectedTab.value = null
                        _selectedInstance.value = null
                        _selectedMediaServer.value = null
                    }
                } else if (!userHasSelectedTab && current is UnifiedLibraryTabTarget.MediaServer && firstArr != null) {
                    selectTab(firstArr, userInitiated = false)
                } else {
                    val updated = tabs.firstOrNull { it.tabId == current.tabId }
                    if (updated != null && updated != current) {
                        selectTab(updated, userInitiated = false)
                    }
                }
            }
        }

        viewModelScope.launch {
            _selectedInstance.filterNotNull().collect { instance ->
                _searchQuery.value = ""
                selectionState.exitSelectionMode()
                val repo = instanceManager.getArrRepository(instance.id)
                repo?.refreshAllMetadata()
            }
        }
    }

    fun selectTab(tab: UnifiedLibraryTabTarget, userInitiated: Boolean = false) {
        if (userInitiated) {
            userHasSelectedTab = true
        }
        _selectedTab.value = tab
        when (tab) {
            is UnifiedLibraryTabTarget.Arr -> {
                _selectedInstance.value = tab.instance
                _selectedMediaServer.value = null
                viewModelScope.launch {
                    updateAllPreferencesUseCase.syncGlobalPreferencesToInstance(tab.instance.id)
                }
            }
            is UnifiedLibraryTabTarget.MediaServer -> {
                _selectedInstance.value = null
                _selectedMediaServer.value = tab.integration
                viewModelScope.launch {
                    updateAllPreferencesUseCase.syncGlobalPreferencesToMediaServer(tab.integration.id)
                }
                loadMediaServerLibrariesAndContents(tab.integration, null)
            }
        }
    }

    fun selectInstance(instance: Instance) {
        selectTab(UnifiedLibraryTabTarget.Arr(instance), userInitiated = true)
    }

    fun selectMediaServer(server: MediaServerIntegration) {
        selectTab(UnifiedLibraryTabTarget.MediaServer(server), userInitiated = true)
    }

    fun selectMediaServerLibrary(library: UnifiedMediaLibrary?) {
        _selectedMediaServerLibrary.value = library
        val server = _selectedMediaServer.value ?: return
        loadMediaServerContents(server, library)
    }

    fun setMediaServerViewType(viewType: ViewType) {
        updateViewType(viewType)
    }

    fun toggleMediaServerViewType() {
        val next = if (preferences.value.viewType == ViewType.Grid) ViewType.List else ViewType.Grid
        updateViewType(next)
    }

    fun setMediaServerSortBy(sortBy: SortBy) {
        updateSortBy(sortBy)
    }

    fun setMediaServerSortOrder(order: SortOrder) {
        updateSortOrder(order)
    }

    fun toggleMediaServerSortOrder() {
        val next = if (preferences.value.sortOrder == SortOrder.Asc) SortOrder.Desc else SortOrder.Asc
        updateSortOrder(next)
    }

    fun setMediaServerWatchedFilter(filter: WatchedFilter) {
        _mediaServerWatchedFilter.value = filter
    }

    fun refreshMediaServer() {
        val server = _selectedMediaServer.value ?: return
        loadMediaServerLibrariesAndContents(server, _selectedMediaServerLibrary.value)
    }

    fun toggleMediaServerWatched(item: UnifiedMediaItem) {
        val manager = mediaServerManager ?: return
        val repo = manager.getRepository(item.serverType) ?: return
        viewModelScope.launch {
            val newPlayed = !item.played
            if (repo.markWatched(item.id, newPlayed) is NetworkResult.Success) {
                _mediaServerRawItems.value = _mediaServerRawItems.value.map {
                    if (it.id == item.id) it.copy(played = newPlayed) else it
                }
            }
        }
    }

    private fun loadMediaServerLibrariesAndContents(
        server: MediaServerIntegration,
        targetLibrary: UnifiedMediaLibrary?,
    ) {
        val manager = mediaServerManager ?: return
        viewModelScope.launch {
            val repo = manager.getOrCreateRepository(server)
            _mediaServerIsLoading.value = true
            when (val libResult = repo.getLibraries()) {
                is NetworkResult.Success -> {
                    val libs = libResult.data
                    _mediaServerLibraries.value = libs
                    val libraryToLoad = targetLibrary?.takeIf { libs.any { l -> l.id == it.id } }
                    _selectedMediaServerLibrary.value = libraryToLoad
                    if (libraryToLoad != null) {
                        loadMediaServerContentsInternal(repo, libraryToLoad)
                    } else {
                        loadAllMediaServerContentsInternal(repo, libs)
                    }
                }
                is NetworkResult.Error -> {
                    _errorMessage.value = libResult.message ?: "Failed to load libraries"
                    _mediaServerIsLoading.value = false
                }
                else -> {
                    _mediaServerIsLoading.value = false
                }
            }
        }
    }

    private fun loadMediaServerContents(
        server: MediaServerIntegration,
        library: UnifiedMediaLibrary?,
    ) {
        val manager = mediaServerManager ?: return
        viewModelScope.launch {
            val repo = manager.getOrCreateRepository(server)
            _mediaServerIsLoading.value = true
            if (library != null) {
                loadMediaServerContentsInternal(repo, library)
            } else {
                loadAllMediaServerContentsInternal(repo, _mediaServerLibraries.value)
            }
        }
    }

    private suspend fun loadMediaServerContentsInternal(
        repo: MediaServerRepository,
        library: UnifiedMediaLibrary,
    ) {
        when (val result = repo.getAllLibraryContents(library.id)) {
            is NetworkResult.Success -> {
                _mediaServerRawItems.value = result.data
                _mediaServerIsLoading.value = false
            }
            is NetworkResult.Error -> {
                _errorMessage.value = result.message ?: "Failed to load library items"
                _mediaServerIsLoading.value = false
            }
            else -> {
                _mediaServerIsLoading.value = false
            }
        }
    }

    private suspend fun loadAllMediaServerContentsInternal(
        repo: MediaServerRepository,
        libraries: List<UnifiedMediaLibrary>,
    ) = coroutineScope {
        try {
            val deferredList = libraries.map { lib ->
                async { repo.getAllLibraryContents(lib.id) }
            }
            val results = deferredList.awaitAll()
            val all = mutableListOf<UnifiedMediaItem>()
            results.forEach { res ->
                if (res is NetworkResult.Success) all.addAll(res.data)
            }
            _mediaServerRawItems.value = all
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "Error loading media items"
        } finally {
            _mediaServerIsLoading.value = false
        }
    }

    fun refreshSelected() {
        val instance = _selectedInstance.value ?: return
        refreshInstance(instance.id)
    }

    fun refreshInstance(instanceId: Long) {
        val repo = instanceManager.getArrRepository(instanceId) ?: return
        viewModelScope.launch {
            repo.refreshLibrary()
            repo.refreshAllMetadata()
            repo.refreshStatus()
        }
    }

    fun resetErrorMessage() {
        _errorMessage.value = null
    }

    fun resetDeleteStatus() {
        _deleteStatus.value = OperationStatus.Idle
    }

    fun resetEditItemStatus() {
        _editItemStatus.value = OperationStatus.Idle
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateFilterBy(filterBy: FilterBy) {
        viewModelScope.launch {
            val instance = _selectedInstance.value ?: return@launch
            val updatedPreferences = preferences.value.copy(filterBy = filterBy, customFilterId = null)
            updateInstancePreferencesUseCase(instance.id, updatedPreferences)
        }
    }

    fun updateCustomFilter(customFilterId: Long?) {
        viewModelScope.launch {
            val instance = _selectedInstance.value ?: return@launch
            val updatedPreferences =
                preferences.value.copy(
                    customFilterId = customFilterId,
                    filterBy = if (customFilterId != null) FilterBy.All else preferences.value.filterBy,
                )
            updateInstancePreferencesUseCase(instance.id, updatedPreferences)
        }
    }

    fun updateSortBy(sortBy: SortBy) {
        viewModelScope.launch {
            val instance = _selectedInstance.value ?: return@launch
            val updatedPreferences = preferences.value.copy(sortBy = sortBy)
            updateInstancePreferencesUseCase(instance.id, updatedPreferences)
        }
    }

    fun updateSortOrder(sortOrder: SortOrder) {
        viewModelScope.launch {
            val instance = _selectedInstance.value ?: return@launch
            val updatedPreferences = preferences.value.copy(sortOrder = sortOrder)
            updateInstancePreferencesUseCase(instance.id, updatedPreferences)
        }
    }

    fun updateViewType(viewType: ViewType) {
        safeSavePreference { it.copy(viewType = viewType) }
    }

    fun updateShowFullDetails(show: Boolean) {
        safeSavePreference { it.copy(showFullDetails = show) }
    }

    fun updateShowOverlay(show: Boolean) {
        safeSavePreference { it.copy(showOverlay = show) }
    }

    fun updateShowBannerBackground(show: Boolean) {
        safeSavePreference { it.copy(showBannerBackground = show) }
    }

    fun updateIncludeOverview(show: Boolean) {
        safeSavePreference { it.copy(includeOverview = show) }
    }

    fun updateBannerBlur(blur: Blur) {
        safeSavePreference { it.copy(bannerBlur = blur) }
    }

    fun updateGridDensity(density: GridDensity) {
        safeSavePreference { it.copy(gridDensity = density) }
    }

    fun updateGridSpacing(spacing: GridSpacing) {
        safeSavePreference { it.copy(gridSpacing = spacing) }
    }

    fun updatePosterElevation(elevation: PosterElevation) {
        safeSavePreference { it.copy(posterElevation = elevation) }
    }

    fun updatePosterRadius(radius: PosterRadius) {
        safeSavePreference { it.copy(posterRadius = radius) }
    }

    fun updateDeleteDeleteFiles(deleteFiles: Boolean) {
        safeSavePreference { it.copy(deleteDeleteFiles = deleteFiles) }
    }

    fun updateDeleteAddExclusion(addExclusion: Boolean) {
        safeSavePreference { it.copy(deleteAddExclusion = addExclusion) }
    }

    fun updateApplyGlobally(applyGlobally: Boolean) {
        safeSavePreference { it.copy(applyGlobally = applyGlobally) }
    }

    private fun safeSavePreference(transform: (InstancePreferences) -> InstancePreferences) {
        viewModelScope.launch {
            val currentTab = _selectedTab.value ?: return@launch
            val prefs = preferences.value
            val updated = transform(prefs)

            if (updated.applyGlobally) {
                updateAllPreferencesUseCase(updated)
            } else {
                when (currentTab) {
                    is UnifiedLibraryTabTarget.Arr -> {
                        updateInstancePreferencesUseCase(currentTab.instance.id, updated)
                    }
                    is UnifiedLibraryTabTarget.MediaServer -> {
                        val store = instancePreferenceStoreRepository.getMediaServerPreferences(currentTab.integration.id)
                        store.savePreferences(updated)
                    }
                }
            }
        }
    }

    fun deleteMedia(
        item: ArrMedia,
        deleteFiles: Boolean,
        addImportExclusion: Boolean,
    ) {
        val mediaId = item.id ?: return
        viewModelScope.launch {
            val instance = _selectedInstance.value ?: return@launch
            val repository = instanceManager.getArrRepository(instance.id) ?: return@launch

            updateDeleteDeleteFiles(deleteFiles)
            updateDeleteAddExclusion(addImportExclusion)

            deleteMediaUseCase(mediaId, deleteFiles, addImportExclusion, repository)
                .collect { status ->
                    _deleteStatus.value = status
                }
        }
    }

    fun editItem(
        item: ArrMedia,
        moveFiles: Boolean = false,
    ) {
        viewModelScope.launch {
            val instance = _selectedInstance.value ?: return@launch
            val repository = instanceManager.getArrRepository(instance.id) ?: return@launch
            updateMediaUseCase.edit(item, moveFiles, repository)
        }
    }

    fun toggleItemSelection(id: Long) {
        selectionState.toggle(id)
    }

    fun enterSelectionMode() {
        selectionState.enterSelectionMode()
    }

    fun selectAllItems() {
        val success = currentLibraryState.value as? ArrLibrary.Success ?: return
        selectionState.selectAll(success.items.mapNotNull { it.id })
    }

    fun areAllItemsSelected(): Boolean {
        val success = currentLibraryState.value as? ArrLibrary.Success ?: return false
        return selectionState.areAllSelected(success.items.mapNotNull { it.id })
    }

    fun clearSelection() {
        selectionState.clearSelection()
    }

    fun exitSelectionMode() {
        selectionState.exitSelectionMode()
    }

    fun toggleMonitored(item: ArrMedia) {
        viewModelScope.launch {
            val instance = _selectedInstance.value ?: return@launch
            val repository = instanceManager.getArrRepository(instance.id) ?: return@launch
            toggleMonitorUseCase.toggleMedia(item, repository)
        }
    }

    fun performRefresh(item: ArrMedia) {
        val mediaId = item.id ?: return
        viewModelScope.launch {
            val instance = _selectedInstance.value ?: return@launch
            val repository = instanceManager.getArrRepository(instance.id) ?: return@launch
            performRefreshUseCase(mediaId, instance.type, repository)
        }
    }

    fun performAutomaticLookup(item: ArrMedia) {
        val mediaId = item.id ?: return
        viewModelScope.launch {
            val instance = _selectedInstance.value ?: return@launch
            val repository = instanceManager.getArrRepository(instance.id) ?: return@launch
            performAutomaticSearchUseCase(mediaId, instance.type, repository)
                .onSuccess { _lastSearchResult.value = true }
                .onError { _, _, _ -> _lastSearchResult.value = false }
            _lastSearchResult.value = null
        }
    }

    fun performSubtitleSearch(item: ArrMedia) {
        val mediaId = item.id ?: return
        viewModelScope.launch {
            val instance = _selectedInstance.value ?: return@launch
            val bazarrRepo = instanceManager.getAllBazarrRepositories().firstOrNull() ?: return@launch
            when (instance.type) {
                InstanceType.Sonarr -> bazarrRepo.autoSearchSeriesSubtitles(mediaId)
                InstanceType.Radarr -> bazarrRepo.autoSearchMovieSubtitles(mediaId)
                else -> {}
            }
        }
    }

    fun refreshSelectedItems() {
        viewModelScope.launch {
            val instance = _selectedInstance.value ?: return@launch
            val repository = instanceManager.getArrRepository(instance.id) ?: return@launch
            val selectedIds = selectionState.selectedItems.value.toList()
            if (selectedIds.isNotEmpty()) {
                performRefreshUseCase.bulkRefresh(selectedIds, instance.type, repository)
            }
            selectionState.exitSelectionMode()
        }
    }

    fun deleteSelected(
        deleteFiles: Boolean,
        addExclusion: Boolean,
    ) {
        viewModelScope.launch {
            val instance = _selectedInstance.value ?: return@launch
            val repository = instanceManager.getArrRepository(instance.id) ?: return@launch

            updateDeleteDeleteFiles(deleteFiles)
            updateDeleteAddExclusion(addExclusion)

            val selectedIds = selectionState.selectedItems.value
            coroutineScope {
                selectedIds.forEach { id ->
                    launch {
                        repository.delete(id, deleteFiles, addExclusion)
                    }
                }
            }

            selectionState.exitSelectionMode()
            repository.refreshLibrary()
        }
    }

    fun toggleMonitoringForSelected() {
        viewModelScope.launch {
            val instance = _selectedInstance.value ?: return@launch
            val repository = instanceManager.getArrRepository(instance.id) ?: return@launch
            val selectedIds = selectionState.selectedItems.value.toList()
            val currentItems = (currentLibraryState.value as? ArrLibrary.Success)?.items ?: emptyList()

            selectedIds.forEach { id ->
                val item = currentItems.find { it.id == id } ?: return@forEach
                toggleMonitorUseCase.toggleMedia(item, repository)
            }

            selectionState.exitSelectionMode()
        }
    }

    fun performAutomaticLookupSelected() {
        viewModelScope.launch {
            val instance = _selectedInstance.value ?: return@launch
            val repository = instanceManager.getArrRepository(instance.id) ?: return@launch
            val selectedIds = selectionState.selectedItems.value

            selectedIds.forEach { id ->
                performAutomaticSearchUseCase(id, instance.type, repository)
            }

            _lastSearchResult.value = true
            selectionState.exitSelectionMode()
        }
    }

    fun performSubtitleSearchSelected() {
        viewModelScope.launch {
            val instance = _selectedInstance.value ?: return@launch
            val selectedIds = selectionState.selectedItems.value
            val bazarrRepo = instanceManager.getAllBazarrRepositories().firstOrNull() ?: return@launch

            selectedIds.forEach { id ->
                when (instance.type) {
                    InstanceType.Sonarr -> bazarrRepo.autoSearchSeriesSubtitles(id)
                    InstanceType.Radarr -> bazarrRepo.autoSearchMovieSubtitles(id)
                    else -> {}
                }
            }

            selectionState.exitSelectionMode()
        }
    }

    fun runRssSync() {
        val instanceId = _selectedInstance.value?.id ?: return
        viewModelScope.launch {
            val result = executeArrCommandUseCase.runRssSync(instanceId)
            _lastSearchResult.value = result is NetworkResult.Success
        }
    }

    fun searchAllMissing() {
        val instanceId = _selectedInstance.value?.id ?: return
        viewModelScope.launch {
            val result = executeArrCommandUseCase.searchAllMissing(instanceId)
            _lastSearchResult.value = result is NetworkResult.Success
        }
    }

    fun searchFiltered() {
        val success = currentLibraryState.value as? ArrLibrary.Success ?: return
        val instance = _selectedInstance.value ?: return
        val repository = instanceManager.getArrRepository(instance.id) ?: return
        val filteredIds = success.items.mapNotNull { it.id }
        if (filteredIds.isEmpty()) return
        viewModelScope.launch {
            filteredIds.forEach { id ->
                performAutomaticSearchUseCase(id, instance.type, repository)
            }
            _lastSearchResult.value = true
        }
    }

    fun updateLibrary() {
        val instanceId = _selectedInstance.value?.id ?: return
        viewModelScope.launch {
            val result = executeArrCommandUseCase.updateLibrary(instanceId)
            _lastSearchResult.value = result is NetworkResult.Success
        }
    }

    fun backupDatabase() {
        val instanceId = _selectedInstance.value?.id ?: return
        viewModelScope.launch {
            val result = executeArrCommandUseCase.backupDatabase(instanceId)
            _lastSearchResult.value = result is NetworkResult.Success
        }
    }
}
