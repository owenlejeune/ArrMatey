package com.dnfapps.arrmatey.bazarr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.bazarr.state.BazarrLibrary
import com.dnfapps.arrmatey.bazarr.usecase.GetBazarrLibraryUseCase
import com.dnfapps.arrmatey.instances.repository.BazarrInstanceRepository
import com.dnfapps.arrmatey.instances.usecase.GetBazarrInstanceRepositoryUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class BazarrLibraryViewModel(
    private val getBazarrLibraryUseCase: GetBazarrLibraryUseCase,
    private val getBazarrRespositoryUseCase: GetBazarrInstanceRepositoryUseCase
): ViewModel() {

    private val currentRepository = getBazarrRespositoryUseCase
        .observeSelected()
        .filterNotNull()
        .distinctUntilChanged { old, new ->
            old.instance.id == new.instance.id
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val uiState: StateFlow<BazarrLibrary> = currentRepository
        .filterNotNull()
        .flatMapLatest { repository ->
            getBazarrLibraryUseCase(repository)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BazarrLibrary.Initial
        )

    fun refresh() {
        viewModelScope.launch {
            currentRepository.value?.refresh()
        }
    }
}
