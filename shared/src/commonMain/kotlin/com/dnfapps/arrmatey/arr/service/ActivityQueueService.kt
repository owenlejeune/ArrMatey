package com.dnfapps.arrmatey.arr.service

import com.dnfapps.arrmatey.arr.api.model.BlocklistItem
import com.dnfapps.arrmatey.arr.api.model.HistoryItem
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.api.model.WantedMediaItem
import com.dnfapps.arrmatey.arr.api.model.groupByTask
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class ActivityQueueService(
    private val instanceManager: InstanceManager,
) {
    private val pollingDelay = 30_000L

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var pollingJob: Job? = null
    private var refreshJob: Job? = null

    init {
        scope.launch {
            instanceManager.instanceRepositories
                .filter { it.values.any { repo -> repo is ArrInstanceRepository && repo.instance.type.supportsActivityQueue } }
                .collect {
                    manualRefresh()
                }
        }
    }

    private val _isPolling = MutableStateFlow(false)
    val isPolling: StateFlow<Boolean> = _isPolling

    private val _hasLoaded = MutableStateFlow(false)
    val hasLoaded: StateFlow<Boolean> = _hasLoaded.asStateFlow()

    private val _allActivityTasks = MutableStateFlow<List<QueueItem>>(emptyList())
    val allActivityTasks: StateFlow<List<QueueItem>> = _allActivityTasks.asStateFlow()

    private val _tasksWithIssues = MutableStateFlow(0)
    val tasksWithIssues: StateFlow<Int> = _tasksWithIssues.asStateFlow()

    private val _allHistory = MutableStateFlow<List<HistoryItem>>(emptyList())
    val allHistory: StateFlow<List<HistoryItem>> = _allHistory.asStateFlow()

    private val _isHistoryLoading = MutableStateFlow(false)
    val isHistoryLoading: StateFlow<Boolean> = _isHistoryLoading.asStateFlow()

    private val _hasHistoryLoaded = MutableStateFlow(false)
    val hasHistoryLoaded: StateFlow<Boolean> = _hasHistoryLoaded.asStateFlow()

    private val _allBlocklist = MutableStateFlow<List<BlocklistItem>>(emptyList())
    val allBlocklist: StateFlow<List<BlocklistItem>> = _allBlocklist.asStateFlow()

    private val _isBlocklistLoading = MutableStateFlow(false)
    val isBlocklistLoading: StateFlow<Boolean> = _isBlocklistLoading.asStateFlow()

    private val _hasBlocklistLoaded = MutableStateFlow(false)
    val hasBlocklistLoaded: StateFlow<Boolean> = _hasBlocklistLoaded.asStateFlow()

    private val _allMissing = MutableStateFlow<List<WantedMediaItem>>(emptyList())
    val allMissing: StateFlow<List<WantedMediaItem>> = _allMissing.asStateFlow()

    private val _isMissingLoading = MutableStateFlow(false)
    val isMissingLoading: StateFlow<Boolean> = _isMissingLoading.asStateFlow()

    private val _hasMissingLoaded = MutableStateFlow(false)
    val hasMissingLoaded: StateFlow<Boolean> = _hasMissingLoaded.asStateFlow()

    private val _allCutoff = MutableStateFlow<List<WantedMediaItem>>(emptyList())
    val allCutoff: StateFlow<List<WantedMediaItem>> = _allCutoff.asStateFlow()

    private val _isCutoffLoading = MutableStateFlow(false)
    val isCutoffLoading: StateFlow<Boolean> = _isCutoffLoading.asStateFlow()

    private val _hasCutoffLoaded = MutableStateFlow(false)
    val hasCutoffLoaded: StateFlow<Boolean> = _hasCutoffLoaded.asStateFlow()

    fun startPolling() {
        if (pollingJob?.isActive == true) return

        pollingJob =
            scope.launch {
                while (isActive) {
                    try {
                        pollActivityTasks()
                    } catch (_: Exception) {
                    }
                    delay(pollingDelay.milliseconds)
                }
            }
    }

    suspend fun refreshActivity() {
        pollActivityTasks()
    }

    suspend fun refreshHistory() {
        pollHistory()
    }

    suspend fun refreshBlocklist() {
        pollBlocklist()
    }

    suspend fun refreshMissing() {
        pollMissing()
    }

    suspend fun refreshCutoff() {
        pollCutoff()
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun removeTaskLocally(taskId: Int, instanceId: Long? = null) {
        _allActivityTasks.value = _allActivityTasks.value.filter {
            if (instanceId != null) {
                !(it.id == taskId && it.instanceId == instanceId)
            } else {
                it.id != taskId
            }
        }
        val issueCount = _allActivityTasks.value.groupByTask().count { task -> task.hasIssue }
        _tasksWithIssues.value = issueCount
    }

    fun removeTasksLocally(items: Collection<QueueItem>) {
        val toRemove = items.map { (it.instanceId ?: 0L) to it.id }.toSet()
        _allActivityTasks.value = _allActivityTasks.value.filter {
            ((it.instanceId ?: 0L) to it.id) !in toRemove
        }
        val issueCount = _allActivityTasks.value.groupByTask().count { task -> task.hasIssue }
        _tasksWithIssues.value = issueCount
    }

    private suspend fun pollActivityTasks() {
        _isPolling.value = true
        try {
            val repositories =
                instanceManager
                    .getAllArrRepositories()
                    .filter { it.instance.type.supportsActivityQueue }

            val allTasks =
                coroutineScope {
                    repositories
                        .map { repo ->
                            async {
                                try {
                                    repo.refreshActivityTasks()
                                    repo.activityTasks.value
                                } catch (_: Exception) {
                                    repo.activityTasks.value
                                }
                            }
                        }.awaitAll()
                        .flatten()
                }

            _allActivityTasks.value = allTasks

            val issueCount = allTasks.groupByTask().count { task -> task.hasIssue }
            _tasksWithIssues.value = issueCount
            if (repositories.isNotEmpty()) {
                _hasLoaded.value = true
            }
        } finally {
            _isPolling.value = false
        }
    }

    private suspend fun pollHistory() {
        _isHistoryLoading.value = true
        try {
            val repositories =
                instanceManager
                    .getAllArrRepositories()
                    .filter { it.instance.type.supportsActivityQueue }

            val allHistoryList =
                coroutineScope {
                    repositories
                        .map { repo ->
                            async {
                                try {
                                    repo.refreshHistory()
                                    repo.history.value
                                } catch (_: Exception) {
                                    repo.history.value
                                }
                            }
                        }.awaitAll()
                        .flatten()
                        .sortedByDescending { it.date }
                }

            _allHistory.value = allHistoryList
            if (repositories.isNotEmpty()) {
                _hasHistoryLoaded.value = true
            }
        } finally {
            _isHistoryLoading.value = false
        }
    }

    fun cleanup() {
        stopPolling()
        scope.cancel()
    }

    private suspend fun pollBlocklist() {
        _isBlocklistLoading.value = true
        try {
            val repositories =
                instanceManager
                    .getAllArrRepositories()
                    .filter { it.instance.type.supportsActivityQueue }

            val allBlocklistList =
                coroutineScope {
                    repositories
                        .map { repo ->
                            async {
                                try {
                                    repo.refreshBlocklist()
                                    repo.blocklist.value
                                } catch (_: Exception) {
                                    repo.blocklist.value
                                }
                            }
                        }.awaitAll()
                        .flatten()
                        .sortedByDescending { it.date }
                }

            _allBlocklist.value = allBlocklistList
            if (repositories.isNotEmpty()) {
                _hasBlocklistLoaded.value = true
            }
        } finally {
            _isBlocklistLoading.value = false
        }
    }

    suspend fun manualRefresh() {
        val existing = refreshJob
        if (existing != null && existing.isActive) {
            existing.join()
            return
        }
        coroutineScope {
            val job = launch {
                coroutineScope {
                    launch { pollActivityTasks() }
                    launch { pollHistory() }
                    launch { pollBlocklist() }
                    launch { pollMissing() }
                    launch { pollCutoff() }
                }
            }
            refreshJob = job
            job.join()
        }
    }

    private suspend fun pollMissing() {
        _isMissingLoading.value = true
        try {
            val repositories =
                instanceManager
                    .getAllArrRepositories()
                    .filter { it.instance.type.supportsActivityQueue }

            val list =
                coroutineScope {
                    repositories
                        .map { repo ->
                            async {
                                try {
                                    repo.refreshMissing()
                                    repo.missing.value
                                } catch (_: Exception) {
                                    repo.missing.value
                                }
                            }
                        }.awaitAll()
                        .flatten()
                }

            _allMissing.value = list
            if (repositories.isNotEmpty()) {
                _hasMissingLoaded.value = true
            }
        } finally {
            _isMissingLoading.value = false
        }
    }

    private suspend fun pollCutoff() {
        _isCutoffLoading.value = true
        try {
            val repositories =
                instanceManager
                    .getAllArrRepositories()
                    .filter { it.instance.type.supportsActivityQueue }

            val list =
                coroutineScope {
                    repositories
                        .map { repo ->
                            async {
                                try {
                                    repo.refreshCutoff()
                                    repo.cutoffUnmet.value
                                } catch (_: Exception) {
                                    repo.cutoffUnmet.value
                                }
                            }
                        }.awaitAll()
                        .flatten()
                }

            _allCutoff.value = list
            if (repositories.isNotEmpty()) {
                _hasCutoffLoaded.value = true
            }
        } finally {
            _isCutoffLoading.value = false
        }
    }
}
