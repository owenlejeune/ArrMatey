package com.dnfapps.arrmatey.instances.repository

import androidx.room.Index
import com.dnfapps.arrmatey.arr.api.client.ProwlarrClient
import com.dnfapps.arrmatey.arr.api.model.ArrSoftwareStatus
import com.dnfapps.arrmatey.arr.api.model.IndexerStatus
import com.dnfapps.arrmatey.arr.api.model.ProwlarrIndexer
import com.dnfapps.arrmatey.arr.api.model.ProwlarrSearchResult
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
import com.dnfapps.arrmatey.instances.model.Instance
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProwlarrInstanceRepository(
    override val instance: Instance,
    httpClient: HttpClient
) : InstanceScopedRepository {

    private val prowlarrClient = ProwlarrClient(instance, httpClient)

    private val _softwareStatus = MutableStateFlow<ArrSoftwareStatus?>(null)
    val softwareStatus: StateFlow<ArrSoftwareStatus?> = _softwareStatus.asStateFlow()

    private val _indexerStatus = MutableStateFlow<List<IndexerStatus>>(emptyList())
    val indexerStatus: StateFlow<List<IndexerStatus>> = _indexerStatus.asStateFlow()

    private val _indexers = MutableStateFlow<List<ProwlarrIndexer>>(emptyList())
    val indexers: StateFlow<List<ProwlarrIndexer>> = _indexers.asStateFlow()

    override suspend fun testConnection(): NetworkResult<Unit> =
        prowlarrClient.testConnection()

    suspend fun refreshStatus() {
        prowlarrClient.getStatus()
            .onSuccess { _softwareStatus.value = it }
    }

    suspend fun getIndexers(): NetworkResult<List<ProwlarrIndexer>> =
        prowlarrClient.getIndexers().onSuccess { _indexers.value = it }

    suspend fun getIndexerStatus() {
        prowlarrClient.getIndexerStatus()
            .onSuccess { _indexerStatus.value = it }
    }

    suspend fun search(
        query: String,
        categories: List<Int> = emptyList(),
        indexerIds: List<Long> = emptyList()
    ): NetworkResult<List<ProwlarrSearchResult>> =
        prowlarrClient.search(query = query, categories = categories, indexerIds = indexerIds)

    suspend fun testIndexer(indexer: ProwlarrIndexer): NetworkResult<Unit> =
        prowlarrClient.testIndexer(indexer)

    suspend fun updateIndexer(indexer: ProwlarrIndexer): NetworkResult<ProwlarrIndexer> =
        prowlarrClient.updateIndexer(indexer)

    suspend fun grabRelease(guid: String, indexerId: Long): NetworkResult<ProwlarrSearchResult> =
        prowlarrClient.grab(guid, indexerId)
}
