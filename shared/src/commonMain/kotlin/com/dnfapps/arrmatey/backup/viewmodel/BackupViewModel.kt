package com.dnfapps.arrmatey.backup.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.backup.state.ExportUiState
import com.dnfapps.arrmatey.backup.state.ImportUiState
import com.dnfapps.arrmatey.backup.usecase.ExportDataUseCase
import com.dnfapps.arrmatey.backup.usecase.ImportDataUseCase
import com.dnfapps.arrmatey.database.dao.InstanceDao
import com.dnfapps.arrmatey.downloadclient.database.DownloadClientDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BackupViewModel(
    private val exportDataUseCase: ExportDataUseCase,
    private val importDataUseCase: ImportDataUseCase,
    private val instanceDao: InstanceDao,
    private val downloadClientDao: DownloadClientDao
): ViewModel() {

    private val _exportUiState = MutableStateFlow(ExportUiState())
    val exportUiState: StateFlow<ExportUiState> = _exportUiState.asStateFlow()

    private val _importUiState = MutableStateFlow(ImportUiState())
    val importUiState: StateFlow<ImportUiState> = _importUiState.asStateFlow()

    init {
        loadExportableData()
    }

    private fun loadExportableData() {
        viewModelScope.launch {
            val instances = instanceDao.getAllInstances()
            val downloadClients = downloadClientDao.getAllDownloadClients()
            _exportUiState.update { it.copy(
                instances = instances,
                downloadClients = downloadClients,
                selectedInstanceIds = instances.map { i -> i.id }.toSet(),
                selectedDownloadClientIds = downloadClients.map { c -> c.id }.toSet()
            ) }
        }
    }

    fun toggleInstanceSelection(id: Long) {
        _exportUiState.update { state ->
            val newSelected = state.selectedInstanceIds.toMutableSet()
            if (newSelected.contains(id)) newSelected.remove(id) else newSelected.add(id)
            state.copy(selectedInstanceIds = newSelected)
        }
    }

    fun toggleDownloadClientSelection(id: Long) {
        _exportUiState.update { state ->
            val newSelected = state.selectedDownloadClientIds.toMutableSet()
            if (newSelected.contains(id)) newSelected.remove(id) else newSelected.add(id)
            state.copy(selectedDownloadClientIds = newSelected)
        }
    }

    fun setExportPassword(password: String) {
        _exportUiState.update { it.copy(password = password) }
    }

    fun toggleIncludePreferences() {
        _exportUiState.update { it.copy(includeInstancePreferences = !it.includeInstancePreferences) }
    }

    fun toggleIncludeTabPreferences() {
        _exportUiState.update { it.copy(includeTabPreferences = !it.includeTabPreferences) }
    }

    fun toggleIncludeUiPreferences() {
        _exportUiState.update { it.copy(includeUiPreferences = !it.includeUiPreferences) }
    }

    fun exportData(onExportReady: (String) -> Unit) {
        val state = _exportUiState.value
        if (state.password.isBlank()) return
        
        viewModelScope.launch {
            _exportUiState.update { it.copy(isExporting = true) }
            val encryptedData = exportDataUseCase(
                password = state.password,
                selectedInstanceIds = state.selectedInstanceIds,
                selectedDownloadClientIds = state.selectedDownloadClientIds,
                includeInstancePreferences = state.includeInstancePreferences,
                includeTabPreferences = state.includeTabPreferences,
                includeUiPreferences = state.includeUiPreferences
            )
            _exportUiState.update { it.copy(isExporting = false) }
            onExportReady(encryptedData)
        }
    }

    // Import logic
    fun setImportPassword(password: String) {
        _importUiState.update { it.copy(password = password) }
    }

    fun prepareImport(encryptedData: String) {
        val password = _importUiState.value.password
        if (password.isBlank()) return

        viewModelScope.launch {
            try {
                val backup = importDataUseCase.decryptBackup(encryptedData, password)
                _importUiState.update { it.copy(
                    decryptedBackup = backup,
                    selectedInstanceIndices = backup.instances.indices.toSet(),
                    selectedDownloadClientIndices = backup.downloadClients.indices.toSet(),
                    error = null
                ) }
            } catch (e: Exception) {
                _importUiState.update { it.copy(error = "Invalid password or corrupted backup file\n${e.message}") }
            }
        }
    }

    fun toggleImportInstanceSelection(index: Int) {
        _importUiState.update { state ->
            val newSelected = state.selectedInstanceIndices.toMutableSet()
            if (newSelected.contains(index)) newSelected.remove(index) else newSelected.add(index)
            state.copy(selectedInstanceIndices = newSelected)
        }
    }

    fun toggleImportDownloadClientSelection(index: Int) {
        _importUiState.update { state ->
            val newSelected = state.selectedDownloadClientIndices.toMutableSet()
            if (newSelected.contains(index)) newSelected.remove(index) else newSelected.add(index)
            state.copy(selectedDownloadClientIndices = newSelected)
        }
    }

    fun toggleImportTabPreferences() {
        _importUiState.update { it.copy(importTabPreferences = !it.importTabPreferences) }
    }

    fun toggleImportUiPreferences() {
        _importUiState.update { it.copy(importUiPreferences = !it.importUiPreferences) }
    }

    fun executeImport(onComplete: () -> Unit) {
        val state = _importUiState.value
        val backup = state.decryptedBackup ?: return

        viewModelScope.launch {
            _importUiState.update { it.copy(isImporting = true) }
            importDataUseCase.importSelected(
                backup = backup,
                selectedInstanceIndices = state.selectedInstanceIndices,
                selectedDownloadClientIndices = state.selectedDownloadClientIndices,
                importTabPreferences = state.importTabPreferences,
                importUiPreferences = state.importUiPreferences
            )
            _importUiState.update { it.copy(isImporting = false) }
            onComplete()
        }
    }
}
