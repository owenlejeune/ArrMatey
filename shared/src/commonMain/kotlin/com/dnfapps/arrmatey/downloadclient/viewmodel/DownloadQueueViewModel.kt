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
import com.dnfapps.arrmatey.downloadclient.state.DownloadQueueSortState
import com.dnfapps.arrmatey.downloadclient.state.DownloadQueueState
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

    private val _searchQuery = MutableStateFlow("")

    private val _clientIdsFilters = MutableStateFlow<List<Long>>(emptyList())
    val clientIdsFilters: StateFlow<List<Long>> = _clientIdsFilters.asStateFlow()


    val sortState: StateFlow<DownloadQueueSortState> = observeDownloadClientPreferencesUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DownloadQueueSortState()
        )

    val downloadQueueState: StateFlow<DownloadQueueBundle> =
        combine(
            downloadQueueService.allTransfers,
            _searchQuery,
            sortState,
            _clientIdsFilters
        ) { queueState, query, sorting, filterIds ->
            val filtered = queueState.queueItems.filter { item ->
                item.name.contains(query, ignoreCase = true) &&
                        filterIds.contains(item.client.id)
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

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val isPolling: StateFlow<Boolean> = downloadQueueService.isPolling
    val hasLoaded: StateFlow<Boolean> = downloadQueueService.hasLoaded

    init {
        viewModelScope.launch {
            downloadQueueService.manualRefresh()

            val clients = downloadQueueRepository.getAllDownloadClients()
            _clientIdsFilters.value = clients.map { it.id }
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
        _searchQuery.value = query
    }

    fun toggleClientIdFilter(id: Long) {
        _clientIdsFilters.update {
            val current = it.toMutableList()
            if (current.contains(id)) {
                current.remove(id)
            } else {
                current.add(id)
            }
            current
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
