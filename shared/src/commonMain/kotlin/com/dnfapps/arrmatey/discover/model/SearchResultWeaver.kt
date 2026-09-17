package com.dnfapps.arrmatey.discover.model

import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.math.ln
import kotlin.math.max
import kotlin.time.Clock

object SearchResultWeaver {
    private const val BASE_RANK_SCORE = 100.0
    private const val RANK_DECAY_FACTOR = 15.0

    private const val EXACT_MATCH_BOOST = 150.0
    private const val PREFIX_MATCH_BOOST = 75.0
    private const val CONTAINS_MATCH_BOOST = 30.0

    private const val VOTE_COUNT_LOG_FACTOR = 4.0
    private const val POPULARITY_LOG_FACTOR = 3.0
    private const val VOTE_AVERAGE_FACTOR = 2.0

    private const val RECENCY_BONUS = 10.0
    private const val RECENCY_YEAR_THRESHOLD = 3
    private const val IN_LIBRARY_BONUS = 5.0

    private const val MIN_SCORE = 0.0
    private const val MIN_POPULARITY = 0.0
    private const val LOG_OFFSET = 1.0
    private const val DEFAULT_YEAR = 0

    private val SANITIZE_REGEX = "[^a-z0-9]".toRegex()

    private fun sanitize(input: String): String = input.lowercase().replace(SANITIZE_REGEX, "")

    fun calculateScore(
        item: SearchResult,
        query: String,
    ): Double {
        val cleanQuery = sanitize(query)
        val cleanTitle = sanitize(item.cleanTitle.ifBlank { item.title })

        var score = max(MIN_SCORE, BASE_RANK_SCORE - (item.originalRank * RANK_DECAY_FACTOR))

        when {
            cleanTitle == cleanQuery -> score += EXACT_MATCH_BOOST
            cleanTitle.startsWith(cleanQuery) -> score += PREFIX_MATCH_BOOST
            cleanTitle.contains(cleanQuery) -> score += CONTAINS_MATCH_BOOST
        }

        val voteCountScore = ln(LOG_OFFSET + item.voteCount) * VOTE_COUNT_LOG_FACTOR
        score += voteCountScore

        val popScore = ln(LOG_OFFSET + max(MIN_POPULARITY, item.popularity)) * POPULARITY_LOG_FACTOR
        score += popScore

        score += (item.voteAverage * VOTE_AVERAGE_FACTOR)

        if (item is SearchResult.ArrMediaResult && item.media.id != null) {
            score += IN_LIBRARY_BONUS
        }

        val currentYear = Clock.System.todayIn(TimeZone.currentSystemDefault()).year
        if (item.year != null && (item.year ?: DEFAULT_YEAR) >= (currentYear - RECENCY_YEAR_THRESHOLD)) {
            score += RECENCY_BONUS
        }

        return score
    }

    fun weave(
        query: String,
        results: List<SearchResult>,
    ): List<SearchResult> =
        results
            .distinctBy { it.id }
            .sortedWith(
                compareByDescending<SearchResult> { calculateScore(it, query) }
                    .thenByDescending { it.popularity }
                    .thenByDescending { it.voteCount }
                    .thenByDescending { it.voteAverage }
                    .thenByDescending { it.year ?: DEFAULT_YEAR }
                    .thenBy { it.title }
                    .thenBy { it.id }
            )
}
