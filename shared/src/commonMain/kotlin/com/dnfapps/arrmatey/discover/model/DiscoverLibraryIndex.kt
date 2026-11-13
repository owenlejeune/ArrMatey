package com.dnfapps.arrmatey.discover.model

import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.mediaserver.model.MediaType
import com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaItem

val CLEAN_TITLE_REGEX = Regex("[^a-zA-Z0-9]")

class DiscoverLibraryIndex(arrLibraries: List<ArrMedia>) {
    private val moviesByTmdbId = HashMap<Long, ArrMovie>()
    private val moviesByCleanTitle = HashMap<String, ArrMovie>()
    private val seriesByTmdbId = HashMap<Long, ArrSeries>()
    private val seriesByTvdbId = HashMap<Long, ArrSeries>()
    private val seriesByCleanTitle = HashMap<String, ArrSeries>()

    init {
        arrLibraries.forEach { media ->
            when (media) {
                is ArrMovie -> {
                    if (media.tmdbId != 0L) {
                        moviesByTmdbId[media.tmdbId] = media
                    }
                    val clean = media.cleanTitle?.lowercase()
                    if (!clean.isNullOrEmpty()) {
                        moviesByCleanTitle[clean] = media
                    }
                }
                is ArrSeries -> {
                    val tmdb = media.tmdbId
                    if (tmdb != null && tmdb != 0L) {
                        seriesByTmdbId[tmdb] = media
                    }
                    if (media.tvdbId != 0L) {
                        seriesByTvdbId[media.tvdbId] = media
                    }
                    val clean = media.cleanTitle?.lowercase()
                    if (!clean.isNullOrEmpty()) {
                        seriesByCleanTitle[clean] = media
                    }
                }
                else -> {}
            }
        }
    }

    fun findMovie(tmdbId: Long, mediaInfoTmdbId: Long? = null, cleanTitle: String? = null): ArrMovie? {
        if (tmdbId != 0L) {
            moviesByTmdbId[tmdbId]?.let { return it }
        }
        if (mediaInfoTmdbId != null && mediaInfoTmdbId != 0L) {
            moviesByTmdbId[mediaInfoTmdbId]?.let { return it }
        }
        if (!cleanTitle.isNullOrEmpty()) {
            moviesByCleanTitle[cleanTitle]?.let { return it }
        }
        return null
    }

    fun findSeries(
        tmdbId: Long? = null,
        tvdbId: Long? = null,
        mediaInfoTmdbId: Long? = null,
        mediaInfoTvdbId: Long? = null,
        cleanTitle: String? = null,
    ): ArrSeries? {
        if (tvdbId != null && tvdbId != 0L) {
            seriesByTvdbId[tvdbId]?.let { return it }
        }
        if (mediaInfoTvdbId != null && mediaInfoTvdbId != 0L) {
            seriesByTvdbId[mediaInfoTvdbId]?.let { return it }
        }
        if (tmdbId != null && tmdbId != 0L) {
            seriesByTmdbId[tmdbId]?.let { return it }
        }
        if (mediaInfoTmdbId != null && mediaInfoTmdbId != 0L) {
            seriesByTmdbId[mediaInfoTmdbId]?.let { return it }
        }
        if (!cleanTitle.isNullOrEmpty()) {
            seriesByCleanTitle[cleanTitle]?.let { return it }
        }
        return null
    }
}

class DiscoverMediaServerIndex(serverItems: List<UnifiedMediaItem>) {
    private val itemsByTmdbId = HashMap<Long, UnifiedMediaItem>()
    private val itemsByTvdbId = HashMap<Long, UnifiedMediaItem>()
    private val itemsByCleanTitle = HashMap<Pair<String, MediaType?>, UnifiedMediaItem>()

    init {
        serverItems.forEach { item ->
            if (item.tmdbId != null && item.tmdbId != 0L) {
                itemsByTmdbId[item.tmdbId] = item
            }
            if (item.tvdbId != null && item.tvdbId != 0L) {
                itemsByTvdbId[item.tvdbId] = item
            }
            val clean = item.title.replace(CLEAN_TITLE_REGEX, "").lowercase()
            if (clean.isNotEmpty()) {
                itemsByCleanTitle[clean to item.type] = item
            }
        }
    }

    fun findMatch(
        tmdbId: Long?,
        tvdbId: Long?,
        title: String?,
        year: Int?,
        mediaType: MediaType?,
    ): UnifiedMediaItem? {
        if (tmdbId != null && tmdbId > 0) {
            itemsByTmdbId[tmdbId]?.let { return it }
        }
        if (tvdbId != null && tvdbId > 0) {
            itemsByTvdbId[tvdbId]?.let { return it }
        }
        if (!title.isNullOrBlank()) {
            val cleanTitle = title.replace(CLEAN_TITLE_REGEX, "").lowercase()
            val match = itemsByCleanTitle[cleanTitle to mediaType]
                ?: itemsByCleanTitle[cleanTitle to null]
            if (match != null) {
                val yearMatches = year == null || match.year == null || match.year == year
                if (yearMatches) return match
            }
        }
        return null
    }
}
