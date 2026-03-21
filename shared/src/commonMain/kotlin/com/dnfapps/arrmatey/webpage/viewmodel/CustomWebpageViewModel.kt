package com.dnfapps.arrmatey.webpage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.instances.model.InstanceHeader
import com.dnfapps.arrmatey.webpage.model.CustomWebpage
import com.dnfapps.arrmatey.webpage.repository.CustomWebpageRepository
import com.dnfapps.arrmatey.webpage.state.CustomWebpageUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Clock

class CustomWebpageViewModel(
    private val repository: CustomWebpageRepository
) : ViewModel() {

    val webpages: StateFlow<List<CustomWebpage>> = repository.getAllWebpages()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(CustomWebpageUiState())
    val uiState: StateFlow<CustomWebpageUiState> = _uiState.asStateFlow()

    fun setName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun setUrl(url: String) {
        _uiState.value = _uiState.value.copy(url = url)
    }

    fun setHeaders(headers: List<InstanceHeader>) {
        _uiState.value = _uiState.value.copy(headers = headers)
    }

    fun loadWebpage(id: Long) {
        viewModelScope.launch {
            val webpage = repository.getWebpageById(id)
            if (webpage != null) {
                _uiState.value = CustomWebpageUiState(
                    id = webpage.id,
                    name = webpage.name,
                    url = webpage.url,
                    headers = webpage.headers,
                    isEditing = true
                )
            }
        }
    }

    fun saveWebpage() {
        val state = _uiState.value

        if (state.name.isBlank() || state.url.isBlank()) {
            _uiState.value = state.copy(error = "Name and URL are required")
            return
        }

        viewModelScope.launch {
            try {
                val webpage = CustomWebpage(
                    id = state.id,
                    name = state.name,
                    url = state.url,
                    headers = state.headers,
                    position = 0,
                    createdAt = if (state.isEditing) state.id else currentTimeMillis(),
                    updatedAt = currentTimeMillis()
                )

                if (state.isEditing) {
                    repository.updateWebpage(webpage)
                } else {
                    repository.addWebpage(webpage)
                }

                _uiState.value = CustomWebpageUiState()
            } catch (e: Exception) {
                _uiState.value = state.copy(error = e.message ?: "Failed to save")
            }
        }
    }

    fun deleteWebpage(id: Long) {
        viewModelScope.launch {
            repository.deleteWebpageById(id)
        }
    }

    fun reorderWebpages(webpages: List<CustomWebpage>) {
        viewModelScope.launch {
            repository.updatePositions(webpages)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun reset() {
        _uiState.value = CustomWebpageUiState()
    }

    private fun currentTimeMillis(): Long {
        return Clock.System.now().toEpochMilliseconds()
    }
}