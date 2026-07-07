package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.state.ArrLibrary
import com.dnfapps.arrmatey.arr.usecase.DeleteMediaUseCase
import com.dnfapps.arrmatey.arr.usecase.GetLibraryUseCase
import com.dnfapps.arrmatey.arr.usecase.PerformAutomaticSearchUseCase
import com.dnfapps.arrmatey.arr.usecase.PerformRefreshUseCase
import com.dnfapps.arrmatey.arr.usecase.ToggleMonitorUseCase
import com.dnfapps.arrmatey.arr.usecase.UpdateMediaUseCase
import com.dnfapps.arrmatey.client.ErrorType
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.client.onError
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.compose.utils.FilterBy
import com.dnfapps.arrmatey.compose.utils.SortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder
import com.dnfapps.arrmatey.datastore.InstancePreferenceStoreRepository
import com.dnfapps.arrmatey.datastore.InstancePreferences
import com.dnfapps.arrmatey.instances.model.InstanceData
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.instances.usecase.GetArrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.instances.usecase.UpdateAllPreferencesUseCase
import com.dnfapps.arrmatey.instances.usecase.UpdateInstancePreferencesUseCase
import com.dnfapps.arrmatey.ui.theme.ViewType
import com.dnfapps.arrmatey.utils.Blur
import com.dnfapps.arrmatey.utils.GridDensity
import com.dnfapps.arrmatey.utils.GridSpacing
import com.dnfapps.arrmatey.utils.MultiSelectState
import com.dnfapps.arrmatey.utils.PosterElevation
import com.dnfapps.arrmatey.utils.PosterRadius
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ArrMediaViewModel(
    private val instanceType: InstanceType,
    private val getArrInstanceRepositoryUseCase: GetArrInstanceRepositoryUseCase,
    private val getLibraryUseCase: GetLibraryUseCase,
    private val updatePreferencesUseCase: UpdateInstancePreferencesUseCase,
    private val updateAllPreferencesUseCase: UpdateAllPreferencesUseCase,
    private val instancePreferenceStoreRepository: InstancePreferenceStoreRepository,
    private val toggleMonitorUseCase: ToggleMonitorUseCase,
    private val performAutomaticSearchUseCase: PerformAutomaticSearchUseCase,
    private val updateMediaUseCase: UpdateMediaUseCase,
    private val deleteMediaUseCase: DeleteMediaUseCase,
    private val performRefreshUseCase: PerformRefreshUseCase
): ViewModel() {

    private val _addItemStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val addItemStatus: StateFlow<OperationStatus> = _addItemStatus.asStateFlow()

    private val _monitorStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val monitorStatus: StateFlow<OperationStatus> = _monitorStatus.asStateFlow()

    private val _editItemStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val editItemStatus: StateFlow<OperationStatus> = _editItemStatus.asStateFlow()

    private val _deleteStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val deleteStatus: StateFlow<OperationStatus> = _deleteStatus.asStateFlow()

    private val _lastSearchResult = MutableStateFlow<Boolean?>(null)
    val lastSearchResult: StateFlow<Boolean?> = _lastSearchResult.asStateFlow()

    private val _hasServerConnectivityError = MutableStateFlow(false)
    val hasServerConnectivityError: StateFlow<Boolean> = _hasServerConnectivityError.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val selectionState = MultiSelectState<ArrMedia>()

    private var currentRepository: ArrInstanceRepository? = null

    private val selectedRepository = getArrInstanceRepositoryUseCase
        .observeSelected(instanceType)
        .filterNotNull()
        .distinctUntilChanged { old, new ->
            // Only emit if the instance ID actually changed
            old.instance.id == new.instance.id
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val preferences: StateFlow<InstancePreferences> = selectedRepository
        .filterNotNull()
        .flatMapLatest {
            instancePreferenceStoreRepository.getInstancePreferences(it.instance.id).observePreferences()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = InstancePreferences()
        )

    val uiState: StateFlow<ArrLibrary> = selectedRepository
        .filterNotNull()
        .flatMapLatest { repository ->
            currentRepository = repository
            _searchQuery.value = ""

            selectionState.exitSelectionMode()

            viewModelScope.launch {
                repository.refreshAllMetadata()
            }

            getLibraryUseCase(repository.instance.id)
                .combine(_searchQuery) { state, query ->
                    when (state) {
                        is ArrLibrary.Success -> {
                            filterSuccessState(state, query)
                        }

                        is ArrLibrary.Error -> {
                            handleErrorState(state)
                            state
                        }

                        else -> state
                    }
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ArrLibrary.Initial
        )

    val instanceData: StateFlow<InstanceData?> = selectedRepository
        .filterNotNull()
        .distinctUntilChanged { old, new ->
            old.instance.id == new.instance.id
        }
        .flatMapLatest { repository ->
            combine(
                repository.qualityProfiles,
                repository.rootFolders,
                repository.tags,
                repository.customFilters
            ) { profiles, folders, tags, filters ->
                InstanceData(
                    qualityProfiles = profiles,
                    rootFolders = folders,
                    tags = tags,
                    customFilters = filters
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        viewModelScope.launch {
            selectedRepository.filterNotNull().collect { repository ->
                currentRepository = repository
            }
        }
    }

    private fun filterSuccessState(state: ArrLibrary.Success, query: String) =
        state.copy(
            items = state.items.filter {
                it.title?.contains(query, ignoreCase = true) == true
            }
        )

    private fun handleErrorState(state: ArrLibrary.Error) {
        _errorMessage.value = state.message
        _hasServerConnectivityError.value = (state.type == ErrorType.Network)
    }

    fun resetErrorMessage() {
        _errorMessage.value = null
    }

    fun executeAutomaticSearch(seriesId: Long) {
        viewModelScope.launch {
            currentRepository?.executeAutomaticSearch(seriesId)
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

    fun updateSortBy(sortBy: SortBy) {
        safeSavePreference { it.copy(sortBy = sortBy) }
    }

    fun updateSortOrder(sortOrder: SortOrder) {
        safeSavePreference { it.copy(sortOrder = sortOrder) }
    }

    fun updateFilterBy(filterBy: FilterBy) {
        viewModelScope.launch {
            val repository = currentRepository ?: return@launch
            val updatedPreferences = preferences.value.copy(filterBy = filterBy, customFilterId = null)

            if (updatedPreferences.applyGlobally) {
                updateAllPreferencesUseCase(updatedPreferences)
            } else {
                updatePreferencesUseCase(repository.instance.id, updatedPreferences)
            }
        }
    }

    fun updateCustomFilter(customFilterId: Int?) {
        viewModelScope.launch {
            val repository = currentRepository ?: return@launch
            val updatedPreferences = preferences.value.copy(
                customFilterId = customFilterId,
                filterBy = if (customFilterId != null) FilterBy.All else preferences.value.filterBy
            )

            if (updatedPreferences.applyGlobally) {
                updateAllPreferencesUseCase(updatedPreferences)
            } else {
                updatePreferencesUseCase(repository.instance.id, updatedPreferences)
            }
        }
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

    fun updateApplyGlobally(applyGlobally: Boolean) {
        safeSavePreference { it.copy(applyGlobally = applyGlobally) }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private fun safeSavePreference(transform: (InstancePreferences) -> InstancePreferences) {
        viewModelScope.launch {
            val repository = currentRepository ?: return@launch
            val preferences = preferences.value

            val updatedPreferences = transform(preferences)

            if (updatedPreferences.applyGlobally) {
                updateAllPreferencesUseCase(updatedPreferences)
            } else {
                updatePreferencesUseCase(repository.instance.id, updatedPreferences)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            currentRepository?.refreshLibrary()
        }
    }

    fun toggleMonitored(item: ArrMedia) {
        viewModelScope.launch {
            val repository = currentRepository ?: return@launch
            toggleMonitorUseCase.toggleMedia(item, repository)
        }
    }

    fun performAutomaticLookup(item: ArrMedia) {
        val mediaId = item.id ?: return
        viewModelScope.launch {
            val repository = currentRepository ?: return@launch
            performAutomaticSearchUseCase(mediaId, instanceType, repository)
                .onSuccess { _lastSearchResult.value = true }
                .onError { _, _, _ -> _lastSearchResult.value = false }
            _lastSearchResult.value = null
        }
    }

    fun performRefresh(item: ArrMedia) {
        val mediaId = item.id ?: return
        viewModelScope.launch {
            val repository = currentRepository ?: return@launch
            performRefreshUseCase(mediaId, instanceType, repository)
        }
    }

    fun deleteMedia(item: ArrMedia, deleteFiles: Boolean, addImportExclusion: Boolean) {
        val mediaId = item.id ?: return
        viewModelScope.launch {
            val repository = currentRepository ?: return@launch
            deleteMediaUseCase(mediaId, deleteFiles, addImportExclusion, repository)
                .collect { status ->
                    _deleteStatus.value = status
                }
        }
    }

    fun editItem(item: ArrMedia, moveFiles: Boolean = false) {
        viewModelScope.launch {
            val repository = currentRepository ?: return@launch
            updateMediaUseCase.edit(item, moveFiles, repository)
        }
    }

    fun resetDeleteStatus() {
        _deleteStatus.value = OperationStatus.Idle
    }

    fun resetEditItemStatus() {
        _editItemStatus.value = OperationStatus.Idle
    }

    fun toggleItemSelection(item: ArrMedia) {
        selectionState.toggle(item)
    }

    fun selectAllItems() {
        val success = uiState.value as? ArrLibrary.Success ?: return
        selectionState.selectAll(success.items)
    }

    fun toggleAllItems() {
        val success = uiState.value as? ArrLibrary.Success ?: return
        selectionState.toggleAll(success.items)
    }

    fun areAllItemsSelected(): Boolean {
        val success = uiState.value as? ArrLibrary.Success ?: return false
        return selectionState.areAllSelected(success.items)
    }

    fun clearSelection() {
        selectionState.clearSelection()
    }

    fun exitSelectionMode() {
        selectionState.exitSelectionMode()
    }

    fun enterSelectionMode() {
        selectionState.enterSelectionMode()
    }

    fun refreshSelected() {
        viewModelScope.launch {
            val repository = currentRepository ?: return@launch
            val selectedIds = selectionState.selectedItems.value.mapNotNull { it.id }
            if (selectedIds.isNotEmpty()) {
                performRefreshUseCase.bulkRefresh(selectedIds, instanceType, repository)
            }
        }
    }

    fun deleteSelected(deleteFiles: Boolean, addExclusion: Boolean) {
        viewModelScope.launch {
            val repository = currentRepository ?: return@launch
            val selectedItems = selectionState.selectedItems.value

            selectedItems.forEach { item ->
                item.id?.let { id ->
                    repository.delete(id, deleteFiles, addExclusion)
                }
            }

            selectionState.exitSelectionMode()
            repository.refreshLibrary()
        }
    }

    fun toggleMonitoringForSelected() {
        viewModelScope.launch {
            val repository = currentRepository ?: return@launch
            val selectedItems = selectionState.selectedItems.value

            selectedItems.forEach { item ->
                item.id?.let { id ->
                    repository.setMonitorState(id, !item.monitored)
                }
            }

            repository.refreshLibrary()
        }
    }
}