package com.dnfapps.arrmatey.discover.usecase

import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Arrtist
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.SearchAudiobook
import com.dnfapps.arrmatey.discover.model.SearchResult
import com.dnfapps.arrmatey.discover.model.SearchResultWeaver
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.mediaserver.model.MediaType
import com.dnfapps.arrmatey.mediaserver.repository.MediaServerManager
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.asSuccess
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val NORMALIZE_REGEX = Regex("[^a-zA-Z0-9]")

class GlobalSearchUseCase(
    private val instanceManager: InstanceManager,
    private val mediaServerManager: MediaServerManager? = null,
) {
    operator fun invoke(query: String): Flow<List<SearchResult>> = channelFlow {
        if (query.isBlank()) {
            send(emptyList())
            return@channelFlow
        }

        val arrRepos = (instanceManager.getAllArrRepositories().ifEmpty { instanceManager.awaitAllArrRepositories() }).sortedBy { it.instance.id }
        val seerrRepos = instanceManager.getAllSeerrRepositories().sortedBy { it.instance.id }
        val mediaServerRepos = mediaServerManager?.activeRepositories?.value ?: emptyList()

        if (arrRepos.isEmpty() && seerrRepos.isEmpty() && mediaServerRepos.isEmpty()) {
            send(emptyList())
            return@channelFlow
        }

        val hasSonarr = arrRepos.any { it.instance.type == InstanceType.Sonarr }
        val hasRadarr = arrRepos.any { it.instance.type == InstanceType.Radarr }
        val includeSeerrMedia = !(hasSonarr && hasRadarr)

        val libraryByTmdbId = mutableMapOf<Long, MutableList<SearchResult.ArrMediaResult>>()
        val libraryByTvdbId = mutableMapOf<Long, MutableList<SearchResult.ArrMediaResult>>()
        val libraryByCleanTitle = mutableMapOf<String, MutableList<SearchResult.ArrMediaResult>>()

        arrRepos.forEach { repo ->
            val items =
                repo.library.value
                    ?.asSuccess()
                    ?.data ?: emptyList()
            items.forEach { media ->
                val res = SearchResult.ArrMediaResult(
                    media = media,
                    instanceId = repo.instance.id,
                    originalRank = 0,
                    instanceTypeOverride = repo.instance.type,
                )
                when (media) {
                    is ArrMovie -> {
                        if (media.tmdbId != 0L) libraryByTmdbId.getOrPut(media.tmdbId) { mutableListOf() }.add(res)
                        val clean = normalizeTitle(media.cleanTitle ?: media.title.orEmpty())
                        if (clean.isNotBlank()) {
                            libraryByCleanTitle.getOrPut(clean) { mutableListOf() }.add(res)
                        }
                    }
                    is ArrSeries -> {
                        if (media.tvdbId != 0L) libraryByTvdbId.getOrPut(media.tvdbId) { mutableListOf() }.add(res)
                        if (media.tmdbId != null &&
                            media.tmdbId != 0L
                        ) {
                            libraryByTmdbId.getOrPut(media.tmdbId) { mutableListOf() }.add(res)
                        }
                        val clean = normalizeTitle(media.cleanTitle ?: media.title.orEmpty())
                        if (clean.isNotBlank()) {
                            libraryByCleanTitle.getOrPut(clean) { mutableListOf() }.add(res)
                        }
                    }
                    else -> {}
                }
            }
        }

        val mutex = Mutex()
        val accumulatedResults = mutableListOf<SearchResult>()
        val searchJobs = mutableListOf<Job>()

        arrRepos.forEach { repo ->
            val job =
                launch {
                    try {
                        val result = repo.directLookup(query)
                        val data = if (result is NetworkResult.Success) result.data else emptyList()
                        val enrichedData = if (repo.instance.type == InstanceType.Chaptarr || repo.instance.type == InstanceType.Bookshelf) {
                            coroutineScope {
                                data.map { media ->
                                    async {
                                        try {
                                            if (media is Author && media.images.isEmpty() && !media.foreignAuthorId.isNullOrBlank()) {
                                                val foreignId = media.foreignAuthorId
                                                val queries = listOf(
                                                    "author:$foreignId",
                                                    if (foreignId.startsWith("gr:")) "author:${foreignId.removePrefix("gr:")}" else "author:gr:$foreignId",
                                                    foreignId,
                                                ).distinct()
                                                var enriched: Author = media
                                                for (q in queries) {
                                                    val detailRes = repo.directLookup(q)
                                                    if (detailRes is NetworkResult.Success && detailRes.data.isNotEmpty()) {
                                                        val full = detailRes.data.filterIsInstance<Author>().firstOrNull { it.images.isNotEmpty() || !it.overview.isNullOrBlank() }
                                                            ?: detailRes.data.filterIsInstance<Author>().firstOrNull()
                                                        if (full != null) {
                                                            enriched = full
                                                            break
                                                        }
                                                    }
                                                }
                                                enriched
                                            } else {
                                                media
                                            }
                                        } catch (_: Exception) {
                                            media
                                        }
                                    }
                                }.awaitAll()
                            }
                        } else {
                            data
                        }
                        val mapped =
                            enrichedData.mapIndexed { index, media ->
                                SearchResult.ArrMediaResult(
                                    media = media,
                                    instanceId = repo.instance.id,
                                    originalRank = index,
                                    instanceTypeOverride = repo.instance.type,
                                )
                            }
                        if (mapped.isNotEmpty()) {
                            val woven =
                                mutex.withLock {
                                    accumulatedResults.addAll(mapped)
                                    processAndWeave(
                                        query = query,
                                        allResults = accumulatedResults,
                                        libraryByTmdbId = libraryByTmdbId,
                                        libraryByTvdbId = libraryByTvdbId,
                                        libraryByCleanTitle = libraryByCleanTitle,
                                    )
                                }
                            send(woven)
                        }
                    } catch (_: Exception) {
                    }
                }
            searchJobs.add(job)
        }

        seerrRepos.forEach { repo ->
            val job =
                launch {
                    try {
                        val result = repo.client.search(query, page = 1)
                        val data = if (result is NetworkResult.Success) result.data.results else emptyList()
                        val mapped =
                            data.mapIndexedNotNull { index, res ->
                                if (res.mediaType == RequestType.Person) {
                                    SearchResult.SeerrPersonResult(res, originalRank = index)
                                } else if (includeSeerrMedia) {
                                    SearchResult.SeerrMediaResult(res, originalRank = index)
                                } else {
                                    null
                                }
                            }
                        if (mapped.isNotEmpty()) {
                            val woven =
                                mutex.withLock {
                                    accumulatedResults.addAll(mapped)
                                    processAndWeave(
                                        query = query,
                                        allResults = accumulatedResults,
                                        libraryByTmdbId = libraryByTmdbId,
                                        libraryByTvdbId = libraryByTvdbId,
                                        libraryByCleanTitle = libraryByCleanTitle,
                                    )
                                }
                            send(woven)
                        }
                    } catch (_: Exception) {
                    }
                }
            searchJobs.add(job)
        }

        mediaServerRepos.forEach { repo ->
            val job =
                launch {
                    try {
                        val result = repo.search(query, limit = 20)
                        val data = if (result is NetworkResult.Success) result.data else emptyList()
                        val mapped =
                            data.mapIndexed { index, item ->
                                SearchResult.MediaServerResult(
                                    item = item,
                                    serverType = repo.serverType,
                                    serverLabel = repo.integration.label,
                                    originalRank = index,
                                )
                            }
                        if (mapped.isNotEmpty()) {
                            val woven =
                                mutex.withLock {
                                    accumulatedResults.addAll(mapped)
                                    processAndWeave(
                                        query = query,
                                        allResults = accumulatedResults,
                                        libraryByTmdbId = libraryByTmdbId,
                                        libraryByTvdbId = libraryByTvdbId,
                                        libraryByCleanTitle = libraryByCleanTitle,
                                    )
                                }
                            send(woven)
                        }
                    } catch (_: Exception) {
                    }
                }
            searchJobs.add(job)
        }

        searchJobs.joinAll()

        if (accumulatedResults.isEmpty()) {
            send(emptyList())
        }
    }

    private fun processAndWeave(
        query: String,
        allResults: List<SearchResult>,
        libraryByTmdbId: Map<Long, List<SearchResult.ArrMediaResult>>,
        libraryByTvdbId: Map<Long, List<SearchResult.ArrMediaResult>>,
        libraryByCleanTitle: Map<String, List<SearchResult.ArrMediaResult>>,
    ): List<SearchResult> {
        val clusters = mutableListOf<MutableList<SearchResult>>()
        for (i in allResults.indices) {
            val result = allResults[i]
            var matchedCluster: MutableList<SearchResult>? = null
            var c = 0
            while (c < clusters.size) {
                val cluster = clusters[c]
                var matches = false
                for (j in cluster.indices) {
                    if (areMatching(cluster[j], result)) {
                        matches = true
                        break
                    }
                }
                if (matches) {
                    if (matchedCluster == null) {
                        matchedCluster = cluster
                        cluster.add(result)
                        c++
                    } else {
                        matchedCluster.addAll(cluster)
                        clusters.removeAt(c)
                    }
                } else {
                    c++
                }
            }
            if (matchedCluster == null) {
                clusters.add(mutableListOf(result))
            }
        }

        val combined = mutableListOf<SearchResult>()
        clusters.forEach { cluster ->
            val clusterTmdbIds = cluster.mapNotNull { it.tmdbId() }.toSet()
            val clusterTvdbIds = cluster.mapNotNull { it.tvdbId() }.toSet()
            val clusterCleanTitles = cluster.mapNotNull {
                val clean = normalizeTitle(it.cleanTitle.ifBlank { it.title })
                clean.takeIf { it.isNotBlank() }
            }.toSet()

            val libItems = mutableListOf<SearchResult.ArrMediaResult>()
            clusterTmdbIds.forEach { libraryByTmdbId[it]?.let { libItems.addAll(it) } }
            clusterTvdbIds.forEach { libraryByTvdbId[it]?.let { libItems.addAll(it) } }
            clusterCleanTitles.forEach { libraryByCleanTitle[it]?.let { libItems.addAll(it) } }

            cluster.addAll(libItems.distinctBy { it.id })

            val bestItem = selectBestItem(cluster)
            combined.add(bestItem)
        }

        return SearchResultWeaver.weave(query, combined)
    }

    private fun areMatching(a: SearchResult, b: SearchResult): Boolean {
        // 1. TMDB ID match (if both have valid tmdbId)
        val aTmdb = a.tmdbId()
        val bTmdb = b.tmdbId()
        if (aTmdb != null && bTmdb != null && aTmdb == bTmdb && aTmdb != 0L) return true

        // 2. TVDB ID match (if both have valid tvdbId)
        val aTvdb = a.tvdbId()
        val bTvdb = b.tvdbId()
        if (aTvdb != null && bTvdb != null && aTvdb == bTvdb && aTvdb != 0L) return true

        // 3. ASIN match
        val aAsin = a.asin()
        val bAsin = b.asin()
        if (aAsin != null && bAsin != null && aAsin == bAsin && aAsin.isNotBlank()) return true

        // 4. Author ID match
        val aAuthor = a.authorId()
        val bAuthor = b.authorId()
        if (aAuthor != null && bAuthor != null && aAuthor == bAuthor && aAuthor.isNotBlank()) return true

        // 5. MusicBrainz ID match
        val aMb = a.mbId()
        val bMb = b.mbId()
        if (aMb != null && bMb != null && aMb == bMb && aMb.isNotBlank()) return true

        // 6. Title + Kind + Year match (for Movie and Series)
        val aKind = a.mediaKind()
        val bKind = b.mediaKind()
        if ((aKind == MediaKind.MOVIE || aKind == MediaKind.SERIES) && aKind == bKind) {
            val aClean = normalizeTitle(a.cleanTitle.ifBlank { a.title })
            val bClean = normalizeTitle(b.cleanTitle.ifBlank { b.title })
            if (aClean.isNotBlank() && bClean.isNotBlank()) {
                val titleMatches = aClean == bClean ||
                    aClean.removePrefix("the") == bClean.removePrefix("the")
                if (titleMatches) {
                    val yearMatches = a.year == null || b.year == null || kotlin.math.abs(a.year!! - b.year!!) <= 1
                    if (yearMatches) return true
                }
            }
        }

        return false
    }

    private fun SearchResult.tmdbId(): Long? = when (this) {
        is SearchResult.ArrMediaResult -> when (val m = media) {
            is ArrMovie -> m.tmdbId.takeIf { it != 0L }
            is ArrSeries -> m.tmdbId?.takeIf { it != 0L }
            else -> null
        }
        is SearchResult.MediaServerResult -> item.tmdbId?.takeIf { it != 0L }
        is SearchResult.SeerrMediaResult -> result.id.takeIf { it != 0L }
        is SearchResult.SeerrPersonResult -> null
    }

    private fun SearchResult.tvdbId(): Long? = when (this) {
        is SearchResult.ArrMediaResult -> when (val m = media) {
            is ArrSeries -> m.tvdbId.takeIf { it != 0L }
            else -> null
        }
        is SearchResult.MediaServerResult -> item.tvdbId?.takeIf { it != 0L }
        else -> null
    }

    private fun SearchResult.asin(): String? = when (this) {
        is SearchResult.ArrMediaResult -> when (val m = media) {
            is Audiobook -> m.asin?.takeIf { it.isNotBlank() }
            is SearchAudiobook -> m.asin.takeIf { it.isNotBlank() }
            else -> null
        }
        else -> null
    }

    private fun SearchResult.authorId(): String? = when (this) {
        is SearchResult.ArrMediaResult -> when (val m = media) {
            is Author -> m.foreignAuthorId?.takeIf { it.isNotBlank() }?.let { "${instanceType}_$it" }
            else -> null
        }
        else -> null
    }

    private fun SearchResult.mbId(): String? = when (this) {
        is SearchResult.ArrMediaResult -> when (val m = media) {
            is Arrtist -> (m.mbId ?: m.foreignArtistId)?.takeIf { it.isNotBlank() }
            else -> null
        }
        else -> null
    }

    private enum class MediaKind {
        MOVIE,
        SERIES,
        EPISODE,
        BOOK,
        AUDIOBOOK,
        MUSIC,
        PERSON,
        OTHER,
    }

    private fun SearchResult.mediaKind(): MediaKind = when (this) {
        is SearchResult.ArrMediaResult -> when (media) {
            is ArrMovie -> MediaKind.MOVIE
            is ArrSeries -> MediaKind.SERIES
            is Author -> MediaKind.BOOK
            is Audiobook, is SearchAudiobook -> MediaKind.AUDIOBOOK
            is Arrtist -> MediaKind.MUSIC
            else -> MediaKind.OTHER
        }
        is SearchResult.MediaServerResult -> when (item.type) {
            MediaType.MOVIE -> MediaKind.MOVIE
            MediaType.SHOW -> MediaKind.SERIES
            MediaType.EPISODE -> MediaKind.EPISODE
            MediaType.TRACK, MediaType.ALBUM -> MediaKind.MUSIC
            else -> MediaKind.OTHER
        }
        is SearchResult.SeerrMediaResult -> when (result.mediaType) {
            RequestType.Movie -> MediaKind.MOVIE
            RequestType.Tv -> MediaKind.SERIES
            else -> MediaKind.OTHER
        }
        is SearchResult.SeerrPersonResult -> MediaKind.PERSON
    }

    private fun normalizeTitle(title: String): String = title.replace(NORMALIZE_REGEX, "").lowercase()

    private fun selectBestItem(items: List<SearchResult>): SearchResult {
        val inLibraryArr = items.filterIsInstance<SearchResult.ArrMediaResult>().firstOrNull { it.media.id != null && it.media.id != 0L }
        val anyArr = inLibraryArr ?: items.filterIsInstance<SearchResult.ArrMediaResult>().firstOrNull()

        val matchingMediaServer = items.filterIsInstance<SearchResult.MediaServerResult>().firstOrNull()

        if (anyArr != null) {
            val serverMatch = matchingMediaServer ?: run {
                val manager = mediaServerManager ?: return@run null
                val tmdbId = when (val m = anyArr.media) {
                    is ArrMovie -> m.tmdbId.takeIf { it != 0L }
                    is ArrSeries -> m.tmdbId?.takeIf { it != 0L }
                    else -> null
                }
                val tvdbId = when (val m = anyArr.media) {
                    is ArrSeries -> m.tvdbId.takeIf { it != 0L }
                    else -> null
                }
                val type = when (anyArr.media) {
                    is ArrMovie -> MediaType.MOVIE
                    is ArrSeries -> MediaType.SHOW
                    else -> null
                }
                val match = runCatching {
                    manager.findMatchingItem(
                        tmdbId = tmdbId,
                        tvdbId = tvdbId,
                        title = anyArr.cleanTitle,
                        year = anyArr.year,
                        mediaType = type,
                    )
                }.getOrNull()
                match?.let {
                    SearchResult.MediaServerResult(
                        item = it,
                        serverType = it.serverType,
                        serverLabel = it.serverType.name,
                    )
                }
            }

            return if (serverMatch != null) {
                anyArr.copy(
                    mediaServerType = serverMatch.serverType,
                    mediaServerLabel = serverMatch.serverLabel,
                )
            } else {
                anyArr
            }
        }

        if (matchingMediaServer != null) {
            return matchingMediaServer
        }

        val seerr = items.filterIsInstance<SearchResult.SeerrMediaResult>().firstOrNull()
        if (seerr != null) {
            return seerr
        }

        return items.first()
    }
}
