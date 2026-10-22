package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.IndexerHealthStatus
import com.dnfapps.arrmatey.arr.state.ArrDashboardState
import com.dnfapps.arrmatey.arr.state.BazarrOverview
import com.dnfapps.arrmatey.arr.state.HttpErrorType
import com.dnfapps.arrmatey.arr.state.ProwlarrOverview
import com.dnfapps.arrmatey.arr.state.SeerrOverview
import com.dnfapps.arrmatey.arr.state.TautulliOverview
import com.dnfapps.arrmatey.arr.state.TracearrOverview
import com.dnfapps.arrmatey.arr.usecase.ExecuteArrCommandUseCase
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.UpdateInfo
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.BazarrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.instances.repository.InstanceScopedRepository
import com.dnfapps.arrmatey.instances.repository.PlaybackMonitoringRepository
import com.dnfapps.arrmatey.instances.repository.ProwlarrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.TautulliRepository
import com.dnfapps.arrmatey.instances.repository.TracearrRepository
import com.dnfapps.arrmatey.instances.usecase.DeleteInstanceUseCase
import com.dnfapps.arrmatey.instances.usecase.GetArrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.notifications.NotificationManager
import com.dnfapps.arrmatey.seerr.api.model.IssueState
import com.dnfapps.arrmatey.seerr.api.model.RequestState
import com.dnfapps.networking.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ArrInstanceDashboardViewModel(
    private val instanceId: Long,
    private val getArrInstanceRepositoryUseCase: GetArrInstanceRepositoryUseCase,
    private val executeArrCommandUseCase: ExecuteArrCommandUseCase,
    private val deleteInstanceUseCase: DeleteInstanceUseCase,
    private val notificationManager: NotificationManager,
    private val instanceManager: InstanceManager,
) : ViewModel() {
    private val _state = MutableStateFlow<ArrDashboardState>(ArrDashboardState.Initial)
    val state: StateFlow<ArrDashboardState> = _state.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _instance = MutableStateFlow<Instance?>(null)
    val instance: StateFlow<Instance?> = _instance.asStateFlow()

    private val _lastCommandResult = MutableStateFlow<Boolean?>(null)
    val lastCommandResult: StateFlow<Boolean?> = _lastCommandResult.asStateFlow()

    private var scopedRepository: InstanceScopedRepository? = null

    init {
        loadInstanceAndObserve()
    }

    private fun loadInstanceAndObserve() {
        viewModelScope.launch {
            _state.value = ArrDashboardState.Loading

            val repo = instanceManager.getOrCreateRepository(instanceId)
            scopedRepository = repo
            if (repo == null) {
                _state.value =
                    ArrDashboardState.Error(
                        type = HttpErrorType.Unexpected,
                        message = "Could not connect to instance repository",
                    )
                return@launch
            }
            _instance.value = repo.instance

            refresh()

            when (repo) {
                is ArrInstanceRepository -> {
                    combine(
                        repo.softwareStatus,
                        repo.diskSpace,
                        repo.health,
                        repo.updates,
                    ) { software, disks, health, updates ->
                        val latestUpdate = updates.firstOrNull { it.latest } ?: updates.firstOrNull { !it.installed }
                        val isUpdate = latestUpdate != null && !latestUpdate.installed
                        val updateInfo = if (isUpdate) {
                            UpdateInfo(
                                isAvailable = true,
                                currentVersion = software?.version,
                                latestVersion = latestUpdate.version,
                                releaseUrl = latestUpdate.url,
                            )
                        } else {
                            null
                        }

                        ArrDashboardState.Success(
                            softwareStatus = software,
                            disks = disks,
                            healthItems = health,
                            updateInfo = updateInfo,
                        )
                    }.collect { newState ->
                        _state.value = newState
                    }
                }
                is ProwlarrInstanceRepository -> {
                    val indexersFlow = combine(repo.indexers, repo.indexerStatus) { idxs, status -> idxs to status }
                    combine(
                        indexersFlow,
                        repo.softwareStatus,
                        repo.diskSpace,
                        repo.health,
                        repo.updates,
                    ) { (indexers, indexerStatus), software, disks, health, updates ->
                        val failingCount = indexerStatus.count { it.health == IndexerHealthStatus.Failing }
                        val healthyCount = indexers.count { indexer ->
                            val status = indexerStatus.firstOrNull { it.indexerId == indexer.id }
                            status == null || status.health == IndexerHealthStatus.Healthy
                        }
                        val latestUpdate = updates.firstOrNull { it.latest } ?: updates.firstOrNull { !it.installed }
                        val isUpdate = latestUpdate != null && !latestUpdate.installed
                        val updateInfo = if (isUpdate) {
                            UpdateInfo(
                                isAvailable = true,
                                currentVersion = software?.version,
                                latestVersion = latestUpdate.version,
                                releaseUrl = latestUpdate.url,
                            )
                        } else {
                            null
                        }

                        ArrDashboardState.Success(
                            softwareStatus = software,
                            disks = disks,
                            healthItems = health,
                            prowlarrOverview = ProwlarrOverview(
                                totalIndexers = indexers.size,
                                healthyIndexers = healthyCount,
                                failingIndexers = failingCount,
                            ),
                            updateInfo = updateInfo,
                        )
                    }.collect { newState ->
                        _state.value = newState
                    }
                }
                is BazarrInstanceRepository -> {
                    combine(
                        repo.systemStatus,
                        repo.wantedEpisodesCount,
                        repo.wantedMoviesCount,
                        repo.providerIssuesCount,
                    ) { status, wantedEpisodes, wantedMovies, providerIssues ->
                        val data = status?.data
                        val overview = data?.let {
                            BazarrOverview(
                                version = it.bazarr_version,
                                packageVersion = it.package_version,
                                operatingSystem = it.operating_system,
                                pythonVersion = it.python_version,
                                bazarrDirectory = it.bazarr_directory,
                                configDirectory = it.bazarr_config_directory,
                                cpuCores = it.cpu_cores,
                                timezone = it.timezone,
                                wantedEpisodes = wantedEpisodes,
                                wantedMovies = wantedMovies,
                                providerIssues = providerIssues,
                            )
                        }
                        ArrDashboardState.Success(
                            bazarrOverview = overview,
                        )
                    }.collect { newState ->
                        _state.value = newState
                    }
                }
                is TautulliRepository -> {
                    combine(
                        repo.serverStatus,
                        repo.activity,
                        repo.updateAvailable,
                    ) { serverStatus, act, updateAvailable ->
                        val overview = serverStatus?.let {
                            TautulliOverview(
                                tautulliVersion = it.tautulliVersion,
                                installType = it.tautulliInstallType,
                                pmsName = it.pmsName,
                                pmsVersion = it.pmsVersion,
                                pmsUrl = it.pmsUrl,
                                pmsIdentifier = it.pmsIdentifier,
                                streamCount = act?.streamCountInt ?: 0,
                                totalBandwidth = act?.totalBandwidth,
                                lanBandwidth = act?.lanBandwidth,
                                wanBandwidth = act?.wanBandwidth,
                                updateAvailable = updateAvailable,
                            )
                        }
                        val updateInfo = if (updateAvailable) {
                            UpdateInfo(
                                isAvailable = true,
                                currentVersion = serverStatus?.tautulliVersion,
                                latestVersion = null,
                                releaseUrl = null,
                            )
                        } else {
                            null
                        }

                        ArrDashboardState.Success(
                            tautulliOverview = overview,
                            updateInfo = updateInfo,
                        )
                    }.collect { newState ->
                        _state.value = newState
                    }
                }
                is TracearrRepository -> {
                    combine(
                        repo.servers,
                        repo.todayStats,
                        repo.activeStreams,
                        repo.version,
                    ) { servers, today, streams, ver ->
                        val overview = TracearrOverview(
                            version = ver?.current?.version,
                            commitTag = ver?.current?.commit?.take(7) ?: ver?.current?.tag,
                            updateAvailable = ver?.updateAvailable ?: false,
                            servers = servers,
                            todayStats = today,
                            activeStreams = streams,
                        )
                        val updateInfo = if (ver?.updateAvailable == true) {
                            UpdateInfo(
                                isAvailable = true,
                                currentVersion = ver.current?.version,
                                latestVersion = ver.latest?.version,
                                releaseUrl = ver.latest?.releaseUrl,
                            )
                        } else {
                            null
                        }

                        ArrDashboardState.Success(
                            tracearrOverview = overview,
                            updateInfo = updateInfo,
                        )
                    }.collect { newState ->
                        _state.value = newState
                    }
                }
                is SeerrInstanceRepository -> {
                    combine(
                        repo.status,
                        repo.requests,
                        repo.openIssues,
                    ) { status, reqs, issues ->
                        val overview = SeerrOverview(
                            version = status?.version,
                            commitTag = status?.commitTag,
                            updateAvailable = status?.updateAvailable ?: false,
                            totalRequests = reqs.size,
                            pendingRequests = reqs.count { it.request.matchesFilter(RequestState.Pending) },
                            approvedRequests = reqs.count { it.request.matchesFilter(RequestState.Approved) },
                            processingRequests = reqs.count { it.request.matchesFilter(RequestState.Processing) },
                            availableRequests = reqs.count { it.request.matchesFilter(RequestState.Available) },
                            totalIssues = issues.size,
                            openIssues = issues.count { it.issue.matchesFilter(IssueState.Open) },
                        )
                        val updateInfo = if (status?.updateAvailable == true) {
                            UpdateInfo(
                                isAvailable = true,
                                currentVersion = status.version,
                                latestVersion = null,
                                releaseUrl = null,
                            )
                        } else {
                            null
                        }

                        ArrDashboardState.Success(
                            seerrOverview = overview,
                            updateInfo = updateInfo,
                        )
                    }.collect { newState ->
                        _state.value = newState
                    }
                }
                is PlaybackMonitoringRepository -> Unit
            }
        }
    }

    fun refresh() {
        if (_isRefreshing.value) return

        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                when (val repo = scopedRepository) {
                    is ArrInstanceRepository -> repo.refreshInstanceStatuses()
                    is ProwlarrInstanceRepository -> repo.refreshInstanceStatuses()
                    is BazarrInstanceRepository -> {
                        repo.getSystemStatus()
                        repo.refreshBadges()
                    }
                    is TautulliRepository -> {
                        repo.getServerStatus()
                        repo.refreshStreams()
                        repo.checkUpdate()
                    }
                    is TracearrRepository -> {
                        repo.getVersion()
                        repo.refreshServers()
                        repo.getTodayStats()
                        repo.getPublicStreams()
                    }
                    is SeerrInstanceRepository -> {
                        repo.refreshStatus()
                        repo.refreshCounts()
                    }
                    is PlaybackMonitoringRepository -> Unit
                    null -> Unit
                }
            } catch (e: Exception) {
                if (_state.value is ArrDashboardState.Initial) {
                    _state.value = ArrDashboardState.Error(HttpErrorType.Unexpected, e.message)
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun runRssSync() {
        viewModelScope.launch {
            val result = executeArrCommandUseCase.runRssSync(instanceId)
            _lastCommandResult.value = result is NetworkResult.Success
        }
    }

    fun searchAllMissing() {
        viewModelScope.launch {
            val result = executeArrCommandUseCase.searchAllMissing(instanceId)
            _lastCommandResult.value = result is NetworkResult.Success
        }
    }

    fun updateLibrary() {
        viewModelScope.launch {
            val result = executeArrCommandUseCase.updateLibrary(instanceId)
            _lastCommandResult.value = result is NetworkResult.Success
        }
    }

    fun backupDatabase() {
        viewModelScope.launch {
            val result = executeArrCommandUseCase.backupDatabase(instanceId)
            _lastCommandResult.value = result is NetworkResult.Success
        }
    }

    fun deleteInstance() {
        viewModelScope.launch {
            _instance.value?.let { inst ->
                if (inst.notificationsEnabled) {
                    notificationManager.cancelNotificationsForInstance(inst.label)
                }
                deleteInstanceUseCase(inst)
            }
        }
    }
}
