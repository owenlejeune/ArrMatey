package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.state.AuthorFilesState
import com.dnfapps.arrmatey.arr.usecase.GetAuthorFilesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthorFilesViewModel(
    private val authorId: Long,
    private val getAuthorFilesUseCase: GetAuthorFilesUseCase
): ViewModel() {

    private val _uiState = MutableStateFlow(AuthorFilesState())
    val uiState: StateFlow<AuthorFilesState> = _uiState.asStateFlow()

    init {
        observeAuthorFiles()
        refreshHistory()
    }

    private fun observeAuthorFiles() {
        viewModelScope.launch {
            getAuthorFilesUseCase(authorId)
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    fun refreshHistory() {
        viewModelScope.launch {
            getAuthorFilesUseCase.refreshHistory(authorId)
        }
    }
}