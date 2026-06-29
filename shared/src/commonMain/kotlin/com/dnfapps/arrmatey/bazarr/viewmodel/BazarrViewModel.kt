package com.dnfapps.arrmatey.bazarr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.bazarr.state.BazarrLibrary
import com.dnfapps.arrmatey.bazarr.state.BazarrSection
import com.dnfapps.arrmatey.bazarr.usecase.GetBazarrLibraryUseCase
import com.dnfapps.arrmatey.bazarr.usecase.RefreshBazarrBadgesUseCase
import com.dnfapps.arrmatey.bazarr.usecase.ResetBazarrProvidersUseCase
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.instances.usecase.GetBazarrInstanceRepositoryUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Drives the Bazarr tab: paged "wanted" subtitle lists for episodes and movies, the
 * provider status list, and the selected sub-section.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BazarrViewModel(
    private val getBazarrLibraryUseCase: GetBazarrLibraryUseCase,
    private val getBazarrInstanceRepositoryUseCase: GetBazarrInstanceRepositoryUseCase,
    private val refreshBazarrBadgesUseCase: RefreshBazarrBadgesUseCase,
    private val resetBazarrProvidersUseCase: ResetBazarrProvidersUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedSection = MutableStateFlow(BazarrSection.entries.first())
    val selectedSection: StateFlow<BazarrSection> = _selectedSection.asStateFlow()

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    private val currentRepository = getBazarrInstanceRepositoryUseCase
        .observeSelected()
        .distinctUntilChanged { old, new -> old?.instance?.id == new?.instance?.id }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val uiState: StateFlow<BazarrLibrary> = combine(
        currentRepository.filterNotNull(),
        refreshTrigger
    ) { repo, _ -> repo }
        .flatMapLatest { repo ->
            getBazarrLibraryUseCase(repo)
        }
        .combine(_searchQuery) { library, query ->
            if (query.isBlank() || library !is BazarrLibrary.Success) {
                library
            } else {
                library.copy(
                    series = library.series.filter { it.title.contains(query, ignoreCase = true) },
                    movies = library.movies.filter { it.title.contains(query, ignoreCase = true) },
                    wantedEpisodes = library.wantedEpisodes.filter {
                        it.seriesTitle.contains(query, ignoreCase = true) ||
                                it.episodeTitle.contains(query, ignoreCase = true)
                    },
                    wantedMovies = library.wantedMovies.filter { it.title.contains(query, ignoreCase = true) },
                    providers = library.providers.filter { it.name.contains(query, ignoreCase = true) }
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BazarrLibrary.Initial
        )

    fun selectSection(section: BazarrSection) {
        _selectedSection.value = section
    }

    fun refresh() {
        viewModelScope.launch {
            val repo = currentRepository.value ?: return@launch
            repo.refresh()
            refreshBazarrBadgesUseCase(repo)
            refreshTrigger.emit(Unit)
        }
    }

    fun resetProviders() {
        viewModelScope.launch {
            val repo = currentRepository.value ?: return@launch
            resetBazarrProvidersUseCase(repo).onSuccess {
                refresh()
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}
