package com.dnfapps.arrmatey.downloadclient.repository

import com.dnfapps.arrmatey.arr.api.client.HttpClientFactory
import com.dnfapps.arrmatey.downloadclient.api.DelugeClient
import com.dnfapps.arrmatey.downloadclient.api.DownloadClientApi
import com.dnfapps.arrmatey.downloadclient.api.QBittorrentClient
import com.dnfapps.arrmatey.downloadclient.api.SABnzbdClient
import com.dnfapps.arrmatey.downloadclient.api.TransmissionClient
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.model.DownloadClientType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class DownloadClientManager(
    private val downloadClientRepository: DownloadClientRepository,
    private val httpClientFactory: HttpClientFactory
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _downloadClientApis = MutableStateFlow<Map<Long, DownloadClientApi>>(emptyMap())
    val downloadClientApis: StateFlow<Map<Long, DownloadClientApi>> = _downloadClientApis

    // Tracks the DownloadClient row backing each cached API so edits trigger a rebuild.
    private val cachedClients: MutableMap<Long, DownloadClient> = mutableMapOf()

    init {
        observeDownloadClients()
    }

    private fun observeDownloadClients() {
        scope.launch {
            downloadClientRepository.observeAllDownloadClients()
                .collect { downloadClients ->
                    updateClientApis(downloadClients)
                }
        }
    }

    private fun updateClientApis(downloadClients: List<DownloadClient>) {
        val currentApis = _downloadClientApis.value.toMutableMap()
        val incomingIds = downloadClients.mapTo(mutableSetOf()) { it.id }

        (currentApis.keys - incomingIds).forEach { id ->
            currentApis.remove(id)
            cachedClients.remove(id)
        }

        downloadClients.forEach { downloadClient ->
            val cached = cachedClients[downloadClient.id]
            if (cached == null || cached != downloadClient) {
                currentApis[downloadClient.id] = createApi(downloadClient)
                cachedClients[downloadClient.id] = downloadClient
            }
        }

        _downloadClientApis.value = currentApis
    }

    fun observeAllDownloadClients(): Flow<List<DownloadClient>> =
        downloadClientRepository.observeAllDownloadClients()

    fun observeSelectedDownloadClient(): Flow<DownloadClient?> =
        downloadClientRepository.observeSelectedDownloadClient()

    fun getSelectedDownloadClientApi(): Flow<DownloadClientApi?> {
        return observeSelectedDownloadClient()
            .map { selectedClient ->
                selectedClient?.let { _downloadClientApis.value[it.id] }
            }
    }

    fun observeSelectedApiClient(): Flow<DownloadClientApi?> =
        getSelectedDownloadClientApi()

    fun getDownloadClientApi(id: Long): DownloadClientApi? =
        _downloadClientApis.value[id]

    fun getApiClient(id: Long): DownloadClientApi? =
        getDownloadClientApi(id)

    suspend fun getOrCreateApi(id: Long): DownloadClientApi? {
        _downloadClientApis.value[id]?.let { return it }

        val client = downloadClientRepository.getDownloadClientById(id) ?: return null
        val api = createApi(client)

        _downloadClientApis.value += (id to api)
        cachedClients[id] = client

        return api
    }

    suspend fun refreshApi(id: Long): DownloadClientApi? {
        _downloadClientApis.value -= id
        cachedClients.remove(id)

        val client = downloadClientRepository.getDownloadClientById(id) ?: return null
        val api = createApi(client)

        _downloadClientApis.value += (id to api)
        cachedClients[id] = client

        return api
    }

    fun removeApi(id: Long) {
        _downloadClientApis.value -= id
        cachedClients.remove(id)
    }

    fun createApiFromClient(client: DownloadClient): DownloadClientApi {
        return createApi(client)
    }

    suspend fun getSelectedDownloadClientApiSnapshot(): DownloadClientApi? {
        val selectedClient = downloadClientRepository.getSelectedDownloadClient()
        return selectedClient?.let {
            getOrCreateApi(it.id)
        }
    }

    suspend fun getAllDownloadClientApis(): List<DownloadClientApi> =
        _downloadClientApis.value.values.toList()

    suspend fun getDownloadClientById(id: Long): DownloadClient? =
        downloadClientRepository.getDownloadClientById(id)

    suspend fun setSelectedClient(downloadClient: DownloadClient) {
        downloadClientRepository.setDownloadClientActive(downloadClient)
    }

    private fun createApi(downloadClient: DownloadClient): DownloadClientApi {
        val httpClient = httpClientFactory.createDownloadClient(downloadClient)
        return when (downloadClient.type) {
            DownloadClientType.QBittorrent -> QBittorrentClient(downloadClient, httpClient)
            DownloadClientType.SABnzbd -> SABnzbdClient(downloadClient, httpClient)
            DownloadClientType.Deluge -> DelugeClient(downloadClient, httpClient)
            DownloadClientType.Transmission -> TransmissionClient(downloadClient, httpClient)
        }
    }

    fun cleanup() {
        scope.cancel()
    }
}