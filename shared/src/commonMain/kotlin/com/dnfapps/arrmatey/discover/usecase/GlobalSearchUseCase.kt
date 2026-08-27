package com.dnfapps.arrmatey.discover.usecase

import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.discover.model.SearchResult
import com.dnfapps.arrmatey.discover.model.SearchResultWeaver
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.networking.NetworkResult
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class GlobalSearchUseCase(
    private val instanceManager: InstanceManager
) {
    suspend operator fun invoke(query: String): List<SearchResult> = coroutineScope {
        if (query.isBlank()) return@coroutineScope emptyList()

        val arrRepos = instanceManager.getAllArrRepositories()
        val seerrRepos = instanceManager.getAllSeerrRepositories()

        val arrDeferred = arrRepos.map { repo ->
            async {
                val result = repo.directLookup(query)
                val data = if (result is NetworkResult.Success) result.data else emptyList()
                data.mapIndexed { index, media ->
                    SearchResult.ArrMediaResult(media, originalRank = index)
                }
            }
        }

        val seerrDeferred = seerrRepos.map { repo ->
            async {
                val result = repo.client.search(query, page = 1)
                val data = if (result is NetworkResult.Success) result.data.results else emptyList()
                data.mapIndexed { index, res ->
                    if (res.mediaType == RequestType.Person) {
                        SearchResult.SeerrPersonResult(res, originalRank = index)
                    } else {
                        SearchResult.SeerrMediaResult(res, originalRank = index)
                    }
                }
            }
        }

        val allResults: List<SearchResult> = (arrDeferred + seerrDeferred).awaitAll().flatten()

        val resultsByTmdbId = mutableMapOf<Long, MutableList<SearchResult>>()
        val resultsByTvdbId = mutableMapOf<Long, MutableList<SearchResult>>()
        val others = mutableListOf<SearchResult>()

        allResults.forEach { result ->
            when (result) {
                is SearchResult.ArrMediaResult -> {
                    val item = result.media
                    when (item) {
                        is ArrMovie -> resultsByTmdbId.getOrPut(item.tmdbId) { mutableListOf() }.add(result)
                        is ArrSeries -> {
                            resultsByTvdbId.getOrPut(item.tvdbId) { mutableListOf() }.add(result)
                            item.tmdbId?.let { resultsByTmdbId.getOrPut(it) { mutableListOf() }.add(result) }
                        }
                        else -> others.add(result)
                    }
                }
                is SearchResult.SeerrMediaResult -> {
                    resultsByTmdbId.getOrPut(result.result.id) { mutableListOf() }.add(result)
                }
                is SearchResult.SeerrPersonResult -> others.add(result)
            }
        }

        val combined = mutableListOf<SearchResult>()
        val processedTmdbIds = mutableSetOf<Long>()
        val processedTvdbIds = mutableSetOf<Long>()

        // Process by TMDB ID
        resultsByTmdbId.forEach { (tmdbId, items) ->
            if (tmdbId !in processedTmdbIds) {
                val bestItem = selectBestItem(items)
                combined.add(bestItem)
                processedTmdbIds.add(tmdbId)
                if (bestItem is SearchResult.ArrMediaResult && bestItem.media is ArrSeries) {
                    processedTvdbIds.add(bestItem.media.tvdbId)
                }
            }
        }

        // Process by TVDB ID
        resultsByTvdbId.forEach { (tvdbId, items) ->
            if (tvdbId !in processedTvdbIds) {
                val bestItem = selectBestItem(items)
                if (bestItem !in combined) {
                    combined.add(bestItem)
                }
                processedTvdbIds.add(tvdbId)
            }
        }

        combined.addAll(others)

        SearchResultWeaver.weave(query, combined)
    }

    private fun selectBestItem(items: List<SearchResult>): SearchResult {
        return items.firstOrNull { it is SearchResult.ArrMediaResult && it.media.id != null }
            ?: items.firstOrNull { it is SearchResult.ArrMediaResult && it.media.id == null }
            ?: items.first()
    }
}
