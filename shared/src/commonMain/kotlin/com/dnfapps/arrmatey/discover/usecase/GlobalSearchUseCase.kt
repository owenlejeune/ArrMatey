package com.dnfapps.arrmatey.discover.usecase

import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Arrtist
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.SearchAudiobook
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
                    SearchResult.ArrMediaResult(media, instanceId = repo.instance.id, originalRank = index)
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
        val resultsByAsin = mutableMapOf<String, MutableList<SearchResult>>()
        val resultsByMbId = mutableMapOf<String, MutableList<SearchResult>>()
        val others = mutableListOf<SearchResult>()

        allResults.forEach { result ->
            when (result) {
                is SearchResult.ArrMediaResult -> {
                    when (val item = result.media) {
                        is ArrMovie -> resultsByTmdbId.getOrPut(item.tmdbId) { mutableListOf() }.add(result)
                        is ArrSeries -> {
                            resultsByTvdbId.getOrPut(item.tvdbId) { mutableListOf() }.add(result)
                            item.tmdbId?.let { resultsByTmdbId.getOrPut(it) { mutableListOf() }.add(result) }
                        }
                        is Audiobook -> item.asin?.let { resultsByAsin.getOrPut(it) { mutableListOf() }.add(result) } ?: others.add(result)
                        is SearchAudiobook -> resultsByAsin.getOrPut(item.asin) { mutableListOf() }.add(result)
                        is Arrtist -> (item.mbId ?: item.foreignArtistId)?.let { resultsByMbId.getOrPut(it) { mutableListOf() }.add(result) } ?: others.add(result)
                        is Author -> item.foreignAuthorId?.let { resultsByAsin.getOrPut(it) { mutableListOf() }.add(result) } ?: others.add(result)
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
        val processedAsins = mutableSetOf<String>()
        val processedMbIds = mutableSetOf<String>()

        // Process by TMDB ID
        resultsByTmdbId.forEach { (tmdbId, items) ->
            if (tmdbId !in processedTmdbIds) {
                val bestItem = selectBestItem(items)
                combined.add(bestItem)
                processedTmdbIds.add(tmdbId)
                if (bestItem is SearchResult.ArrMediaResult) {
                    val media = bestItem.media
                    if (media is ArrSeries) {
                        processedTvdbIds.add(media.tvdbId)
                    }
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

        // Process by ASIN
        resultsByAsin.forEach { (asin, items) ->
            if (asin !in processedAsins) {
                val bestItem = selectBestItem(items)
                if (bestItem !in combined) {
                    combined.add(bestItem)
                }
                processedAsins.add(asin)
            }
        }

        // Process by MBID
        resultsByMbId.forEach { (mbId, items) ->
            if (mbId !in processedMbIds) {
                val bestItem = selectBestItem(items)
                if (bestItem !in combined) {
                    combined.add(bestItem)
                }
                processedMbIds.add(mbId)
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
