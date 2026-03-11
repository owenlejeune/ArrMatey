package com.dnfapps.arrmatey.downloadclient.service

import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.client.onError
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.downloadclient.model.DownloadItem
import com.dnfapps.arrmatey.downloadclient.model.DownloadTransferInfo
import com.dnfapps.arrmatey.downloadclient.repository.DownloadClientManager
import com.dnfapps.arrmatey.downloadclient.state.DownloadQueueBundle
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

class DownloadQueueService(
    private val downloadClientManager: DownloadClientManager
) {
    private val pollingDelay = 30_000L

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var pollingJob: Job? = null

    private var _isPolling = MutableStateFlow(false)
    val isPolling: StateFlow<Boolean> = _isPolling

    private val _allTransfers = MutableStateFlow(DownloadQueueBundle())
    val allTransfers: StateFlow<DownloadQueueBundle> = _allTransfers.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        observeClientsAndManagePolling()
    }

    private fun observeClientsAndManagePolling() {
        scope.launch {
            downloadClientManager.observeAllDownloadClients().collect { clients ->
                if (clients.isNotEmpty()) {
                    startPolling()
                } else {
                    stopPolling()
                }
            }
        }
    }

    fun startPolling() {
        if (pollingJob?.isActive == true) return

        pollingJob = scope.launch {
            while(isActive) {
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
        _isPolling.value = true

        fetchAllDownloadData()
            .onSuccess { bundle -> _allTransfers.value = bundle }
            .onError { _, message, _ -> _errorMessage.value = message }

        _isPolling.value = false
    }

    suspend fun fetchAllDownloadData(): NetworkResult<DownloadQueueBundle> {
        val downloadClients = downloadClientManager.getAllDownloadClientApis()

        val deferredResults = downloadClients.flatMap { client ->
            listOf(
                scope.async { client.getDownloads() },
                scope.async { client.getTransferInfo() }
            )
        }

        val allResults = deferredResults.awaitAll()

        val queueItems = mutableListOf<DownloadItem>()
        val transferInfos = mutableListOf<DownloadTransferInfo>()
        val errors = mutableListOf<NetworkResult.Error>()

        allResults.forEach { result ->
            when (result) {
                is NetworkResult.Success<*> -> {
                    val data = result.data
                    if (data is List<*>) {
                        queueItems.addAll(data.filterIsInstance<DownloadItem>())
                    } else if (data is DownloadTransferInfo) {
                        transferInfos.add(data)
                    }
                }
                is NetworkResult.Error -> errors.add(result)
                is NetworkResult.Loading -> {}
            }
        }

        return when {
            queueItems.isNotEmpty() || transferInfos.isNotEmpty() -> {
                NetworkResult.Success(
                    DownloadQueueBundle(
                        queueItems = queueItems,
                        transferInfo = transferInfos
                    )
                )
            }
            errors.isNotEmpty() -> errors.first()
            else -> NetworkResult.Success(DownloadQueueBundle())
        }
    }

    fun cleanup() {
        stopPolling()
        scope.cancel()
    }

    suspend fun manualRefresh() {
        pollDownloadQueue()
    }
}