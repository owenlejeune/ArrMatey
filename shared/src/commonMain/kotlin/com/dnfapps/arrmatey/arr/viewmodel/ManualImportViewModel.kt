package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.ManualImportFile
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.service.ActivityQueueService
import com.dnfapps.arrmatey.arr.usecase.ManualImportUseCase
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ManualImportViewModel(
    val item: QueueItem,
    private val manualImportUseCase: ManualImportUseCase,
    private val activityQueueService: ActivityQueueService,
) : ViewModel() {
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isWorking = MutableStateFlow(false)
    val isWorking: StateFlow<Boolean> = _isWorking.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _files = MutableStateFlow<List<ManualImportFile>>(emptyList())
    val files: StateFlow<List<ManualImportFile>> = _files.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds: StateFlow<Set<String>> = _selectedIds.asStateFlow()

    private val _importSuccess = MutableStateFlow(false)
    val importSuccess: StateFlow<Boolean> = _importSuccess.asStateFlow()

    init {
        loadFiles()
    }

    fun loadFiles() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            manualImportUseCase.getImportableFiles(item)
                .onSuccess { importableFiles ->
                    _files.value = importableFiles
                    val acceptable = importableFiles.filter { it.isAcceptable }.map { it.stableId }
                    _selectedIds.value = if (acceptable.isNotEmpty()) acceptable.toSet() else importableFiles.map { it.stableId }.toSet()
                    _isLoading.value = false
                }
                .onError { _, message, _ ->
                    _error.value = message ?: "Failed to fetch importable files"
                    _isLoading.value = false
                }
        }
    }

    fun toggleFileSelected(file: ManualImportFile) {
        val id = file.stableId
        val current = _selectedIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _selectedIds.value = current
    }

    fun setFileSelected(file: ManualImportFile, isSelected: Boolean) {
        val id = file.stableId
        val current = _selectedIds.value.toMutableSet()
        if (isSelected) {
            current.add(id)
        } else {
            current.remove(id)
        }
        _selectedIds.value = current
    }

    fun importFiles(onSuccess: (() -> Unit)? = null) {
        val selectedFiles = _files.value.filter { _selectedIds.value.contains(it.stableId) }
        if (selectedFiles.isEmpty()) return

        _isWorking.value = true
        _error.value = null
        viewModelScope.launch {
            manualImportUseCase.executeManualImport(item, selectedFiles)
                .collect { status ->
                    when (status) {
                        is OperationStatus.Success -> {
                            _isWorking.value = false
                            _importSuccess.value = true
                            activityQueueService.removeTaskLocally(item.id)
                            activityQueueService.manualRefresh()
                            onSuccess?.invoke()
                        }
                        is OperationStatus.Error -> {
                            _isWorking.value = false
                            _error.value = status.message ?: "Failed to import files"
                        }
                        is OperationStatus.InProgress -> {
                            _isWorking.value = true
                        }
                        OperationStatus.Idle -> Unit
                    }
                }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
