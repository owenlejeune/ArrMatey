package com.dnfapps.arrmatey.discover.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.CalendarItem
import com.dnfapps.arrmatey.client.paging.PagedData
import com.dnfapps.arrmatey.client.paging.PagingController
import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.datastore.DiscoverSectionPreferences
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.discover.model.DiscoverCategory
import com.dnfapps.arrmatey.discover.model.SearchResult
import com.dnfapps.arrmatey.discover.usecase.GlobalSearchUseCase
import com.dnfapps.arrmatey.extensions.mergeWithLibrary
import com.dnfapps.networking.asSuccess
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.seerr.api.model.DiscoverResult
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.usecase.GetDiscoverMoviesUseCase
import com.dnfapps.arrmatey.seerr.usecase.GetDiscoverTvUseCase
import com.dnfapps.arrmatey.seerr.usecase.GetTrendingUseCase
import com.dnfapps.arrmatey.seerr.usecase.GetUpcomingMoviesUseCase
import com.dnfapps.arrmatey.seerr.usecase.GetUpcomingTvUseCase
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
    private val globalSearchUseCase: GlobalSearchUseCase,
    private val preferencesStore: PreferencesStore,
) : ViewModel() {
    private val seerrRepository: StateFlow<SeerrInstanceRepository?> =
        instanceManager
            .getSelectedSeerrRepository()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null,
            )

    val instances: StateFlow<List<Instance>> =
        instanceRepository.allInstancesFlow
            .map { all -> all.filter { it.type == InstanceType.Seerr } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    val selectedInstance: StateFlow<Instance?> =
        seerrRepository
            .map { it?.instance }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null,
            )

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

    val discoverSectionPreferences: StateFlow<DiscoverSectionPreferences> =
        preferencesStore.discoverSectionPreferences
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DiscoverSectionPreferences())

    val isInitialLoading: StateFlow<Boolean> =
        combine(
            _trendingState,
            _moviesState,
            _tvState,
            _upcomingMoviesState,
            _upcomingTvState,
            discoverSectionPreferences,
        ) { flows ->
            @Suppress("UNCHECKED_CAST")
            val trending = flows[0] as PagedData<DiscoverResult>

            @Suppress("UNCHECKED_CAST")
            val movies = flows[1] as PagedData<DiscoverResult>

            @Suppress("UNCHECKED_CAST")
            val tv = flows[2] as PagedData<DiscoverResult>

            @Suppress("UNCHECKED_CAST")
            val upcomingMovies = flows[3] as PagedData<DiscoverResult>

            @Suppress("UNCHECKED_CAST")
            val upcomingTv = flows[4] as PagedData<DiscoverResult>
            val prefs = flows[5] as DiscoverSectionPreferences

            val visible = prefs.visibleCategories.toSet()
            val statesMap =
                mapOf(
                    DiscoverCategory.TRENDING to trending,
                    DiscoverCategory.POPULAR_MOVIES to movies,
                    DiscoverCategory.POPULAR_SERIES to tv,
                    DiscoverCategory.UPCOMING_MOVIES to upcomingMovies,
                    DiscoverCategory.UPCOMING_SERIES to upcomingTv,
                )
            visible.any { category ->
                val state = statesMap[category]
                state != null && state.isLoading && state.items.isEmpty()
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false,
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchState = MutableStateFlow<List<SearchResult>>(emptyList())

    private val allLibraries: StateFlow<List<ArrMedia>> =
        instanceManager
            .observeAllArrLibraries()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    val searchState: StateFlow<List<SearchResult>> =
        combine(_searchState, allLibraries) { results, libraries ->
            results.map { result ->
                when (result) {
                    is SearchResult.ArrMediaResult -> {
                        val merged = listOf(result.media).mergeWithLibrary(libraries).first()
                        result.copy(media = merged)
                    }

                    is SearchResult.SeerrMediaResult -> {
                        val tmdbId = result.result.id
                        val cleanTitle = (result.result.title ?: result.result.name)?.replace(Regex("[^a-zA-Z0-9]"), "")?.lowercase()
                        val match = if (result.result.mediaType == RequestType.Movie) {
                            libraries.filterIsInstance<ArrMovie>()
                                .firstOrNull { (it.tmdbId != 0L && it.tmdbId == tmdbId) || (cleanTitle != null && it.cleanTitle?.equals(cleanTitle, ignoreCase = true) == true) }
                        } else if (result.result.mediaType == RequestType.Tv) {
                            libraries.filterIsInstance<ArrSeries>().firstOrNull {
                                (it.tmdbId != null && it.tmdbId != 0L && it.tmdbId == tmdbId) ||
                                    (cleanTitle != null && it.cleanTitle?.equals(cleanTitle, ignoreCase = true) == true)
                            }
                        } else null

                        if (match != null) {
                            val instanceId = (match as? ArrMovie)?.instanceId
                                ?: (match as? CalendarItem)?.instanceId
                                ?: instanceManager.getAllArrRepositories().firstOrNull { repo ->
                                    repo.library.value?.asSuccess()?.data?.any { it.id == match.id } == true
                                }?.instance?.id
                            SearchResult.ArrMediaResult(
                                media = match,
                                instanceId = instanceId,
                                originalRank = result.originalRank,
                            )
                        } else {
                            result
                        }
                    }

                    is SearchResult.SeerrPersonResult -> result
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    val searchShowBanners: StateFlow<Boolean> =
        preferencesStore.searchShowBanners
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

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
                        searchJob?.cancel()
                        _searchState.value = emptyList()
                        _isSearching.value = false
                    }
                }
        }
    }

    private fun performSearch(query: String) {
        searchJob?.cancel()
        searchJob =
            viewModelScope.launch {
                _isSearching.value = true
                _searchState.value = emptyList()
                globalSearchUseCase(query).collect { results ->
                    _searchState.value = results
                }
                _isSearching.value = false
            }
    }

    fun updateSearchQuery(query: String) {
        if (_searchQuery.value != query) {
            searchJob?.cancel()
            _isSearching.value = false
            _searchQuery.value = query
            if (query.isEmpty()) {
                _searchState.value = emptyList()
            }
        }
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

    fun getStateForCategory(category: DiscoverCategory): StateFlow<PagedData<DiscoverResult>> =
        when (category) {
            DiscoverCategory.TRENDING -> trendingState
            DiscoverCategory.POPULAR_MOVIES -> moviesState
            DiscoverCategory.POPULAR_SERIES -> tvState
            DiscoverCategory.UPCOMING_MOVIES -> upcomingMoviesState
            DiscoverCategory.UPCOMING_SERIES -> upcomingTvState
        }

    fun loadNextPageForCategory(category: DiscoverCategory) {
        when (category) {
            DiscoverCategory.TRENDING -> loadNextTrendingPage()
            DiscoverCategory.POPULAR_MOVIES -> loadNextMoviesPage()
            DiscoverCategory.POPULAR_SERIES -> loadNextTvPage()
            DiscoverCategory.UPCOMING_MOVIES -> loadNextUpcomingMoviesPage()
            DiscoverCategory.UPCOMING_SERIES -> loadNextUpcomingTvPage()
        }
    }

    fun updateDiscoverSectionPreferences(prefs: DiscoverSectionPreferences) {
        preferencesStore.saveDiscoverSectionPreferences(prefs)
    }

    fun resetDiscoverSectionPreferences() {
        preferencesStore.resetDiscoverSectionPreferences()
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
