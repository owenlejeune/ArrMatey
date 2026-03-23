package com.dnfapps.arrmatey.webpage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.instances.model.InstanceHeader
import com.dnfapps.arrmatey.webpage.model.CustomWebpage
import com.dnfapps.arrmatey.webpage.repository.CustomWebpageRepository
import com.dnfapps.arrmatey.webpage.state.CustomWebpageUiState
import com.dnfapps.arrmatey.webpage.usecase.AddCustomWebpageUseCase
import com.dnfapps.arrmatey.webpage.usecase.DeleteCustomWebpageUseCase
import com.dnfapps.arrmatey.webpage.usecase.UpdateCustomWebpageUseCase
import io.ktor.http.headers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

class CustomWebpageConfigurationViewModel(
    webpageId: Long?,
    private val repository: CustomWebpageRepository,
    private val addWebpageUseCase: AddCustomWebpageUseCase,
    private val updateWebpageUseCase: UpdateCustomWebpageUseCase,
    private val deleteCustomWebpageUseCase: DeleteCustomWebpageUseCase
) : ViewModel() {

    val webpages: StateFlow<List<CustomWebpage>> = repository.getAllWebpages()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(CustomWebpageUiState())
    val uiState: StateFlow<CustomWebpageUiState> = _uiState.asStateFlow()

    init {
        webpageId?.let { id ->
            loadWebpage(id)
        }
    }

    fun setName(name: String) {
        _uiState.value = _uiState.value.copy(
            name = name,
            saveButtonEnabled = name.isNotBlank() && _uiState.value.url.isNotBlank()
        )
    }

    fun setUrl(url: String) {
        _uiState.value = _uiState.value.copy(
            url = url,
            saveButtonEnabled = url.isNotBlank() && _uiState.value.name.isNotBlank()
        )
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
        viewModelScope.launch {
            val newWebpage = CustomWebpage(
                id = _uiState.value.id,
                name = _uiState.value.name,
                url = _uiState.value.url,
                headers = _uiState.value.headers
            )

            val result = if (_uiState.value.isEditing) {
                updateWebpageUseCase(newWebpage)
            } else {
                addWebpageUseCase(newWebpage)
            }
            _uiState.update { it.copy(saveResult = result) }
        }
    }

    fun deleteWebpage(id: Long) {
        viewModelScope.launch {
            deleteCustomWebpageUseCase(id)
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