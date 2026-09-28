package com.dnfapps.arrmatey.arr.service

import com.dnfapps.arrmatey.arr.api.model.HistoryItem
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.api.model.groupByTask
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ActivityQueueService(
    private val instanceManager: InstanceManager,
) {
    private val pollingDelay = 30_000L

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var pollingJob: Job? = null

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

    fun startPolling() {
        if (pollingJob?.isActive == true) return

        pollingJob =
            scope.launch {
                while (isActive) {
                    try {
                        pollActivityTasks()
                        pollHistory()
                    } catch (_: Exception) {
                    }
                    delay(pollingDelay)
                }
            }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun removeTaskLocally(taskId: Int) {
        _allActivityTasks.value = _allActivityTasks.value.filter { it.id != taskId }
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

    suspend fun manualRefresh() {
        pollActivityTasks()
        pollHistory()
    }
}
