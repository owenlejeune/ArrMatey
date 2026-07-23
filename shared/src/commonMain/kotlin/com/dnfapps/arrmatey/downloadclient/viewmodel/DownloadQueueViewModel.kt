package com.dnfapps.arrmatey.downloadclient.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.IndexerPrivacy
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.compose.utils.SortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder
import com.dnfapps.arrmatey.downloadclient.model.DownloadItem
import com.dnfapps.arrmatey.downloadclient.repository.DownloadClientRepository
import com.dnfapps.arrmatey.downloadclient.service.DownloadQueueService
import com.dnfapps.arrmatey.downloadclient.state.DownloadClientCommandState
import com.dnfapps.arrmatey.downloadclient.state.DownloadQueueBundle
import com.dnfapps.arrmatey.downloadclient.state.DownloadQueueFilterState
import com.dnfapps.arrmatey.downloadclient.state.DownloadQueueSortState
import com.dnfapps.arrmatey.downloadclient.state.DownloadQueueState
import com.dnfapps.arrmatey.downloadclient.model.DownloadItemStatus
import com.dnfapps.arrmatey.downloadclient.usecase.DeleteDownloadUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.ObserveDownloadClientsUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.ObserveDownloadQueueUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.PauseDownloadUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.RefreshDownloadQueueUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.ResumeDownloadUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.UpdateDownloadClientPreferencesUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.UpdateDownloadClientUseCase
import com.dnfapps.arrmatey.extensions.orderedSortedWith
import com.dnfapps.arrmatey.instances.usecase.ObserveDownloadClientPreferencesUseCase
import io.ktor.util.Hash.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

class DownloadQueueViewModel(
    private val downloadQueueRepository: DownloadClientRepository,
    private val downloadQueueService: DownloadQueueService,
    private val pauseDownloadUseCase: PauseDownloadUseCase,
    private val resumeDownloadUseCase: ResumeDownloadUseCase,
    private val deleteDownloadUseCase: DeleteDownloadUseCase,
    private val updateDownloadClientPreferencesUseCase: UpdateDownloadClientPreferencesUseCase,
    observeDownloadClientPreferencesUseCase: ObserveDownloadClientPreferencesUseCase
): ViewModel() {

    private val _filterState = MutableStateFlow(DownloadQueueFilterState())
    val filterState: StateFlow<DownloadQueueFilterState> = _filterState.asStateFlow()

    val sortState: StateFlow<DownloadQueueSortState> = observeDownloadClientPreferencesUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DownloadQueueSortState()
        )

    val downloadQueueState: StateFlow<DownloadQueueBundle> =
        combine(
            downloadQueueService.allTransfers,
            _filterState,
            sortState
        ) { queueState, filters, sorting ->
            val filtered = queueState.queueItems.filter { item ->
                val matchesQuery = filters.query.isBlank() || item.name.contains(filters.query, ignoreCase = true)
                val matchesClient = filters.clientIds.isEmpty() || filters.clientIds.contains(item.client.id)

                val matchesStatus = if (filters.selectedStatuses.isEmpty()) {
                    true
                } else {
                    val contains = filters.selectedStatuses.contains(item.status)
                    if (filters.excludeStatuses) !contains else contains
                }

                val matchesTags = if (filters.selectedTags.isEmpty()) {
                    true
                } else {
                    val contains = item.tags.any { filters.selectedTags.contains(it) }
                    if (filters.excludeTags) !contains else contains
                }

                val matchesActive = !filters.activeOnly || (item.downloadSpeed > 0 || item.uploadSpeed > 0)
                val matchesCompleted = !filters.completedOnly || item.progress >= 1.0

                matchesQuery && matchesClient && matchesStatus && matchesTags && matchesActive && matchesCompleted
            }
            val sorted = applySorting(sorting, filtered)
            queueState.copy(queueItems = sorted)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DownloadQueueBundle()
        )

    private val _commandState = MutableStateFlow<DownloadClientCommandState>(DownloadClientCommandState.Initial)
    val commandState: StateFlow<DownloadClientCommandState> = _commandState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(true)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val isPolling: StateFlow<Boolean> = downloadQueueService.isPolling
    val hasLoaded: StateFlow<Boolean> = downloadQueueService.hasLoaded

    val errorMessage: StateFlow<String?> = combine(
        downloadQueueService.allTransfers,
        _filterState,
        downloadQueueRepository.observeAllDownloadClients()
    ) { bundle, filters, allClients ->
        if (bundle.clientErrors.isEmpty()) return@combine null

        val selectedClientIds = filters.clientIds
        val errorsInSelected = bundle.clientErrors.filterKeys { it in selectedClientIds }

        when {
            allClients.size == 1 -> bundle.clientErrors.values.firstOrNull()
            selectedClientIds.size == 1 && errorsInSelected.isNotEmpty() -> errorsInSelected.values.first()
            else -> null
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    init {
        viewModelScope.launch {
            _isRefreshing.value = true
            downloadQueueService.manualRefresh()

            val clients = downloadQueueRepository.getAllDownloadClients()
            _filterState.update { it.copy(clientIds = clients.map { it.id }) }
            _isRefreshing.value = false
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            downloadQueueService.manualRefresh()
            _isRefreshing.value = false
        }
    }

    fun pauseDownload(id: String) {
        viewModelScope.launch {
            pauseDownloadUseCase(id).collect { state ->
                _commandState.value = state.toCommandState()
                if (state is OperationStatus.Success) downloadQueueService.manualRefresh()
            }
        }
    }

    fun resumeDownload(id: String) {
        viewModelScope.launch {
            resumeDownloadUseCase(id).collect { state ->
                _commandState.value = state.toCommandState()
                if (state is OperationStatus.Success) downloadQueueService.manualRefresh()
            }
        }
    }

    fun deleteDownload(id: String, deleteFiles: Boolean) {
        viewModelScope.launch {
            deleteDownloadUseCase(id, deleteFiles).collect { state ->
                _commandState.value = state.toCommandState()
                if (state is OperationStatus.Success) downloadQueueService.manualRefresh()
            }
        }
    }

    fun resetCommandState() {
        _commandState.value = DownloadClientCommandState.Initial
    }

    fun updateSearchQuery(query: String) {
        _filterState.update { it.copy(query = query) }
    }

    fun toggleClientIdFilter(id: Long) {
        _filterState.update { state ->
            val current = state.clientIds.toMutableList()
            if (current.contains(id)) {
                current.remove(id)
            } else {
                current.add(id)
            }
            state.copy(clientIds = current)
        }
    }

    fun toggleStatusFilter(status: DownloadItemStatus) {
        _filterState.update { state ->
            val current = state.selectedStatuses.toMutableSet()
            if (current.contains(status)) {
                current.remove(status)
            } else {
                current.add(status)
            }
            state.copy(selectedStatuses = current)
        }
    }

    fun toggleTagFilter(tag: String) {
        _filterState.update { state ->
            val current = state.selectedTags.toMutableSet()
            if (current.contains(tag)) {
                current.remove(tag)
            } else {
                current.add(tag)
            }
            state.copy(selectedTags = current)
        }
    }

    fun updateActiveOnly(activeOnly: Boolean) {
        _filterState.update { it.copy(activeOnly = activeOnly) }
    }

    fun updateCompletedOnly(completedOnly: Boolean) {
        _filterState.update { it.copy(completedOnly = completedOnly) }
    }

    fun updateExcludeTags(exclude: Boolean) {
        _filterState.update { it.copy(excludeTags = exclude) }
    }

    fun updateExcludeStatuses(exclude: Boolean) {
        _filterState.update { it.copy(excludeStatuses = exclude) }
    }

    fun clearFilters() {
        viewModelScope.launch {
            val clients = downloadQueueRepository.getAllDownloadClients()
            _filterState.value = DownloadQueueFilterState(clientIds = clients.map { it.id })
        }
    }

    fun updateSortBy(sortBy: SortBy) {
        safeSavePreference {
            it.copy(sortBy = sortBy)
        }
    }

    fun updateSortOrder(sortOrder: SortOrder) {
        safeSavePreference {
            it.copy(sortOrder = sortOrder)
        }
    }

    private fun applySorting(sortState: DownloadQueueSortState, items: List<DownloadItem>): List<DownloadItem> {
        val comparator: Comparator<DownloadItem> = when(sortState.sortBy) {
            SortBy.Title -> compareBy { it.name }
            SortBy.Added -> compareBy { it.addedOn }
            SortBy.Size -> compareBy { it.size }
            SortBy.Progress -> compareBy { it.progress }
            SortBy.DownloadSpeed -> compareBy { it.downloadSpeed }
            SortBy.UploadSpeed -> compareBy { it.uploadSpeed }
            SortBy.Eta -> compareBy { it.eta }
            else -> throw IllegalStateException("Unsupport download queue item sort by option: ${sortState.sortBy}")
        }
        return items.orderedSortedWith(sortState.sortOrder, comparator)
    }



    private fun OperationStatus.toCommandState(): DownloadClientCommandState = when (this) {
        is OperationStatus.Idle -> DownloadClientCommandState.Initial
        is OperationStatus.InProgress -> DownloadClientCommandState.Loading
        is OperationStatus.Success -> DownloadClientCommandState.Success
        is OperationStatus.Error -> DownloadClientCommandState.Error(
            code = code,
            message = message,
            cause = cause
        )
    }

    private fun safeSavePreference(transform: (DownloadQueueSortState) -> DownloadQueueSortState) {
        viewModelScope.launch {
            val updated = transform(sortState.value)
            updateDownloadClientPreferencesUseCase(updated)
        }
    }
}
