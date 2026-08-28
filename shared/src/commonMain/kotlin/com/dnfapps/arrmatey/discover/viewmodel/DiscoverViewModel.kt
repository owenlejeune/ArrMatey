package com.dnfapps.arrmatey.discover.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.client.paging.PagedData
import com.dnfapps.arrmatey.client.paging.PagingController
import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.discover.model.SearchResult
import com.dnfapps.arrmatey.discover.usecase.GlobalSearchUseCase
import com.dnfapps.arrmatey.extensions.mergeWithLibrary
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.seerr.api.model.DiscoverResult
import com.dnfapps.arrmatey.seerr.usecase.GetDiscoverMoviesUseCase
import com.dnfapps.arrmatey.seerr.usecase.GetDiscoverTvUseCase
import com.dnfapps.arrmatey.seerr.usecase.GetTrendingUseCase
import com.dnfapps.arrmatey.seerr.usecase.GetUpcomingMoviesUseCase
import com.dnfapps.arrmatey.seerr.usecase.GetUpcomingTvUseCase
import com.dnfapps.arrmatey.seerr.usecase.SearchSeerrUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class DiscoverViewModel(
    private val instanceManager: InstanceManager,
    private val instanceRepository: InstanceRepository,
    private val getTrendingUseCase: GetTrendingUseCase,
    private val getDiscoverMoviesUseCase: GetDiscoverMoviesUseCase,
    private val getDiscoverTvUseCase: GetDiscoverTvUseCase,
    private val getUpcomingMoviesUseCase: GetUpcomingMoviesUseCase,
    private val getUpcomingTvUseCase: GetUpcomingTvUseCase,
    private val searchSeerrUseCase: SearchSeerrUseCase,
    private val globalSearchUseCase: GlobalSearchUseCase,
    private val preferencesStore: PreferencesStore
) : ViewModel() {

    private val seerrRepository: StateFlow<SeerrInstanceRepository?> = instanceManager.getSelectedSeerrRepository()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val instances: StateFlow<List<Instance>> = instanceRepository.allInstancesFlow
        .map { all -> all.filter { it.type == InstanceType.Seerr } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedInstance: StateFlow<Instance?> = seerrRepository
        .map { it?.instance }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var trendingPagingController: PagingController<DiscoverResult>? = null
    private var moviesPagingController: PagingController<DiscoverResult>? = null
    private var tvPagingController: PagingController<DiscoverResult>? = null
    private var upcomingMoviesPagingController: PagingController<DiscoverResult>? = null
    private var upcomingTvPagingController: PagingController<DiscoverResult>? = null
    private var searchPagingController: PagingController<DiscoverResult>? = null
    private var searchJob: Job? = null

    private val _trendingState = MutableStateFlow(PagedData<DiscoverResult>())
    val trendingState: StateFlow<PagedData<DiscoverResult>> = _trendingState.asStateFlow()

    private val _moviesState = MutableStateFlow(PagedData<DiscoverResult>())
    val moviesState: StateFlow<PagedData<DiscoverResult>> = _moviesState.asStateFlow()

    private val _tvState = MutableStateFlow(PagedData<DiscoverResult>())
    val tvState: StateFlow<PagedData<DiscoverResult>> = _tvState.asStateFlow()

    private val _upcomingMoviesState = MutableStateFlow(PagedData<DiscoverResult>())
    val upcomingMoviesState: StateFlow<PagedData<DiscoverResult>> = _upcomingMoviesState.asStateFlow()

    private val _upcomingTvState = MutableStateFlow(PagedData<DiscoverResult>())
    val upcomingTvState: StateFlow<PagedData<DiscoverResult>> = _upcomingTvState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchState = MutableStateFlow<List<SearchResult>>(emptyList())
    
    private val allLibraries: StateFlow<List<ArrMedia>> = instanceManager.observeAllArrLibraries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchState: StateFlow<List<SearchResult>> = combine(_searchState, allLibraries) { results, libraries ->
        results.map { result ->
            if (result is SearchResult.ArrMediaResult) {
                val merged = listOf(result.media).mergeWithLibrary(libraries).first()
                result.copy(media = merged)
            } else {
                result
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    val searchShowBanners: StateFlow<Boolean> = preferencesStore.searchShowBanners
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val searchShowInstanceIndicatorShadow: StateFlow<Boolean> = preferencesStore.searchShowInstanceIndicatorShadow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    init {
        observeRepository()
        observeSearchQuery()
    }

    private fun observeRepository() {
        viewModelScope.launch {
            seerrRepository.collectLatest { repo ->
                if (repo != null) {
                    launch {
                        val controller = getTrendingUseCase.createPagingController(repo, viewModelScope)
                        trendingPagingController = controller
                        controller.loadInitialPage()
                        controller.state.collect {
                            _trendingState.value = it
                        }
                    }
                    launch {
                        val controller = getDiscoverMoviesUseCase.createPagingController(repo, viewModelScope)
                        moviesPagingController = controller
                        controller.loadInitialPage()
                        controller.state.collect {
                            _moviesState.value = it
                        }
                    }
                    launch {
                        val controller = getDiscoverTvUseCase.createPagingController(repo, viewModelScope)
                        tvPagingController = controller
                        controller.loadInitialPage()
                        controller.state.collect {
                            _tvState.value = it
                        }
                    }
                    launch {
                        val controller = getUpcomingMoviesUseCase.createPagingController(repo, viewModelScope)
                        upcomingMoviesPagingController = controller
                        controller.loadInitialPage()
                        controller.state.collect {
                            _upcomingMoviesState.value = it
                        }
                    }
                    launch {
                        val controller = getUpcomingTvUseCase.createPagingController(repo, viewModelScope)
                        upcomingTvPagingController = controller
                        controller.loadInitialPage()
                        controller.state.collect {
                            _upcomingTvState.value = it
                        }
                    }
                } else {
                    trendingPagingController = null
                    moviesPagingController = null
                    tvPagingController = null
                    upcomingMoviesPagingController = null
                    upcomingTvPagingController = null
                    _trendingState.value = PagedData()
                    _moviesState.value = PagedData()
                    _tvState.value = PagedData()
                    _upcomingMoviesState.value = PagedData()
                    _upcomingTvState.value = PagedData()
                }
            }
        }
    }

    private fun observeSearchQuery() {
        viewModelScope.launch {
            searchQuery
                .debounce(500.milliseconds)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isNotEmpty()) {
                        performSearch(query)
                    } else {
                        _searchState.value = emptyList()
                    }
                }
        }
    }

    private fun performSearch(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _isSearching.value = true
            _searchState.value = globalSearchUseCase(query)
            _isSearching.value = false
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadNextTrendingPage() {
        trendingPagingController?.loadNextPage()
    }

    fun loadNextMoviesPage() {
        moviesPagingController?.loadNextPage()
    }

    fun loadNextTvPage() {
        tvPagingController?.loadNextPage()
    }

    fun loadNextUpcomingMoviesPage() {
        upcomingMoviesPagingController?.loadNextPage()
    }

    fun loadNextUpcomingTvPage() {
        upcomingTvPagingController?.loadNextPage()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            trendingPagingController?.refresh()
            moviesPagingController?.refresh()
            tvPagingController?.refresh()
            upcomingMoviesPagingController?.refresh()
            upcomingTvPagingController?.refresh()
            _isRefreshing.value = false
        }
    }
}
