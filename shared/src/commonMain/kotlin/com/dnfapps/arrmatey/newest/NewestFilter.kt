package com.dnfapps.arrmatey.newest

import com.dnfapps.arrmatey.discover.model.DiscoverFilterState
import com.dnfapps.arrmatey.discover.model.DiscoverSortBy
import com.dnfapps.arrmatey.discover.model.DiscoverSortOrder

/**
 * Applies the See More screens' [DiscoverFilterState] to Prowlarr's newest releases.
 * Genre, language, rating and popularity come from the Seerr match, so they only
 * apply when Seerr is configured, and a genre or language filter drops releases
 * Seerr could not match.
 */
object NewestFilter {
    /** Newest uploads first, the order Prowlarr's feed is read in. */
    val DEFAULT = DiscoverFilterState(sortBy = DiscoverSortBy.UPLOAD_TIME)

    fun sortOptions(hasSeerr: Boolean): List<DiscoverSortBy> = if (hasSeerr) {
        listOf(
            DiscoverSortBy.UPLOAD_TIME,
            DiscoverSortBy.RELEASE_DATE,
            DiscoverSortBy.SEEDERS,
            DiscoverSortBy.RATING,
            DiscoverSortBy.POPULARITY,
        )
    } else {
        listOf(DiscoverSortBy.UPLOAD_TIME, DiscoverSortBy.RELEASE_DATE, DiscoverSortBy.SEEDERS)
    }

    fun apply(
        items: List<NewestDownloadableItem>,
        filter: DiscoverFilterState,
        hasSeerr: Boolean,
    ): List<NewestDownloadableItem> {
        val filtered = items.filter { item ->
            val match = item.match
            when {
                filter.hideInLibrary && match?.isAvailableInLibrary == true -> false
                !hasSeerr -> true
                filter.selectedGenreId != null && match?.genreIds?.contains(filter.selectedGenreId) != true -> false
                filter.selectedLanguage != null && !match?.originalLanguage.equals(filter.selectedLanguage, ignoreCase = true) -> false
                else -> true
            }
        }
        val sortBy = if (filter.sortBy in sortOptions(hasSeerr)) filter.sortBy else DEFAULT.sortBy
        return sorted(filtered, sortBy, filter.sortOrder)
    }

    fun sorted(
        items: List<NewestDownloadableItem>,
        sortBy: DiscoverSortBy,
        sortOrder: DiscoverSortOrder,
    ): List<NewestDownloadableItem> {
        val comparator: Comparator<NewestDownloadableItem> = when (sortBy) {
            DiscoverSortBy.UPLOAD_TIME -> compareBy { it.release.publishDate ?: "" }
            DiscoverSortBy.RELEASE_DATE -> compareBy { releaseDate(it) }
            DiscoverSortBy.SEEDERS -> compareBy { it.release.seeders ?: -1 }
            DiscoverSortBy.RATING -> compareBy<NewestDownloadableItem> { it.match?.voteAverage ?: -1.0 }.thenBy { it.match?.voteCount ?: -1 }
            DiscoverSortBy.POPULARITY -> compareBy { it.match?.popularity ?: -1.0 }
        }
        // Ties keep the newest upload first.
        val withTies = comparator.thenByDescending { it.release.publishDate ?: "" }
        return if (sortOrder == DiscoverSortOrder.DESCENDING) {
            items.sortedWith(comparator.reversed().thenByDescending { it.release.publishDate ?: "" })
        } else {
            items.sortedWith(withTies)
        }
    }

    private fun releaseDate(item: NewestDownloadableItem): String = item.match?.releaseDate ?: item.match?.firstAirDate ?: item.release.parsed.year?.toString() ?: ""
}
