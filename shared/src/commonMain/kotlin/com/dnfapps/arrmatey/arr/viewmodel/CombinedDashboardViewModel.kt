@file:Suppress("ktlint:standard:max-line-length")

package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.api.model.groupByTask
import com.dnfapps.arrmatey.arr.service.CalendarService
import com.dnfapps.arrmatey.arr.state.ArrInstanceDashboardState
import com.dnfapps.arrmatey.arr.state.BazarrDashboardState
import com.dnfapps.arrmatey.arr.state.CombinedDashboardState
import com.dnfapps.arrmatey.arr.state.DashboardCalendarItem
import com.dnfapps.arrmatey.arr.state.DownloadClientDashboardState
import com.dnfapps.arrmatey.arr.state.InstanceNetworkStatus
import com.dnfapps.arrmatey.arr.state.NetworkStatusState
import com.dnfapps.arrmatey.arr.state.ProwlarrDashboardState
import com.dnfapps.arrmatey.arr.state.SeerrDashboardState
import com.dnfapps.arrmatey.arr.state.TracearrDashboardState
import com.dnfapps.arrmatey.arr.usecase.DeleteQueueItemUseCase
import com.dnfapps.arrmatey.compose.DashboardCards
import com.dnfapps.arrmatey.compose.DashboardManager
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.downloadclient.model.DownloadItem
import com.dnfapps.arrmatey.downloadclient.model.DownloadTransferInfo
import com.dnfapps.arrmatey.downloadclient.repository.DownloadClientManager
import com.dnfapps.arrmatey.downloadclient.service.DownloadQueueService
import com.dnfapps.arrmatey.downloadclient.state.DownloadQueueBundle
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.BazarrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.instances.repository.ProwlarrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.TracearrRepository
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.arrmatey.seerr.api.model.ApprovalStatus
import com.dnfapps.arrmatey.seerr.api.model.DiscoverResult
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.utils.getNetworkUtils
import com.dnfapps.networking.NetworkResult
import dev.shivathapaa.logger.api.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class CombinedDashboardViewModel(
    private val instanceManager: InstanceManager,
    private val downloadClientManager: DownloadClientManager,
    private val downloadQueueService: DownloadQueueService,
    private val calendarService: CalendarService,
    private val dashboardManager: DashboardManager,
    private val preferencesStore: PreferencesStore,
    private val deleteQueueItemUseCase: DeleteQueueItemUseCase,
    private val logger: Logger,
) : ViewModel() {
    private val _removeItemState = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val removeItemState: StateFlow<OperationStatus> = _removeItemState.asStateFlow()
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _state = MutableStateFlow<CombinedDashboardState>(CombinedDashboardState.Initial)
    val state: StateFlow<CombinedDashboardState> = _state.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    val cards =
        dashboardManager.cardsOrder
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    val showFirstLaunchToast: StateFlow<Boolean> =
        preferencesStore.isFirstLaunch
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

    val showDashboardSearch: StateFlow<Boolean> =
        preferencesStore.showDashboardSearch
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = true,
            )

    val discoverSectionPreferences: StateFlow<com.dnfapps.arrmatey.datastore.DiscoverSectionPreferences> =
        preferencesStore.discoverSectionPreferences
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue =
                    com.dnfapps.arrmatey.datastore
                        .DiscoverSectionPreferences(),
            )

    private val arrInstancesFlow =
        instanceManager.instanceRepositories
            .flatMapLatest { repoMap ->
                val arrRepos = repoMap.values.filterIsInstance<ArrInstanceRepository>()
                if (arrRepos.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val flows =
                        arrRepos.map { repo ->
                            combine(
                                repo.softwareStatus,
                                repo.diskSpace,
                                repo.health,
                                repo.activityTasks.debounce(500.milliseconds),
                                repo.library,
                            ) { software, disks, health, activity, library ->
                                val libraryData = (library as? NetworkResult.Success)?.data ?: emptyList()
                                val totalItems = libraryData.size
                                val sizeOnDisk = libraryData.sumOf { it.fileSize ?: 0L }

                                ArrInstanceDashboardState(
                                    instance = repo.instance,
                                    softwareStatus = software,
                                    disks = disks,
                                    healthItems = health,
                                    library = libraryData,
                                    activityTasks = activity,
                                    activeCount = activity.size,
                                    totalItems = totalItems,
                                    sizeOnDisk = sizeOnDisk,
                                )
                            }
                        }
                    combine(flows) { it.toList() }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    private val seerrInstancesFlow =
        instanceManager.instanceRepositories
            .flatMapLatest { repoMap ->
                val seerrRepos = repoMap.values.filterIsInstance<SeerrInstanceRepository>()
                if (seerrRepos.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val flows =
                        seerrRepos.map { repo ->
                            combine(
                                repo.pendingRequestsCount,
                                repo.openIssuesCount,
                                repo.pendingRequests,
                                repo.openIssues,
                                repo.isOnline,
                            ) { pendingCount, issuesCount, requests, issues, isOnline ->
                                SeerrDashboardState(
                                    instance = repo.instance,
                                    pendingRequestsCount = pendingCount,
                                    openIssuesCount = issuesCount,
                                    pendingRequests = requests,
                                    openIssues = issues,
                                    isOnline = isOnline,
                                )
                            }
                        }
                    combine(flows) { it.toList() }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    private val prowlarrInstancesFlow =
        instanceManager.instanceRepositories
            .flatMapLatest { repoMap ->
                val prowlarrRepos = repoMap.values.filterIsInstance<ProwlarrInstanceRepository>()
                if (prowlarrRepos.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val flows =
                        prowlarrRepos.map { repo ->
                            combine(
                                repo.softwareStatus,
                                repo.indexerStatus,
                                repo.indexers,
                            ) { software, status, indexers ->
                                val failureCount = status.count { it.hasFailure }
                                ProwlarrDashboardState(
                                    instance = repo.instance,
                                    softwareStatus = software,
                                    totalIndexers = indexers.size,
                                    healthyIndexers = indexers.size - failureCount,
                                    failingIndexers = failureCount,
                                )
                            }
                        }
                    combine(flows) { it.toList() }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    private val bazarrInstancesFlow =
        instanceManager.instanceRepositories
            .flatMapLatest { repoMap ->
                val bazarrRepos = repoMap.values.filterIsInstance<BazarrInstanceRepository>()
                if (bazarrRepos.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val flows =
                        bazarrRepos.map { repo ->
                            combine(repo.wantedEpisodesCount, repo.wantedMoviesCount, repo.systemStatus) { episodes, movies, status ->
                                BazarrDashboardState(
                                    instance = repo.instance,
                                    wantedEpisodesCount = episodes,
                                    wantedMoviesCount = movies,
                                    isOnline = status != null,
                                )
                            }
                        }
                    combine(flows) { it.toList() }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    private val tracearrInstancesFlow =
        instanceManager.instanceRepositories
            .flatMapLatest { repoMap ->
                val tracearrRepos = repoMap.values.filterIsInstance<TracearrRepository>()
                if (tracearrRepos.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val flows =
                        tracearrRepos.map { repo ->
                            combine(
                                repo.todayStats,
                                repo.activeStreams,
                            ) { stats, streams ->
                                TracearrDashboardState(
                                    instance = repo.instance,
                                    stats = stats,
                                    activeStreams = streams,
                                )
                            }
                        }
                    combine(flows) { it.toList() }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    private val downloadsFlow =
        downloadQueueService.allTransfers
            .debounce(500.milliseconds)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = DownloadQueueBundle(),
            )

    private val calendarFlow =
        calendarService.items
            .map { itemsByDate ->
                val today =
                    Clock.System
                        .now()
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date
                val todayItems = (itemsByDate[today] ?: emptyList()).map { DashboardCalendarItem(it, today) }
                val upcomingItems =
                    (1..7).flatMap { offset ->
                        val date = today.plus(offset, DateTimeUnit.DAY)
                        (itemsByDate[date] ?: emptyList()).map { DashboardCalendarItem(it, date) }
                    }
                todayItems to upcomingItems
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList<DashboardCalendarItem>() to emptyList(),
            )

    private val recentlyAddedFlow =
        arrInstancesFlow
            .map { instances -> instances.flatMap { it.library } }
            .distinctUntilChangedBy { it.size }
            .flowOn(Dispatchers.Default)
            .map { library ->
                library
                    .asSequence()
                    .filter { it.added != null }
                    .sortedByDescending { it.added }
                    .take(10)
                    .toList()
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    private val recentActivityFlow =
        arrInstancesFlow
            .map { instances -> instances.flatMap { it.activityTasks } }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .map { tasks -> tasks.groupByTask().sortedByDescending { it.added } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    private val downloadClientsFlow =
        combine(
            downloadsFlow,
            downloadClientManager.downloadClientApis,
        ) { downloads, clientApis ->
            val downloadClients =
                downloads.transferInfo
                    .asSequence()
                    .map { transfer ->
                        val clientItems = downloads.queueItems.filter { it.client.id == transfer.client.id }
                        DownloadClientDashboardState(
                            client = transfer.client,
                            transferInfo = transfer,
                            isOnline = true,
                            activeDownloadsCount =
                                clientItems.count {
                                    (it.downloadSpeed > 0) || (it.uploadSpeed > 0) || (it.progress < 1.0)
                                },
                        )
                    }.toMutableList()

            clientApis.keys.forEach { clientId ->
                if (downloadClients.none { it.client.id == clientId }) {
                    downloadClientManager.getDownloadClientById(clientId)?.let { client ->
                        val clientItems = downloads.queueItems.filter { it.client.id == clientId }
                        downloadClients.add(
                            DownloadClientDashboardState(
                                client = client,
                                isOnline = false,
                                activeDownloadsCount =
                                    clientItems.count {
                                        (it.downloadSpeed > 0) || (it.uploadSpeed > 0) || (it.progress < 1.0)
                                    },
                            ),
                        )
                    }
                }
            }
            downloadClients
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    private val _activeDownloadsFlow =
        downloadsFlow
            .map { downloads ->
                downloads.queueItems.sortedByDescending { it.progress }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    private val _trendingDiscover = MutableStateFlow<List<DiscoverResult>>(emptyList())
    private val _popularMoviesDiscover = MutableStateFlow<List<DiscoverResult>>(emptyList())
    private val _popularTvDiscover = MutableStateFlow<List<DiscoverResult>>(emptyList())
    private val _upcomingMoviesDiscover = MutableStateFlow<List<DiscoverResult>>(emptyList())
    private val _upcomingTvDiscover = MutableStateFlow<List<DiscoverResult>>(emptyList())
    private val _quickPickItem = MutableStateFlow<DiscoverResult?>(null)

    init {
        observeDashboard()
        viewModelScope.launch {
            instanceManager.instanceRepositories.collect {
                fetchDiscoverData()
            }
        }
        refresh()
    }

    fun shuffleQuickPick() {
        val pool =
            (_trendingDiscover.value + _popularMoviesDiscover.value + _popularTvDiscover.value)
                .distinctBy { "${it.mediaType.name}_${it.id}" }
        if (pool.isNotEmpty()) {
            val current = _quickPickItem.value
            val available =
                if (pool.size > 1 && current != null) {
                    pool.filterNot { it.id == current.id && it.mediaType == current.mediaType }
                } else {
                    pool
                }
            _quickPickItem.value = available.random()
        }
    }

    private fun observeDashboard() {
        viewModelScope.launch {
            combine(
                listOf(
                    arrInstancesFlow,
                    seerrInstancesFlow,
                    prowlarrInstancesFlow,
                    bazarrInstancesFlow,
                    tracearrInstancesFlow,
                    downloadClientsFlow,
                    recentActivityFlow,
                    recentlyAddedFlow,
                    downloadsFlow.map { it.transferInfo },
                    _activeDownloadsFlow,
                    calendarFlow,
                    _trendingDiscover,
                    _popularMoviesDiscover,
                    _popularTvDiscover,
                    _upcomingMoviesDiscover,
                    _upcomingTvDiscover,
                    _quickPickItem,
                    _isRefreshing,
                ),
            ) { args ->
                @Suppress("UNCHECKED_CAST")
                val instances = args[0] as List<ArrInstanceDashboardState>

                @Suppress("UNCHECKED_CAST")
                val seerrInstances = args[1] as List<SeerrDashboardState>

                @Suppress("UNCHECKED_CAST")
                val prowlarrStats = args[2] as List<ProwlarrDashboardState>

                @Suppress("UNCHECKED_CAST")
                val bazarrStats = args[3] as List<BazarrDashboardState>

                @Suppress("UNCHECKED_CAST")
                val tracearrStats = args[4] as List<TracearrDashboardState>

                @Suppress("UNCHECKED_CAST")
                val downloadClients = args[5] as List<DownloadClientDashboardState>

                @Suppress("UNCHECKED_CAST")
                val activityQueue = args[6] as List<QueueItem>

                @Suppress("UNCHECKED_CAST")
                val recentlyAdded = args[7] as List<ArrMedia>

                @Suppress("UNCHECKED_CAST")
                val downloadTransfers = args[8] as List<DownloadTransferInfo>

                @Suppress("UNCHECKED_CAST")
                val activeDownloads = args[9] as List<DownloadItem>

                @Suppress("UNCHECKED_CAST")
                val calendarPair = args[10] as Pair<List<DashboardCalendarItem>, List<DashboardCalendarItem>>
                val todayCalendar = calendarPair.first
                val upcomingCalendar = calendarPair.second

                @Suppress("UNCHECKED_CAST")
                val trending = args[11] as List<com.dnfapps.arrmatey.seerr.api.model.DiscoverResult>

                @Suppress("UNCHECKED_CAST")
                val popularMovies = args[12] as List<com.dnfapps.arrmatey.seerr.api.model.DiscoverResult>

                @Suppress("UNCHECKED_CAST")
                val popularTv = args[13] as List<com.dnfapps.arrmatey.seerr.api.model.DiscoverResult>

                @Suppress("UNCHECKED_CAST")
                val upcomingMovies = args[14] as List<com.dnfapps.arrmatey.seerr.api.model.DiscoverResult>

                @Suppress("UNCHECKED_CAST")
                val upcomingTv = args[15] as List<com.dnfapps.arrmatey.seerr.api.model.DiscoverResult>

                val quickPick = args[16] as com.dnfapps.arrmatey.seerr.api.model.DiscoverResult?

                val refreshing = args[17] as Boolean

                CombinedDashboardState.Success(
                    instances = instances,
                    seerrInstances = seerrInstances,
                    downloadClients = downloadClients,
                    activityQueue = activityQueue,
                    recentlyAdded = recentlyAdded,
                    downloadTransfers = downloadTransfers,
                    activeDownloads = activeDownloads,
                    calendarItems = todayCalendar,
                    upcomingCalendarItems = upcomingCalendar,
                    prowlarrStats = prowlarrStats,
                    bazarrStats = bazarrStats,
                    tracearrStats = tracearrStats,
                    trendingMedia = trending,
                    popularMovies = popularMovies,
                    popularTv = popularTv,
                    upcomingMovies = upcomingMovies,
                    upcomingTv = upcomingTv,
                    quickPickItem = quickPick,
                    networkStatus =
                        resolveNetworkStatus(
                            instances,
                            seerrInstances,
                            prowlarrStats,
                            bazarrStats,
                            downloadClients,
                            tracearrStats,
                        ),
                    isRefreshing = refreshing,
                )
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    private suspend fun enrichDiscoverResult(
        seerrRepo: SeerrInstanceRepository,
        item: DiscoverResult,
    ): DiscoverResult =
        try {
            when (item.mediaType) {
                RequestType.Movie -> {
                    val detailsRes = seerrRepo.client.getMovieDetails(item.id)
                    if (detailsRes is NetworkResult.Success) {
                        val details = detailsRes.data
                        item.copy(
                            keywords = details.keywords,
                            productionCompanies = details.productionCompanies,
                            contentRating =
                                details.getCertification("US") ?: details.releases
                                    ?.results
                                    ?.firstOrNull()
                                    ?.rating,
                        )
                    } else {
                        item
                    }
                }
                RequestType.Tv -> {
                    val detailsRes = seerrRepo.client.getTvDetails(item.id)
                    if (detailsRes is NetworkResult.Success) {
                        val details = detailsRes.data
                        item.copy(
                            keywords = details.keywords,
                            productionCompanies = details.productionCompanies,
                            networks = details.networks,
                            contentRating =
                                details.getCertification("US") ?: details.contentRatings
                                    ?.results
                                    ?.firstOrNull()
                                    ?.rating,
                        )
                    } else {
                        item
                    }
                }
                else -> item
            }
        } catch (_: Exception) {
            item
        }

    private suspend fun fetchDiscoverData() {
        val seerrRepo = instanceManager.getAllSeerrRepositories().firstOrNull() ?: return
        try {
            val trendingRes = seerrRepo.client.getTrending(page = 1)
            if (trendingRes is NetworkResult.Success) {
                val enrichedTrending =
                    coroutineScope {
                        trendingRes.data.results
                            .mapIndexed { index, item ->
                                if (index < 10) {
                                    async { enrichDiscoverResult(seerrRepo, item) }
                                } else {
                                    async { item }
                                }
                            }.awaitAll()
                    }
                _trendingDiscover.value = enrichedTrending
            }
        } catch (e: Exception) {
            logger.error(e) { "Error fetching trending discover data" }
        }

        try {
            val moviesRes = seerrRepo.client.getDiscoverMovies(page = 1)
            if (moviesRes is NetworkResult.Success) {
                val enrichedMovies =
                    coroutineScope {
                        moviesRes.data.results
                            .mapIndexed { index, item ->
                                if (index < 5) {
                                    async { enrichDiscoverResult(seerrRepo, item) }
                                } else {
                                    async { item }
                                }
                            }.awaitAll()
                    }
                _popularMoviesDiscover.value = enrichedMovies
            }
        } catch (e: Exception) {
            logger.error(e) { "Error fetching popular movies discover data" }
        }

        try {
            val tvRes = seerrRepo.client.getDiscoverTv(page = 1)
            if (tvRes is NetworkResult.Success) {
                val enrichedTv =
                    coroutineScope {
                        tvRes.data.results
                            .mapIndexed { index, item ->
                                if (index < 5) {
                                    async { enrichDiscoverResult(seerrRepo, item) }
                                } else {
                                    async { item }
                                }
                            }.awaitAll()
                    }
                _popularTvDiscover.value = enrichedTv
            }
        } catch (e: Exception) {
            logger.error(e) { "Error fetching popular tv discover data" }
        }

        val today =
            Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
                .toString()

        try {
            val upcomingMoviesRes = seerrRepo.client.getUpcomingMovies(page = 1, today = today)
            if (upcomingMoviesRes is NetworkResult.Success) {
                _upcomingMoviesDiscover.value = upcomingMoviesRes.data.results
            }
        } catch (e: Exception) {
            logger.error(e) { "Error fetching upcoming movies discover data" }
        }

        try {
            val upcomingTvRes = seerrRepo.client.getUpcomingTv(page = 1, today = today)
            if (upcomingTvRes is NetworkResult.Success) {
                _upcomingTvDiscover.value = upcomingTvRes.data.results
            }
        } catch (e: Exception) {
            logger.error(e) { "Error fetching upcoming tv discover data" }
        }

        if (_quickPickItem.value == null) {
            val pool =
                (_trendingDiscover.value + _popularMoviesDiscover.value + _popularTvDiscover.value)
                    .distinctBy { "${it.mediaType.name}_${it.id}" }
            if (pool.isNotEmpty()) {
                _quickPickItem.value = pool.random()
            }
        }
    }

    private fun resolveNetworkStatus(
        arrInstances: List<ArrInstanceDashboardState>,
        seerrInstances: List<SeerrDashboardState>,
        prowlarrInstances: List<ProwlarrDashboardState>,
        bazarrInstances: List<BazarrDashboardState>,
        downloadClients: List<DownloadClientDashboardState>,
        tracearrInstances: List<TracearrDashboardState> = emptyList(),
    ): NetworkStatusState {
        val networkUtils = getNetworkUtils()
        val currentSsid =
            try {
                networkUtils.getCurrentWifiSsid()
            } catch (_: Exception) {
                null
            }
        val isWifi =
            try {
                networkUtils.isConnectedToWifi()
            } catch (_: Exception) {
                false
            }

        val instanceStatuses = mutableListOf<InstanceNetworkStatus>()

        arrInstances.forEach { state ->
            instanceStatuses.add(
                InstanceNetworkStatus(
                    instanceName = state.instance.label,
                    isLocal = state.instance.isUsingLocalNetwork(),
                    currentEndpoint = state.instance.getEffectiveBaseUrl(),
                    icon = state.instance.type.icon,
                    isOnline = state.softwareStatus != null,
                    isLocalSwitchingEnabled = state.instance.localNetworkEnabled,
                ),
            )
        }

        seerrInstances.forEach { state ->
            instanceStatuses.add(
                InstanceNetworkStatus(
                    instanceName = state.instance.label,
                    isLocal = state.instance.isUsingLocalNetwork(),
                    currentEndpoint = state.instance.getEffectiveBaseUrl(),
                    icon = state.instance.type.icon,
                    isOnline = state.isOnline,
                    isLocalSwitchingEnabled = state.instance.localNetworkEnabled,
                ),
            )
        }

        prowlarrInstances.forEach { state ->
            instanceStatuses.add(
                InstanceNetworkStatus(
                    instanceName = state.instance.label,
                    isLocal = state.instance.isUsingLocalNetwork(),
                    currentEndpoint = state.instance.getEffectiveBaseUrl(),
                    icon = state.instance.type.icon,
                    isOnline = state.softwareStatus != null,
                    isLocalSwitchingEnabled = state.instance.localNetworkEnabled,
                ),
            )
        }

        bazarrInstances.forEach { state ->
            instanceStatuses.add(
                InstanceNetworkStatus(
                    instanceName = state.instance.label,
                    isLocal = state.instance.isUsingLocalNetwork(),
                    currentEndpoint = state.instance.getEffectiveBaseUrl(),
                    icon = state.instance.type.icon,
                    isOnline = state.isOnline,
                    isLocalSwitchingEnabled = state.instance.localNetworkEnabled,
                ),
            )
        }

        tracearrInstances.forEach { state ->
            instanceStatuses.add(
                InstanceNetworkStatus(
                    instanceName = state.instance.label,
                    isLocal = state.instance.isUsingLocalNetwork(),
                    currentEndpoint = state.instance.getEffectiveBaseUrl(),
                    icon = state.instance.type.icon,
                    isOnline = state.stats != null,
                    isLocalSwitchingEnabled = state.instance.localNetworkEnabled,
                ),
            )
        }

        downloadClients.forEach { state ->
            instanceStatuses.add(
                InstanceNetworkStatus(
                    instanceName = state.client.label,
                    isLocal = state.client.isUsingLocalNetwork(),
                    currentEndpoint = state.client.getEffectiveBaseUrl(),
                    icon = state.client.type.icon,
                    isOnline = state.isOnline,
                    isLocalSwitchingEnabled = state.client.localNetworkEnabled,
                ),
            )
        }

        return NetworkStatusState(
            ssid = currentSsid,
            isWifi = isWifi,
            instanceStatuses = instanceStatuses.sortedBy { it.instanceName },
        )
    }

    fun refresh() {
        if (_isRefreshing.value) return

        viewModelScope.launch {
            _isRefreshing.value = true
            val repos = instanceManager.getAllArrRepositories()
            repos.forEach { repo ->
                try {
                    repo.refreshInstanceStatuses()
                    repo.refreshActivityTasks()
                    if (repo.library.value == null) {
                        repo.refreshLibrary()
                    }
                } catch (e: Exception) {
                    logger.error(e) { "Error refreshing Arr instance ${repo.instance.label}" }
                }
            }

            val seerrRepos = instanceManager.getAllSeerrRepositories()
            seerrRepos.forEach { repo ->
                try {
                    repo.refreshCounts()
                } catch (e: Exception) {
                    logger.error(e) { "Error refreshing Seerr instance ${repo.instance.label}" }
                }
            }
            fetchDiscoverData()

            val prowlarrRepos =
                instanceManager.instanceRepositories.value.values
                    .filterIsInstance<ProwlarrInstanceRepository>()
            prowlarrRepos.forEach { repo ->
                try {
                    repo.refreshStatus()
                    repo.getIndexerStatus()
                    repo.getIndexers()
                } catch (e: Exception) {
                    logger.error(e) { "Error refreshing Prowlarr instance ${repo.instance.label}" }
                }
            }

            val bazarrRepos =
                instanceManager.instanceRepositories.value.values
                    .filterIsInstance<BazarrInstanceRepository>()
            bazarrRepos.forEach { repo ->
                try {
                    repo.getSystemStatus()
                    repo.refreshBadges()
                } catch (e: Exception) {
                    logger.error(e) { "Error refreshing Bazarr instance ${repo.instance.label}" }
                }
            }

            val tracearrRepos = instanceManager.getAllTracearrRepositories()
            tracearrRepos.forEach { repo ->
                try {
                    repo.refreshStats()
                    repo.refreshStreams()
                } catch (e: Exception) {
                    logger.error(e) { "Error refreshing Tracearr instance ${repo.instance.label}" }
                }
            }

            downloadQueueService.manualRefresh()

            calendarService.load()
            _isRefreshing.value = false
        }
    }

    fun toggleEditing() {
        _isEditing.update { !it }
    }

    fun resetCardsOrder() {
        dashboardManager.reset()
    }

    fun saveCardOrder(cards: List<DashboardCards>) {
        dashboardManager.saveCardOrder(cards)
    }

    fun removeCard(card: DashboardCards) {
        dashboardManager.removeCard(card)
    }

    fun addCard(card: DashboardCards) {
        dashboardManager.addCard(card)
    }

    fun approveRequest(
        requestId: Long,
        profileId: Long? = null,
        rootFolder: String? = null,
        languageProfileId: Long? = null,
        seasons: List<Int>? = null,
    ) {
        viewModelScope.launch {
            val seerrRepos = instanceManager.getAllSeerrRepositories()
            seerrRepos.forEach { repo ->
                repo.setRequestStatus(
                    requestId = requestId,
                    status = ApprovalStatus.Approve,
                    profileId = profileId,
                    rootFolder = rootFolder,
                    languageProfileId = languageProfileId,
                    seasons = seasons,
                )
            }
            refresh()
        }
    }

    fun declineRequest(requestId: Long) {
        viewModelScope.launch {
            val seerrRepos = instanceManager.getAllSeerrRepositories()
            seerrRepos.forEach { repo ->
                repo.setRequestStatus(
                    requestId = requestId,
                    status = ApprovalStatus.Decline,
                )
            }
            refresh()
        }
    }

    fun removeQueueItem(
        item: QueueItem,
        removeFromClient: Boolean,
        addToBlocklist: Boolean,
        skipRedownload: Boolean,
    ) {
        viewModelScope.launch {
            deleteQueueItemUseCase(item, removeFromClient, addToBlocklist, skipRedownload)
                .collect { status ->
                    _removeItemState.value = status
                    if (status is OperationStatus.Success) {
                        refresh()
                    }
                }
        }
    }

    fun resetRemoveItemState() {
        _removeItemState.value = OperationStatus.Idle
    }

    fun setFirstLaunchComplete() {
        preferencesStore.markDashboardAsSeen()
    }

    fun toggleDashboardSearch() {
        preferencesStore.setShowDashboardSearch(!showDashboardSearch.value)
    }
}
