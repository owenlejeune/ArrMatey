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
import com.dnfapps.networking.NetworkResult
import com.dnfapps.arrmatey.compose.utils.FilterBy
import com.dnfapps.arrmatey.compose.utils.SortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder
import com.dnfapps.arrmatey.datastore.InstancePreferenceStoreRepository
import com.dnfapps.arrmatey.datastore.InstancePreferences
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceData
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.instances.usecase.GetBazarrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.instances.usecase.ObserveAllInstancesUseCase
import com.dnfapps.arrmatey.instances.usecase.UpdateAllPreferencesUseCase
import com.dnfapps.arrmatey.instances.usecase.UpdateInstancePreferencesUseCase
import com.dnfapps.arrmatey.ui.theme.ViewType
import com.dnfapps.arrmatey.utils.Blur
import com.dnfapps.arrmatey.utils.GridDensity
import com.dnfapps.arrmatey.utils.GridSpacing
import com.dnfapps.arrmatey.utils.MultiSelectState
import com.dnfapps.arrmatey.utils.PosterElevation
import com.dnfapps.arrmatey.utils.PosterRadius
import com.dnfapps.networking.OperationStatus
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    getActivityTasksUseCase: GetActivityTasksUseCase
) : ViewModel() {

    val activeMediaIdsByInstance: StateFlow<Map<Long, Set<Long>>> = getActivityTasksUseCase()
        .map { tasks ->
            tasks.groupBy { it.instanceId }
                .mapNotNull { (instanceId, instanceTasks) ->
                    instanceId?.let { id ->
                        id to instanceTasks.mapNotNull { it.mediaId }.toSet()
                    }
                }.toMap()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    fun isItemActive(instanceId: Long, mediaId: Long): Boolean =
        activeMediaIdsByInstance.value[instanceId]?.contains(mediaId) == true

    private val arrOrder = InstanceType.arrs()

    val arrInstances: StateFlow<List<Instance>> = observeAllInstancesUseCase()
        .map { all ->
            all.filter { it.type in arrOrder }
                .sortedWith(compareBy<Instance> { arrOrder.indexOf(it.type) }.thenBy { it.label })
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    private val _selectedInstance = MutableStateFlow<Instance?>(null)
    val selectedInstance: StateFlow<Instance?> = _selectedInstance.asStateFlow()

    val libraries: StateFlow<Map<Long, ArrLibrary>> = arrInstances
        .flatMapLatest { instances ->
            if (instances.isEmpty()) {
                flowOf(emptyMap())
            } else {
                val libraryFlows = instances.map { instance ->
                    getLibraryUseCase(instance.id).map { libraryState ->
                        instance.id to libraryState
                    }
                }
                combine(libraryFlows) { results ->
                    results.toMap()
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyMap()
        )

    val offlineInstanceIds: StateFlow<Set<Long>> = libraries
        .map { map ->
            map.filterValues { it is ArrLibrary.Error }.keys
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptySet()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val selectionState = MultiSelectState<Long>()

    private val _deleteStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val deleteStatus: StateFlow<OperationStatus> = _deleteStatus.asStateFlow()

    private val _editItemStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val editItemStatus: StateFlow<OperationStatus> = _editItemStatus.asStateFlow()

    private val _lastSearchResult = MutableStateFlow<Boolean?>(null)
    val lastSearchResult: StateFlow<Boolean?> = _lastSearchResult.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val hasBazarr: StateFlow<Boolean> = getBazarrInstanceRepositoryUseCase
        .observeSelected()
        .map { it != null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    val selectedRepository: StateFlow<ArrInstanceRepository?> = _selectedInstance
        .map { instance ->
            instance?.let { instanceManager.getArrRepository(it.id) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    val preferences: StateFlow<InstancePreferences> = _selectedInstance
        .filterNotNull()
        .flatMapLatest { instance ->
            instancePreferenceStoreRepository.getInstancePreferences(instance.id).observePreferences()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = InstancePreferences()
        )

    val instanceData: StateFlow<InstanceData?> = selectedRepository
        .flatMapLatest { repo ->
            if (repo == null) flowOf(null)
            else combine(
                repo.qualityProfiles,
                repo.rootFolders,
                repo.tags,
                repo.customFilters
            ) { qp, rf, tags, filters ->
                InstanceData(
                    qualityProfiles = qp,
                    rootFolders = rf,
                    tags = tags,
                    customFilters = filters
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    val currentLibraryState: StateFlow<ArrLibrary> = combine(
        _selectedInstance,
        libraries,
        _searchQuery
    ) { instance, libMap, query ->
        if (instance == null) {
            ArrLibrary.Initial
        } else {
            val state = libMap[instance.id] ?: ArrLibrary.Initial
            if (state is ArrLibrary.Success && query.isNotEmpty()) {
                state.copy(items = state.items.filter { it.title?.contains(query, ignoreCase = true) == true })
            } else {
                state
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ArrLibrary.Initial
    )

    val selectedItem: StateFlow<ArrMedia?> = combine(
        selectionState.selectedItems,
        currentLibraryState
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
        initialValue = null
    )

    init {
        viewModelScope.launch {
            arrInstances.collect { instances ->
                val current = _selectedInstance.value
                if (current == null || instances.none { it.id == current.id }) {
                    _selectedInstance.value = instances.firstOrNull()
                } else {
                    _selectedInstance.value = instances.firstOrNull { it.id == current.id }
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

    fun selectInstance(instance: Instance) {
        _selectedInstance.value = instance
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
            val updatedPreferences = preferences.value.copy(
                customFilterId = customFilterId,
                filterBy = if (customFilterId != null) FilterBy.All else preferences.value.filterBy
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
            val instance = _selectedInstance.value ?: return@launch
            val prefs = preferences.value
            val updated = transform(prefs)

            if (updated.applyGlobally) {
                updateAllPreferencesUseCase(updated)
            } else {
                updateInstancePreferencesUseCase(instance.id, updated)
            }
        }
    }

    fun deleteMedia(item: ArrMedia, deleteFiles: Boolean, addImportExclusion: Boolean) {
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

    fun editItem(item: ArrMedia, moveFiles: Boolean = false) {
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

    fun deleteSelected(deleteFiles: Boolean, addExclusion: Boolean) {
        viewModelScope.launch {
            val instance = _selectedInstance.value ?: return@launch
            val repository = instanceManager.getArrRepository(instance.id) ?: return@launch

            updateDeleteDeleteFiles(deleteFiles)
            updateDeleteAddExclusion(addExclusion)

            val selectedIds = selectionState.selectedItems.value
            selectedIds.forEach { id ->
                repository.delete(id, deleteFiles, addExclusion)
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
