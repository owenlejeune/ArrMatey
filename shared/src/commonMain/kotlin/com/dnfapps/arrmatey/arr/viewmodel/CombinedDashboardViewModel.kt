package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.CalendarItem
import com.dnfapps.arrmatey.arr.service.CalendarService
import com.dnfapps.arrmatey.arr.state.ArrInstanceDashboardState
import com.dnfapps.arrmatey.arr.state.CombinedDashboardState
import com.dnfapps.arrmatey.arr.state.DownloadClientDashboardState
import com.dnfapps.arrmatey.arr.state.InstanceNetworkStatus
import com.dnfapps.arrmatey.arr.state.NetworkStatusState
import com.dnfapps.arrmatey.arr.state.ProwlarrDashboardState
import com.dnfapps.arrmatey.arr.state.SeerrDashboardState
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.compose.DashboardCards
import com.dnfapps.arrmatey.compose.DashboardManager
import com.dnfapps.arrmatey.downloadclient.repository.DownloadClientManager
import com.dnfapps.arrmatey.downloadclient.service.DownloadQueueService
import com.dnfapps.arrmatey.downloadclient.state.DownloadQueueBundle
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.instances.repository.ProwlarrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.utils.getNetworkUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class CombinedDashboardViewModel(
    private val instanceManager: InstanceManager,
    private val downloadClientManager: DownloadClientManager,
    private val downloadQueueService: DownloadQueueService,
    private val calendarService: CalendarService,
    private val dashboardManager: DashboardManager
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _state = MutableStateFlow<CombinedDashboardState>(CombinedDashboardState.Initial)
    val state: StateFlow<CombinedDashboardState> = _state.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    val cards = dashboardManager.cardsOrder
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        observeDashboard()
        refresh()
    }

    private fun observeDashboard() {
        viewModelScope.launch {
            combine(
                instanceManager.instanceRepositories.flatMapLatest { repoMap ->
                    val arrRepos = repoMap.values.filterIsInstance<ArrInstanceRepository>()
                    if (arrRepos.isEmpty()) {
                        flowOf(emptyList())
                    } else {
                        val flows = arrRepos.map { repo ->
                            combine(
                                repo.softwareStatus,
                                repo.diskSpace,
                                repo.health,
                                repo.activityTasks,
                                repo.library
                            ) { software, disks, health, activity, library ->
                                val libraryData = (library as? NetworkResult.Success)?.data ?: emptyList()
                                val totalItems = libraryData.size
                                val sizeOnDisk = libraryData.sumOf { it.fileSize }

                                ArrInstanceDashboardState(
                                    instance = repo.instance,
                                    softwareStatus = software,
                                    disks = disks,
                                    healthItems = health,
                                    library = libraryData,
                                    activityTasks = activity,
                                    activeCount = activity.size,
                                    totalItems = totalItems,
                                    sizeOnDisk = sizeOnDisk
                                )
                            }
                        }
                        combine(flows) { it.toList() }
                    }
                },
                instanceManager.instanceRepositories.flatMapLatest { repoMap ->
                    val seerrRepos = repoMap.values.filterIsInstance<SeerrInstanceRepository>()
                    if (seerrRepos.isEmpty()) {
                        flowOf(emptyList())
                    } else {
                        val flows = seerrRepos.map { repo ->
                            combine(
                                repo.pendingRequestsCount,
                                repo.openIssuesCount
                            ) { pending, issues ->
                                SeerrDashboardState(
                                    instance = repo.instance,
                                    pendingRequestsCount = pending,
                                    openIssuesCount = issues
                                )
                            }
                        }
                        combine(flows) { it.toList() }
                    }
                },
                instanceManager.instanceRepositories.flatMapLatest { repoMap ->
                    val prowlarrRepos = repoMap.values.filterIsInstance<ProwlarrInstanceRepository>()
                    if (prowlarrRepos.isEmpty()) {
                        flowOf(emptyList())
                    } else {
                        val flows = prowlarrRepos.map { repo ->
                            combine(repo.indexerStatus, repo.indexers) { status, indexers ->
                                val failureCount = status.count { it.hasFailure }
                                ProwlarrDashboardState(
                                    instance = repo.instance,
                                    totalIndexers = status.size,
                                    healthyIndexers = indexers.size - failureCount,
                                    failingIndexers = failureCount
                                )
                            }
                        }
                        combine(flows) { it.toList() }
                    }
                },
                downloadQueueService.allTransfers,
                downloadClientManager.downloadClientApis,
                calendarService.items.map { itemsByDate ->
                    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                    val todayItems = itemsByDate[today] ?: emptyList()
                    val upcomingItems = (1..7).flatMap {
                        itemsByDate[today.plus(it, DateTimeUnit.DAY)] ?: emptyList()
                    }
                    todayItems to upcomingItems
                },
                _isRefreshing
            ) { args ->
                @Suppress("UNCHECKED_CAST")
                val instances = args[0] as List<ArrInstanceDashboardState>
                @Suppress("UNCHECKED_CAST")
                val seerrInstances = args[1] as List<SeerrDashboardState>
                @Suppress("UNCHECKED_CAST")
                val prowlarrStats = args[2] as List<ProwlarrDashboardState>
                val downloads = args[3] as DownloadQueueBundle
                @Suppress("UNCHECKED_CAST")
                val clientApis = args[4] as Map<Long, *>
                @Suppress("UNCHECKED_CAST")
                val calendarPair = args[5] as Pair<List<CalendarItem>, List<CalendarItem>>
                val todayCalendar = calendarPair.first
                val upcomingCalendar = calendarPair.second
                val refreshing = args[6] as Boolean

                val downloadClients = downloads.transferInfo.map { transfer ->
                    val clientItems = downloads.queueItems.filter { it.client.id == transfer.client.id }
                    DownloadClientDashboardState(
                        client = transfer.client,
                        transferInfo = transfer,
                        isOnline = true,
                        activeDownloadsCount = clientItems.count { it.downloadSpeed > 0 || it.uploadSpeed > 0 || it.progress < 1.0 }
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
                                    activeDownloadsCount = clientItems.count { it.downloadSpeed > 0 || it.uploadSpeed > 0 || it.progress < 1.0 }
                                )
                            )
                        }
                    }
                }

                val recentActivity = instances.flatMap { it.activityTasks }
                    .sortedByDescending { it.added }

                val recentlyAdded = instances.flatMap { it.library }
                    .filter { it.added != null }
                    .sortedByDescending { it.added }
                    .take(10)

                val activeDownloads = downloads.queueItems
                    .sortedByDescending { it.progress }

                CombinedDashboardState.Success(
                    instances = instances,
                    seerrInstances = seerrInstances,
                    downloadClients = downloadClients,
                    activityQueue = recentActivity,
                    recentlyAdded = recentlyAdded,
                    downloadTransfers = downloads.transferInfo,
                    activeDownloads = activeDownloads,
                    calendarItems = todayCalendar,
                    upcomingCalendarItems = upcomingCalendar,
                    prowlarrStats = prowlarrStats,
                    networkStatus = resolveNetworkStatus(instances, seerrInstances, prowlarrStats, downloadClients),
                    isRefreshing = refreshing
                )
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    private fun resolveNetworkStatus(
        arrInstances: List<ArrInstanceDashboardState>,
        seerrInstances: List<SeerrDashboardState>,
        prowlarrInstances: List<ProwlarrDashboardState>,
        downloadClients: List<DownloadClientDashboardState>
    ): NetworkStatusState {
        val networkUtils = getNetworkUtils()
        val currentSsid = try { networkUtils.getCurrentWifiSsid() } catch (e: Exception) { null }
        val isWifi = try { networkUtils.isConnectedToWifi() } catch (e: Exception) { false }
        
        val instanceStatuses = mutableListOf<InstanceNetworkStatus>()
        
        arrInstances.forEach { state ->
            instanceStatuses.add(
                InstanceNetworkStatus(
                    instanceName = state.instance.label,
                    isLocal = state.instance.isUsingLocalNetwork(),
                    currentEndpoint = state.instance.getEffectiveBaseUrl(),
                    icon = state.instance.type.icon,
                    isOnline = state.softwareStatus != null,
                    isLocalSwitchingEnabled = state.instance.localNetworkEnabled
                )
            )
        }
        
        seerrInstances.forEach { state ->
            instanceStatuses.add(
                InstanceNetworkStatus(
                    instanceName = state.instance.label,
                    isLocal = state.instance.isUsingLocalNetwork(),
                    currentEndpoint = state.instance.getEffectiveBaseUrl(),
                    icon = state.instance.type.icon,
                    isOnline = true, // Assume online if we have state
                    isLocalSwitchingEnabled = state.instance.localNetworkEnabled
                )
            )
        }
        
        prowlarrInstances.forEach { state ->
            instanceStatuses.add(
                InstanceNetworkStatus(
                    instanceName = state.instance.label,
                    isLocal = state.instance.isUsingLocalNetwork(),
                    currentEndpoint = state.instance.getEffectiveBaseUrl(),
                    icon = state.instance.type.icon,
                    isOnline = state.totalIndexers > 0 || state.failingIndexers > 0,
                    isLocalSwitchingEnabled = state.instance.localNetworkEnabled
                )
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
                    isLocalSwitchingEnabled = state.client.localNetworkEnabled
                )
            )
        }

        return NetworkStatusState(
            ssid = currentSsid,
            isWifi = isWifi,
            instanceStatuses = instanceStatuses.sortedBy { it.instanceName }
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
                    // Log error but continue with other instances
                }
            }

            val seerrRepos = instanceManager.getAllSeerrRepositories()
            seerrRepos.forEach { repo ->
                try {
                    repo.refreshCounts()
                } catch (e: Exception) {
                    // Log error
                }
            }

            val prowlarrRepos = instanceManager.instanceRepositories.value.values.filterIsInstance<ProwlarrInstanceRepository>()
            prowlarrRepos.forEach { repo ->
                try {
                    repo.getIndexerStatus()
                    repo.getIndexers()
                } catch (e: Exception) {
                    // Log error
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
}
