package com.dnfapps.arrmatey.bazarr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.bazarr.state.BazarrLibrary
import com.dnfapps.arrmatey.bazarr.usecase.GetBazarrLibraryUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class BazarrLibraryViewModel(
    private val getBazarrLibraryUseCase: GetBazarrLibraryUseCase
): ViewModel() {

    val uiState: StateFlow<BazarrLibrary> =
        getBazarrLibraryUseCase()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = BazarrLibrary.Initial
            )
}
