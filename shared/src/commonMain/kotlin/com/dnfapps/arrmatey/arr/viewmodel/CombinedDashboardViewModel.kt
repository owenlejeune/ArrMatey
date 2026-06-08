package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Arrtist
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.CalendarItem
import com.dnfapps.arrmatey.arr.service.CalendarService
import com.dnfapps.arrmatey.arr.state.ArrInstanceDashboardState
import com.dnfapps.arrmatey.arr.state.CombinedDashboardState
import com.dnfapps.arrmatey.arr.state.DownloadClientDashboardState
import com.dnfapps.arrmatey.arr.state.SeerrDashboardState
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.downloadclient.repository.DownloadClientManager
import com.dnfapps.arrmatey.downloadclient.service.DownloadQueueService
import com.dnfapps.arrmatey.downloadclient.state.DownloadQueueBundle
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.InstanceManager
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
import kotlinx.datetime.TimeZone
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
                        flowOf(emptyList<ArrInstanceDashboardState>())
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
                                val sizeOnDisk = libraryData.sumOf { item ->
                                    when (item) {
                                        is ArrSeries -> item.statistics?.sizeOnDisk ?: 0L
                                        is ArrMovie -> item.statistics?.sizeOnDisk ?: 0L
                                        is Arrtist -> item.statistics?.sizeOnDisk ?: 0L
                                        is Author -> item.statistics?.sizeOnDisk ?: 0L
                                        is Audiobook -> item.remoteFileSize ?: 0L
                                        else -> 0L
                                    }
                                }

                                ArrInstanceDashboardState(
                                    instance = repo.instance,
                                    softwareStatus = software,
                                    disks = disks,
                                    healthItems = health,
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
                        flowOf(emptyList<SeerrDashboardState>())
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
                downloadQueueService.allTransfers,
                downloadClientManager.downloadClientApis,
                calendarService.items.map { itemsByDate ->
                    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                    itemsByDate[today] ?: emptyList()
                },
                _isRefreshing
            ) { args ->
                val instances = args[0] as List<ArrInstanceDashboardState>
                val seerrInstances = args[1] as List<SeerrDashboardState>
                val downloads = args[2] as DownloadQueueBundle
                val clientApis = args[3] as Map<Long, *>
                val todayCalendar = args[4] as List<CalendarItem>
                val refreshing = args[5] as Boolean

                val downloadClients = downloads.transferInfo.map { transfer ->
                    DownloadClientDashboardState(
                        client = transfer.client,
                        transferInfo = transfer,
                        isOnline = true,
                        activeDownloadsCount = downloads.queueItems.count { it.client.id == transfer.client.id }
                    )
                }.toMutableList()

                // Add clients that might be offline (not in transferInfo)
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

                CombinedDashboardState.Success(
                    instances = instances,
                    seerrInstances = seerrInstances,
                    downloadClients = downloadClients,
                    downloadTransfers = downloads.transferInfo,
                    activeDownloads = downloads.queueItems.sortedByDescending { it.progress },
                    calendarItems = todayCalendar,
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

            downloadQueueService.manualRefresh()

            calendarService.load()
            _isRefreshing.value = false
        }
    }
}
