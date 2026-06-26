package com.dnfapps.arrmatey.bazarr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.bazarr.api.model.WantedEpisode
import com.dnfapps.arrmatey.bazarr.api.model.WantedMovie
import com.dnfapps.arrmatey.bazarr.state.BazarrSection
import com.dnfapps.arrmatey.bazarr.state.ProvidersUiState
import com.dnfapps.arrmatey.client.onError
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.client.paging.PagedData
import com.dnfapps.arrmatey.client.paging.PagingController
import com.dnfapps.arrmatey.instances.repository.BazarrInstanceRepository
import com.dnfapps.arrmatey.instances.usecase.GetBazarrInstanceRepositoryUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Drives the Bazarr tab: paged "wanted" subtitle lists for episodes and movies, the
 * provider status list, and the selected sub-section. Follows the same selected-instance
 * + [PagingController] pattern as [com.dnfapps.arrmatey.seerr.viewmodel.RequestsViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BazarrViewModel(
    getBazarrInstanceRepositoryUseCase: GetBazarrInstanceRepositoryUseCase
) : ViewModel() {

    private var episodesController: PagingController<WantedEpisode>? = null
    private var moviesController: PagingController<WantedMovie>? = null

    private val _wantedEpisodesState = MutableStateFlow(PagedData<WantedEpisode>())
    val wantedEpisodesState: StateFlow<PagedData<WantedEpisode>> = _wantedEpisodesState.asStateFlow()

    private val _wantedMoviesState = MutableStateFlow(PagedData<WantedMovie>())
    val wantedMoviesState: StateFlow<PagedData<WantedMovie>> = _wantedMoviesState.asStateFlow()

    private val _providersState = MutableStateFlow(ProvidersUiState())
    val providersState: StateFlow<ProvidersUiState> = _providersState.asStateFlow()

    private val _selectedSection = MutableStateFlow(BazarrSection.Episodes)
    val selectedSection: StateFlow<BazarrSection> = _selectedSection.asStateFlow()

    private var currentRepo: BazarrInstanceRepository? = null

    private val selectedRepository = getBazarrInstanceRepositoryUseCase
        .observeSelected()
        .distinctUntilChanged { old, new -> old?.instance?.id == new?.instance?.id }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            selectedRepository.collect { repo ->
                currentRepo = repo
                if (repo == null) {
                    _wantedEpisodesState.value = PagedData()
                    _wantedMoviesState.value = PagedData()
                    _providersState.value = ProvidersUiState()
                    return@collect
                }

                episodesController = PagingController(viewModelScope) { repo.getWantedEpisodesPaging() }
                moviesController = PagingController(viewModelScope) { repo.getWantedMoviesPaging() }

                viewModelScope.launch {
                    episodesController?.state?.collect { _wantedEpisodesState.value = it }
                }
                viewModelScope.launch {
                    moviesController?.state?.collect { _wantedMoviesState.value = it }
                }

                episodesController?.loadInitialPage()
                moviesController?.loadInitialPage()
                loadProviders()
                repo.refreshBadges()
            }
        }
    }

    fun selectSection(section: BazarrSection) {
        _selectedSection.value = section
    }

    fun loadMoreEpisodes() {
        episodesController?.loadNextPage()
    }

    fun loadMoreMovies() {
        moviesController?.loadNextPage()
    }

    fun refresh() {
        episodesController?.refresh()
        moviesController?.refresh()
        loadProviders()
        viewModelScope.launch { currentRepo?.refreshBadges() }
    }

    fun loadProviders() {
        val repo = currentRepo ?: return
        viewModelScope.launch {
            _providersState.value = _providersState.value.copy(isLoading = true, error = null)
            repo.getProviders()
                .onSuccess { _providersState.value = ProvidersUiState(providers = it) }
                .onError { _, message, _ ->
                    _providersState.value = ProvidersUiState(error = message ?: "Failed to load providers")
                }
        }
    }

    fun resetProviders() {
        val repo = currentRepo ?: return
        viewModelScope.launch {
            repo.resetProviders().onSuccess { loadProviders() }
        }
    }
}
