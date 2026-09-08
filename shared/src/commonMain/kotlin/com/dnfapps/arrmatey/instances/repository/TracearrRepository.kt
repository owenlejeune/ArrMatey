package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.tracearr.api.client.TracearrClient
import com.dnfapps.arrmatey.tracearr.api.model.TracearrHistoryResponse
import com.dnfapps.arrmatey.tracearr.api.model.TracearrMediaDetails
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStreamsResponse
import com.dnfapps.arrmatey.tracearr.api.model.TracearrTodayStats
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.onSuccess
import io.ktor.client.HttpClient
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TracearrRepository(
    override val instance: Instance,
    httpClient: HttpClient,
) : InstanceScopedRepository {

    private val tracearrClient = TracearrClient(instance, httpClient)

    private val mediaDetailsCache = mutableMapOf<String, TracearrMediaDetails>()
    private val cacheMutex = Mutex()

    override suspend fun testConnection(): NetworkResult<Unit> =
        tracearrClient.testConnection()

    suspend fun getPublicStreams(): NetworkResult<TracearrStreamsResponse> {
        val baseUrl = instance.getEffectiveBaseUrl()
        return when (val streamsResult = tracearrClient.getPublicStreams()) {
            is NetworkResult.Success -> {
                val refs = streamsResult.data.data.mapNotNull { session ->
                    (session.showMediaId ?: session.mediaId)?.takeIf { it.isNotBlank() }
                }
                val detailsMap = fetchMediaDetailsForRefs(refs)

                val updatedSessions = streamsResult.data.data.map { session ->
                    val ref = (session.showMediaId ?: session.mediaId)?.takeIf { it.isNotBlank() }
                    session.rebuildWithInstanceBaseUrl(baseUrl).copy(
                        mediaDetails = ref?.let { detailsMap[it] },
                    )
                }

                NetworkResult.Success(streamsResult.data.copy(data = updatedSessions))
            }
            is NetworkResult.Error -> streamsResult
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }

    suspend fun getTodayStats(): NetworkResult<TracearrTodayStats> =
        tracearrClient.getTodayStats()

    suspend fun getHistory(cursor: String? = null, pageSize: Int? = null): NetworkResult<TracearrHistoryResponse> {
        val baseUrl = instance.getEffectiveBaseUrl()
        return when (val historyResult = tracearrClient.getHistory(cursor = cursor, pageSize = pageSize)) {
            is NetworkResult.Success -> {
                val refs = historyResult.data.data.mapNotNull { item ->
                    (item.showMediaId ?: item.mediaId)?.takeIf { it.isNotBlank() }
                }
                val detailsMap = fetchMediaDetailsForRefs(refs)

                val updatedItems = historyResult.data.data.map { item ->
                    val ref = (item.showMediaId ?: item.mediaId)?.takeIf { it.isNotBlank() }
                    item.rebuildWithInstanceBaseUrl(baseUrl).copy(
                        mediaDetails = ref?.let { detailsMap[it] },
                    )
                }

                NetworkResult.Success(historyResult.data.copy(data = updatedItems))
            }
            is NetworkResult.Error -> historyResult
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }

    private suspend fun fetchMediaDetailsForRefs(refs: List<String>): Map<String, TracearrMediaDetails> {
        val missingRefs = cacheMutex.withLock {
            refs.filter { it.isNotBlank() && !mediaDetailsCache.containsKey(it) }.distinct()
        }

        if (missingRefs.isNotEmpty()) {
            val fetchedResults = coroutineScope {
                missingRefs.map { ref ->
                    async {
                        var details: TracearrMediaDetails? = null
                        tracearrClient.getMedia(ref).onSuccess {
                            details = it
                        }
                        details?.let { ref to it }
                    }
                }.awaitAll().filterNotNull()
            }

            if (fetchedResults.isNotEmpty()) {
                cacheMutex.withLock {
                    for ((ref, details) in fetchedResults) {
                        mediaDetailsCache[ref] = details
                    }
                }
            }
        }

        return cacheMutex.withLock {
            refs.mapNotNull { ref ->
                mediaDetailsCache[ref]?.let { ref to it }
            }.toMap()
        }
    }
}
