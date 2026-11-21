package com.dnfapps.arrmatey.newest

import com.dnfapps.arrmatey.seerr.api.model.DiscoverResult
import com.dnfapps.arrmatey.seerr.api.model.RequestType

/** Picks the Seerr search result that a parsed release name refers to. */
object TitleMatcher {
    const val MIN_SIMILARITY = 0.85

    private val nonAlphanumeric = Regex("""[^a-z0-9]+""")

    fun normalize(value: String): String = nonAlphanumeric.replace(value.lowercase(), " ").trim()

    /** 1.0 for identical normalized strings, falling towards 0.0 with edit distance. */
    fun similarity(a: String, b: String): Double {
        val left = normalize(a)
        val right = normalize(b)
        val longest = maxOf(left.length, right.length)
        if (longest == 0) return 1.0
        return 1.0 - levenshtein(left, right).toDouble() / longest
    }

    fun bestMatch(
        parsed: ParsedRelease,
        kind: ReleaseKind,
        candidates: List<DiscoverResult>,
    ): DiscoverResult? {
        val wanted = if (kind == ReleaseKind.Movie) RequestType.Movie else RequestType.Tv
        var best: DiscoverResult? = null
        var bestScore = 0.0
        for (candidate in candidates) {
            if (candidate.mediaType != wanted) continue
            val candidateYear = (candidate.releaseDate ?: candidate.firstAirDate)?.take(4)?.toIntOrNull()
            if (parsed.year != null && candidateYear != null && kotlin.math.abs(candidateYear - parsed.year) > 1) continue
            val title = candidate.title ?: candidate.name ?: continue
            val original = candidate.originalTitle ?: candidate.originalName ?: ""
            val score = maxOf(similarity(parsed.title, title), similarity(parsed.title, original))
            if (score > bestScore) {
                best = candidate
                bestScore = score
            }
        }
        return if (bestScore >= MIN_SIMILARITY) best else null
    }

    private fun levenshtein(a: String, b: String): Int {
        var previous = IntArray(b.length + 1) { it }
        var current = IntArray(b.length + 1)
        for (i in 1..a.length) {
            current[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                current[j] = minOf(current[j - 1] + 1, previous[j] + 1, previous[j - 1] + cost)
            }
            val swap = previous
            previous = current
            current = swap
        }
        return previous[b.length]
    }
}
