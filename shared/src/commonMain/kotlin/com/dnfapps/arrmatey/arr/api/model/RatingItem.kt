package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.shared.imdb
import com.dnfapps.arrmatey.shared.rating_star
import com.dnfapps.arrmatey.shared.rt_fresh
import com.dnfapps.arrmatey.shared.rt_rotten
import com.dnfapps.arrmatey.shared.tmdb
import com.dnfapps.arrmatey.shared.trakt
import dev.icerock.moko.resources.ImageResource
import io.ktor.http.encodeURLQueryComponent

data class RatingItem(
    val score: String,
    val icon: ImageResource? = null,
    val url: String? = null,
)

fun ArrMedia.toRatingItems(): List<RatingItem> = when (val r = ratings) {
    is MovieRatings -> {
        val movie = this as? ArrMovie
        val tmdbId = movie?.tmdbId
        val encodedTitle = title?.encodeURLQueryComponent()
        listOfNotNull(
            r.imdb?.let {
                RatingItem(it.value.format(), MR.images.imdb, movie?.imdbId?.let { id -> "https://www.imdb.com/title/$id/" })
            },
            r.tmdb?.let {
                RatingItem(
                    "${it.value.times(10).toInt()}%",
                    MR.images.tmdb,
                    tmdbId?.let { id ->
                        "https://www.themoviedb.org/movie/$id"
                    },
                )
            },
            r.rottenTomatoes?.let {
                val icon = if (it.value >= 60) MR.images.rt_fresh else MR.images.rt_rotten
                // Radarr has no RT id, so fall back to a title search.
                val url = encodedTitle?.let { t -> "https://www.rottentomatoes.com/search?search=$t" }
                RatingItem("${it.value.toInt()}%", icon, url)
            },
            r.trakt?.let {
                // Trakt's id lookup route 404s on the new web app; title search still works.
                RatingItem(
                    "${it.value.times(10).toInt()}%",
                    MR.images.trakt,
                    encodedTitle?.let { t -> "https://trakt.tv/search?q=$t" },
                )
            },
        )
    }
    is SeriesRatings -> {
        val series = this as? ArrSeries
        val url =
            series?.tmdbId?.let { "https://www.themoviedb.org/tv/$it" }
                ?: series?.tvdbId?.let { "https://thetvdb.com/dereferrer/series/$it" }
        listOf(RatingItem(r.value.format(), MR.images.tmdb, url))
    }
    is LidarrRatings -> {
        val url = (this as? Arrtist)?.foreignArtistId?.let { "https://musicbrainz.org/artist/$it" }
        listOf(RatingItem(r.value.format(), MR.images.rating_star, url))
    }
    is BookshelfRatings -> {
        val url = (this as? Author)?.links?.firstOrNull()?.url
        listOf(RatingItem(r.value.format(), MR.images.rating_star, url))
    }
    null -> emptyList()
}

private fun Double.format(): String = if (this == 0.0) {
    "0"
} else {
    val s = this.toString()
    val dotIndex = s.indexOf('.')
    if (dotIndex == -1) s else s.substring(0, (dotIndex + 2).coerceAtMost(s.length))
}

private fun Float.format(): String = this.toDouble().format()
