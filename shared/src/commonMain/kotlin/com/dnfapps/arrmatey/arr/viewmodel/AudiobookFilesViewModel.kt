package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.state.AudiobookFilesState
import com.dnfapps.arrmatey.arr.usecase.GetAudiobookFilesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AudiobookFilesViewModel(
    private val audiobookId: Long,
    private val getAudiobookFilesUseCase: GetAudiobookFilesUseCase
): ViewModel() {

    private val _uiState = MutableStateFlow(AudiobookFilesState())
    val uiState: StateFlow<AudiobookFilesState> = _uiState.asStateFlow()

    init {
        observeAudiobookFiles()
        refreshHistory()
    }

    private fun observeAudiobookFiles() {
        viewModelScope.launch {
            getAudiobookFilesUseCase(audiobookId)
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    fun refreshHistory() {
        viewModelScope.launch {
            getAudiobookFilesUseCase.refreshHistory(audiobookId)
        }
    }
}