package com.dnfapps.arrmatey.downloadclient.service

import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.model.DownloadItem
import com.dnfapps.arrmatey.downloadclient.model.DownloadTransferInfo
import com.dnfapps.arrmatey.downloadclient.repository.DownloadClientManager
import com.dnfapps.arrmatey.downloadclient.state.DownloadQueueBundle
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
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
import kotlinx.coroutines.sync.Mutex

class DownloadQueueService(
    private val downloadClientManager: DownloadClientManager,
) {
    private val pollingDelay = 5_000L

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var pollingJob: Job? = null
    private val pollMutex = Mutex()

    private var _isPolling = MutableStateFlow(false)
    val isPolling: StateFlow<Boolean> = _isPolling

    private var _hasLoaded = MutableStateFlow(false)
    val hasLoaded: StateFlow<Boolean> = _hasLoaded.asStateFlow()

    private val _allTransfers = MutableStateFlow(DownloadQueueBundle())
    val allTransfers: StateFlow<DownloadQueueBundle> = _allTransfers.asStateFlow()

    private var knownClients: List<DownloadClient> = emptyList()

    init {
        observeClientsAndManagePolling()
        observeApisAndTriggerPoll()
    }

    private fun observeClientsAndManagePolling() {
        scope.launch {
            downloadClientManager.observeAllDownloadClients().collect { clients ->
                knownClients = clients
                if (clients.isNotEmpty()) {
                    startPolling()
                } else {
                    stopPolling()
                }
            }
        }
    }

    private fun observeApisAndTriggerPoll() {
        scope.launch {
            downloadClientManager.downloadClientApis.collect { apis ->
                if (apis.isNotEmpty() && !_hasLoaded.value) {
                    pollDownloadQueue()
                }
            }
        }
    }

    fun startPolling() {
        if (pollingJob?.isActive == true) return

        pollingJob =
            scope.launch {
                while (isActive) {
                    pollDownloadQueue()
                    delay(pollingDelay)
                }
            }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private suspend fun pollDownloadQueue() {
        if (!pollMutex.tryLock()) return
        try {
            val apis = downloadClientManager.downloadClientApis.value
            if (apis.isEmpty()) return

            _isPolling.value = true

            fetchAllDownloadData()
                .onSuccess { bundle ->
                    _allTransfers.value = bundle
                    _hasLoaded.value = true
                }.onError { _, _, _ ->
                    _hasLoaded.value = true
                }
        } finally {
            _isPolling.value = false
            pollMutex.unlock()
        }
    }

    suspend fun fetchAllDownloadData(): NetworkResult<DownloadQueueBundle> {
        val downloadClients = downloadClientManager.downloadClientApis.value
        if (downloadClients.isEmpty()) {
            return NetworkResult.Success(DownloadQueueBundle())
        }

        val allResults =
            coroutineScope {
                val deferredResults =
                    downloadClients.entries.flatMap { (id, api) ->
                        listOf(
                            async { id to api.getDownloads() },
                            async { id to api.getTransferInfo() },
                        )
                    }
                deferredResults.awaitAll()
            }

        val queueItems = mutableListOf<DownloadItem>()
        val transferInfos = mutableListOf<DownloadTransferInfo>()
        val clientErrors = mutableMapOf<Long, String>()

        allResults.forEach { (clientId, result) ->
            when (result) {
                is NetworkResult.Success<*> -> {
                    val data = result.data
                    if (data is List<*>) {
                        queueItems.addAll(data.filterIsInstance<DownloadItem>())
                    } else if (data is DownloadTransferInfo) {
                        transferInfos.add(data)
                    }
                }
                is NetworkResult.Error -> {
                    clientErrors[clientId] = result.message ?: "Unknown error"
                }
                is NetworkResult.Loading -> {}
            }
        }

        return NetworkResult.Success(
            DownloadQueueBundle(
                queueItems = queueItems,
                transferInfo = transferInfos,
                clientErrors = clientErrors,
            ),
        )
    }

    fun cleanup() {
        stopPolling()
        scope.cancel()
    }

    suspend fun manualRefresh() {
        pollDownloadQueue()
    }
}
