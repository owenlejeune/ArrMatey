package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.CalendarItem
import com.dnfapps.arrmatey.arr.service.CalendarService
import com.dnfapps.arrmatey.arr.state.ArrInstanceDashboardState
import com.dnfapps.arrmatey.arr.state.CombinedDashboardState
import com.dnfapps.arrmatey.arr.state.DownloadClientDashboardState
import com.dnfapps.arrmatey.arr.state.ProwlarrDashboardState
import com.dnfapps.arrmatey.arr.state.SeerrDashboardState
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.downloadclient.repository.DownloadClientManager
import com.dnfapps.arrmatey.downloadclient.service.DownloadQueueService
import com.dnfapps.arrmatey.downloadclient.state.DownloadQueueBundle
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.instances.repository.ProwlarrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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
    private val calendarService: CalendarService
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _state = MutableStateFlow<CombinedDashboardState>(CombinedDashboardState.Initial)
    val state: StateFlow<CombinedDashboardState> = _state.asStateFlow()

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
                                val failingIndexerIds = status
                                    .filter { it.hasFailure }
                                    .map { it.indexerId }
                                    .toSet()
                                val failingNames = indexers
                                    .filter { it.id in failingIndexerIds }
                                    .mapNotNull { it.name }
                                val failureCount = status.count { it.hasFailure }
                                ProwlarrDashboardState(
                                    instance = repo.instance,
                                    totalIndexers = status.size,
                                    healthyIndexers = indexers.size - failureCount,
                                    failingIndexers = failureCount,
                                    failingIndexerNames = failingNames
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
                    DownloadClientDashboardState(
                        client = transfer.client,
                        transferInfo = transfer,
                        isOnline = true,
                        activeDownloadsCount = downloads.queueItems.count { it.client.id == transfer.client.id }
                    )
                }.toMutableList()

                clientApis.keys.forEach { clientId ->
                    if (downloadClients.none { it.client.id == clientId }) {
                        downloadClientManager.getDownloadClientById(clientId)?.let { client ->
                            downloadClients.add(
                                DownloadClientDashboardState(
                                    client = client,
                                    isOnline = false,
                                    activeDownloadsCount = downloads.queueItems.count { it.client.id == clientId }
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

                CombinedDashboardState.Success(
                    instances = instances,
                    seerrInstances = seerrInstances,
                    downloadClients = downloadClients,
                    recentActivity = recentActivity,
                    recentlyAdded = recentlyAdded,
                    downloadTransfers = downloads.transferInfo,
                    activeDownloads = downloads.queueItems.sortedByDescending { it.progress },
                    calendarItems = todayCalendar,
                    upcomingCalendarItems = upcomingCalendar,
                    prowlarrStats = prowlarrStats,
                    isRefreshing = refreshing
                )
            }.collect { newState ->
                _state.value = newState
            }
        }
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
}
