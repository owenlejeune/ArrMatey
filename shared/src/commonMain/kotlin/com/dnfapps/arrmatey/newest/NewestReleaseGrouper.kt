package com.dnfapps.arrmatey.newest

import com.dnfapps.arrmatey.arr.api.model.ProwlarrSearchResult
import com.dnfapps.arrmatey.arr.api.model.ReleaseProtocol

/**
 * One title (movie) or one season (TV) built from every indexer release that names it.
 * [seeders] is the highest seeder count among those releases and [publishDate] the newest.
 */
data class NewestRelease(
    val parsed: ParsedRelease,
    val kind: ReleaseKind,
    val seeders: Int?,
    val publishDate: String?,
    val ageHours: Double,
    val qualities: List<String>,
    val releaseCount: Int,
)

object NewestReleaseGrouper {
    const val MIN_SEEDERS = 3

    /** Prowlarr category ids for movies and TV. */
    const val MOVIES_CATEGORY = 2000
    const val TV_CATEGORY = 5000

    private val quality = Regex("""\b(2160p|4K|1080p|720p|480p)\b""", RegexOption.IGNORE_CASE)

    fun qualityOf(name: String): String? = quality.find(name)?.groupValues?.get(1)?.lowercase()

    /**
     * Drops low-quality and poorly seeded torrents, then merges releases of the same
     * title and year (movies) or title and season (TV), newest publish date first.
     * Usenet releases carry no seeder count and are kept.
     */
    fun group(
        results: List<ProwlarrSearchResult>,
        kind: ReleaseKind,
        minSeeders: Int = MIN_SEEDERS,
    ): List<NewestRelease> {
        val groups = linkedMapOf<String, NewestRelease>()
        for (result in results) {
            val name = result.title ?: continue
            if (!hasEnoughSeeders(result, minSeeders)) continue
            val parsed = ReleaseNameParser.parse(name, kind) ?: continue
            val key = keyOf(parsed, kind)
            val releaseQuality = qualityOf(name)
            val existing = groups[key]
            groups[key] = if (existing == null) {
                NewestRelease(
                    parsed = parsed,
                    kind = kind,
                    seeders = result.seeders,
                    publishDate = result.publishDate,
                    ageHours = result.ageHours,
                    qualities = listOfNotNull(releaseQuality),
                    releaseCount = 1,
                )
            } else {
                val newer = (result.publishDate ?: "") > (existing.publishDate ?: "")
                existing.copy(
                    seeders = maxOfNullable(existing.seeders, result.seeders),
                    publishDate = if (newer) result.publishDate else existing.publishDate,
                    ageHours = minOf(existing.ageHours, result.ageHours),
                    qualities = if (releaseQuality == null || releaseQuality in existing.qualities) {
                        existing.qualities
                    } else {
                        existing.qualities + releaseQuality
                    },
                    releaseCount = existing.releaseCount + 1,
                )
            }
        }
        return groups.values.sortedByDescending { it.publishDate ?: "" }
    }

    fun keyOf(parsed: ParsedRelease, kind: ReleaseKind): String {
        val title = TitleMatcher.normalize(parsed.title)
        return when (kind) {
            ReleaseKind.Movie -> "$title|${parsed.year ?: ""}"
            ReleaseKind.Tv -> "$title|${parsed.season ?: ""}"
        }
    }

    private fun hasEnoughSeeders(result: ProwlarrSearchResult, minSeeders: Int): Boolean {
        if (result.protocol == ReleaseProtocol.Usenet && result.seeders == null) return true
        return (result.seeders ?: 0) >= minSeeders
    }

    private fun maxOfNullable(a: Int?, b: Int?): Int? = when {
        a == null -> b
        b == null -> a
        else -> maxOf(a, b)
    }
}
