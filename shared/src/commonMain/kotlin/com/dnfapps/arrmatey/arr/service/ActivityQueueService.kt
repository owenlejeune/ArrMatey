package com.dnfapps.arrmatey.arr.service

import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ActivityQueueService(
    private val instanceManager: InstanceManager,
    private val preferencesStore: PreferencesStore
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

    fun startPolling() {
        if (pollingJob?.isActive == true) return

        pollingJob = scope.launch {
            while (isActive) {
                pollActivityTasks()
                delay(pollingDelay)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private suspend fun pollActivityTasks() {
        if (preferencesStore.isPollingEnabled) {
            _isPolling.value = true
            val repositories = instanceManager.getAllArrRepositories()
                .filter { it.instance.type.supportsActivityQueue }

            val allTasks = repositories.map { repo ->
                scope.async {
                    repo.refreshActivityTasks()
                    repo.activityTasks.value
                }
            }.awaitAll().flatten()

            _allActivityTasks.value = allTasks

            val issueCount = allTasks.count { task -> task.hasIssue }
            _tasksWithIssues.value = issueCount
            if (repositories.isNotEmpty()) {
                _hasLoaded.value = true
            }
            _isPolling.value = false
        }
    }

    fun cleanup() {
        stopPolling()
        scope.cancel()
    }

    suspend fun manualRefresh() {
        pollActivityTasks()
    }
}