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

    private const val POPULARITY_LOG_FACTOR = 4.0
    private const val VOTE_AVERAGE_FACTOR = 2.0

    private const val RECENCY_BONUS = 10.0
    private const val RECENCY_YEAR_THRESHOLD = 3

    private fun sanitize(input: String): String = input.lowercase().replace("[^a-z0-9]".toRegex(), "")

    fun calculateScore(
        item: SearchResult,
        query: String,
    ): Double {
        val cleanQuery = sanitize(query)
        val cleanTitle = sanitize(item.cleanTitle.ifBlank { item.title })

        var score = max(0.0, BASE_RANK_SCORE - (item.originalRank * RANK_DECAY_FACTOR))

        when {
            cleanTitle == cleanQuery -> score += EXACT_MATCH_BOOST
            cleanTitle.startsWith(cleanQuery) -> score += PREFIX_MATCH_BOOST
            cleanTitle.contains(cleanQuery) -> score += CONTAINS_MATCH_BOOST
        }

        val popularityScore = ln(1.0 + item.voteCount) * POPULARITY_LOG_FACTOR
        score += popularityScore
        score += (item.voteAverage * VOTE_AVERAGE_FACTOR)

        val currentYear = Clock.System.todayIn(TimeZone.currentSystemDefault()).year
        if (item.year != null && (item.year ?: 0) >= (currentYear - RECENCY_YEAR_THRESHOLD)) {
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
            .sortedByDescending { calculateScore(it, query) }
}
