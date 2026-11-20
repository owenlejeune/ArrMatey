package com.dnfapps.arrmatey.newest

import com.dnfapps.arrmatey.arr.api.model.ProwlarrSearchResult
import com.dnfapps.arrmatey.instances.repository.ProwlarrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.seerr.api.model.DiscoverResult
import com.dnfapps.networking.NetworkResult
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeoutOrNull

/**
 * The newest releases Prowlarr's indexers offer, from an empty-query search,
 * grouped by title and matched to Seerr for posters and details when Seerr is available.
 *
 * The grouped releases are emitted as soon as Prowlarr answers, then again after each
 * batch of Seerr lookups, so posters fill in without waiting for every match.
 * Titles Seerr reports as already available are left out.
 */
class GetNewestDownloadableUseCase {
    private val matchCache = mutableMapOf<String, DiscoverResult?>()

    operator fun invoke(
        prowlarr: ProwlarrInstanceRepository,
        seerr: SeerrInstanceRepository?,
        kind: ReleaseKind,
    ): Flow<NetworkResult<List<NewestDownloadableItem>>> {
        val category = when (kind) {
            ReleaseKind.Movie -> NewestReleaseGrouper.MOVIES_CATEGORY
            ReleaseKind.Tv -> NewestReleaseGrouper.TV_CATEGORY
        }
        return newest(
            kind = kind,
            search = {
                prowlarr.search(
                    query = "",
                    categories = listOf(category),
                    limit = SEARCH_LIMIT,
                    timeoutMillis = SEARCH_TIMEOUT_MILLIS,
                )
            },
            lookup = seerr?.let { repo ->
                { release ->
                    when (val response = repo.client.search(query = release.parsed.title)) {
                        is NetworkResult.Success -> LookupResult.Found(TitleMatcher.bestMatch(release.parsed, release.kind, response.data.results))
                        else -> LookupResult.Failed
                    }
                }
            },
        )
    }

    /** What one Seerr lookup returned. A failed lookup is not cached, so it is tried again. */
    sealed interface LookupResult {
        data class Found(
            val match: DiscoverResult?,
        ) : LookupResult

        object Failed : LookupResult
    }

    internal fun newest(
        kind: ReleaseKind,
        search: suspend () -> NetworkResult<List<ProwlarrSearchResult>>,
        lookup: (suspend (NewestRelease) -> LookupResult)?,
        lookupTimeoutMillis: Long = LOOKUP_TIMEOUT_MILLIS,
    ): Flow<NetworkResult<List<NewestDownloadableItem>>> = flow {
        val result = search()
        if (result !is NetworkResult.Success) {
            emit(result as? NetworkResult.Error ?: NetworkResult.Error(message = "Prowlarr search failed"))
            return@flow
        }
        val releases = NewestReleaseGrouper.group(result.data, kind).take(MAX_ITEMS)
        val matches = releases.associateWith { matchCache[cacheKey(it)] }.toMutableMap()
        emit(NetworkResult.Success(items(releases, matches)))
        if (lookup == null) return@flow

        val pending = releases.filterNot { matchCache.containsKey(cacheKey(it)) }
        for (chunk in pending.chunked(CONCURRENT_LOOKUPS)) {
            val found = coroutineScope {
                chunk.map { release ->
                    async { release to (withTimeoutOrNull(lookupTimeoutMillis) { lookup(release) } ?: LookupResult.Failed) }
                }.awaitAll()
            }
            for ((release, lookupResult) in found) {
                if (lookupResult is LookupResult.Found) {
                    matchCache[cacheKey(release)] = lookupResult.match
                    matches[release] = lookupResult.match
                }
            }
            emit(NetworkResult.Success(items(releases, matches)))
        }
    }

    private fun items(
        releases: List<NewestRelease>,
        matches: Map<NewestRelease, DiscoverResult?>,
    ): List<NewestDownloadableItem> = releases.map { NewestDownloadableItem(it, matches[it]) }.filterNot { it.isInLibrary }

    private fun cacheKey(release: NewestRelease): String = "${release.kind}|${TitleMatcher.normalize(release.parsed.title)}|${release.parsed.year ?: ""}"

    companion object {
        const val MAX_ITEMS = 60
        const val SEARCH_LIMIT = 200
        const val SEARCH_TIMEOUT_MILLIS = 90_000L
        const val LOOKUP_TIMEOUT_MILLIS = 15_000L
        private const val CONCURRENT_LOOKUPS = 6
    }
}
