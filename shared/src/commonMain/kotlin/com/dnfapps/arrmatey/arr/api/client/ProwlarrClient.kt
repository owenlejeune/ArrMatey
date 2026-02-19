package com.dnfapps.arrmatey.arr.api.client

import com.dnfapps.arrmatey.arr.api.model.ProwlarrIndexer
import com.dnfapps.arrmatey.arr.api.model.ProwlarrSearchResult
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.instances.model.Instance
import io.ktor.client.HttpClient

class ProwlarrClient(
    override val instance: Instance,
    httpClient: HttpClient
) : BaseArrClient(httpClient) {

    suspend fun getIndexers(): NetworkResult<List<ProwlarrIndexer>> =
        get("indexer")

    suspend fun search(
        query: String,
        type: String = "search",
        categories: List<Int> = emptyList(),
        indexerIds: List<Long> = emptyList()
    ): NetworkResult<List<ProwlarrSearchResult>> =
        get("search", buildMap {
            put("query", query)
            put("type", type)
            if (categories.isNotEmpty()) {
                put("categories", categories.joinToString(","))
            }
            if (indexerIds.isNotEmpty()) {
                put("indexerIds", indexerIds.joinToString(","))
            }
        })
}
