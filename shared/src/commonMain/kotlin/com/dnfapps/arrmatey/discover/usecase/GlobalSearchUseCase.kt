package com.dnfapps.arrmatey.discover.usecase

import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.discover.model.SearchResult
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.seerr.api.model.DiscoverResult
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
                if (result is NetworkResult.Success) result.data else emptyList()
            }
        }

        val seerrDeferred = seerrRepos.map { repo ->
            async {
                val result = repo.client.search(query, page = 1)
                if (result is NetworkResult.Success) result.data.results else emptyList()
            }
        }

        val allArrResults: List<ArrMedia> = arrDeferred.awaitAll().flatten()
        val allSeerrResults: List<DiscoverResult> = seerrDeferred.awaitAll().flatten()

        val resultsByTmdbId = mutableMapOf<Long, MutableList<SearchResult>>()
        val resultsByTvdbId = mutableMapOf<Long, MutableList<SearchResult>>()
        val persons = mutableListOf<SearchResult.SeerrPersonResult>()

        allArrResults.forEach { item ->
            val result = SearchResult.ArrMediaResult(item)
            when (item) {
                is ArrMovie -> resultsByTmdbId.getOrPut(item.tmdbId) { mutableListOf() }.add(result)
                is ArrSeries -> {
                    resultsByTvdbId.getOrPut(item.tvdbId) { mutableListOf() }.add(result)
                    item.tmdbId?.let { resultsByTmdbId.getOrPut(it) { mutableListOf() }.add(result) }
                }
                else -> {} 
            }
        }

        allSeerrResults.forEach { res ->
            when (res.mediaType) {
                RequestType.Movie -> resultsByTmdbId.getOrPut(res.id) { mutableListOf() }.add(SearchResult.SeerrMediaResult(res))
                RequestType.Tv -> resultsByTmdbId.getOrPut(res.id) { mutableListOf() }.add(SearchResult.SeerrMediaResult(res))
                RequestType.Person -> persons.add(SearchResult.SeerrPersonResult(res))
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
                combined.add(bestItem)
                processedTvdbIds.add(tvdbId)
            }
        }

        combined.addAll(persons)

        combined.sortedBy {
            when (it) {
                is SearchResult.ArrMediaResult -> {
                    val media = it.media
                    if (media is ArrMovie) media.popularity else 0.0
                }
                is SearchResult.SeerrMediaResult -> it.result.popularity
                is SearchResult.SeerrPersonResult -> it.result.popularity
            }
        }
    }

    private fun selectBestItem(items: List<SearchResult>): SearchResult {
        return items.firstOrNull { it is SearchResult.ArrMediaResult && it.media.id != null }
            ?: items.firstOrNull { it is SearchResult.ArrMediaResult && it.media.id == null }
            ?: items.first()
    }
}
