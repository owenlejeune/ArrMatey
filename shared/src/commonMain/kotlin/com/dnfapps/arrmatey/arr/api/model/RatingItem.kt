package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.ImageResource

data class RatingItem(
    val score: String,
    val icon: ImageResource? = null
)

fun ArrRatings.toRatingItems(): List<RatingItem> = when (this) {
    is MovieRatings -> {
        listOfNotNull(
            imdb?.let { RatingItem(it.value.format(), MR.images.imdb) },
            tmdb?.let { RatingItem("${it.value.times(10).toInt()}%", MR.images.tmdb) },
            rottenTomatoes?.let { 
                val icon = if (it.value >= 60) MR.images.rt_fresh else MR.images.rt_rotten
                RatingItem("${it.value.toInt()}%", icon) 
            },
            trakt?.let {
                RatingItem("${it.value.times(10).toInt()}%", MR.images.trakt)
            }
        )
    }
    is SeriesRatings -> listOf(RatingItem(value.format(), MR.images.tmdb))
    is LidarrRatings -> listOf(RatingItem(value.format(), MR.images.tmdb))
    is BookshelfRatings -> listOf(RatingItem(value.format(), MR.images.tmdb))
}

private fun Double.format(): String = if (this == 0.0) "0" else {
    val s = this.toString()
    val dotIndex = s.indexOf('.')
    if (dotIndex == -1) s else s.substring(0, (dotIndex + 2).coerceAtMost(s.length))
}

private fun Float.format(): String = this.toDouble().format()
