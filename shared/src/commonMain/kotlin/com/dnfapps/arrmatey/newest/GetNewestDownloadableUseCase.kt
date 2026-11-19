package com.dnfapps.arrmatey.newest

import com.dnfapps.arrmatey.instances.repository.ProwlarrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.seerr.api.model.DiscoverResult
import com.dnfapps.networking.NetworkResult
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * The newest releases Prowlarr's indexers offer, from an empty-query search,
 * grouped by title and matched to Seerr for posters and details when Seerr is available.
 * Titles Seerr reports as already available are left out.
 */
class GetNewestDownloadableUseCase {
    private val matchCache = mutableMapOf<String, DiscoverResult?>()

    suspend operator fun invoke(
        prowlarr: ProwlarrInstanceRepository,
        seerr: SeerrInstanceRepository?,
        kind: ReleaseKind,
    ): NetworkResult<List<NewestDownloadableItem>> {
        val category = when (kind) {
            ReleaseKind.Movie -> NewestReleaseGrouper.MOVIES_CATEGORY
            ReleaseKind.Tv -> NewestReleaseGrouper.TV_CATEGORY
        }
        return when (val result = prowlarr.search(query = "", categories = listOf(category))) {
            is NetworkResult.Success -> {
                val releases = NewestReleaseGrouper.group(result.data, kind).take(MAX_ITEMS)
                val items = if (seerr == null) {
                    releases.map { NewestDownloadableItem(it) }
                } else {
                    matchAll(releases, seerr)
                }
                NetworkResult.Success(items.filterNot { it.isInLibrary })
            }
            is NetworkResult.Error -> result
            is NetworkResult.Loading -> result
        }
    }

    private suspend fun matchAll(
        releases: List<NewestRelease>,
        seerr: SeerrInstanceRepository,
    ): List<NewestDownloadableItem> = coroutineScope {
        releases.chunked(CONCURRENT_LOOKUPS).flatMap { chunk ->
            chunk.map { release -> async { NewestDownloadableItem(release, match(release, seerr)) } }.awaitAll()
        }
    }

    private suspend fun match(release: NewestRelease, seerr: SeerrInstanceRepository): DiscoverResult? {
        val cacheKey = "${release.kind}|${TitleMatcher.normalize(release.parsed.title)}|${release.parsed.year ?: ""}"
        if (matchCache.containsKey(cacheKey)) return matchCache[cacheKey]
        return when (val response = seerr.client.search(query = release.parsed.title)) {
            is NetworkResult.Success -> {
                val match = TitleMatcher.bestMatch(release.parsed, release.kind, response.data.results)
                matchCache[cacheKey] = match
                match
            }
            // Not cached, so the lookup is tried again on the next refresh.
            else -> null
        }
    }

    companion object {
        const val MAX_ITEMS = 60
        private const val CONCURRENT_LOOKUPS = 6
    }
}
