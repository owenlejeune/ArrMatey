package com.dnfapps.arrmatey.discover.model

import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Arrtist
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.LidarrRatings
import com.dnfapps.arrmatey.arr.api.model.MockMedia
import com.dnfapps.arrmatey.arr.api.model.MovieRatings
import com.dnfapps.arrmatey.arr.api.model.SearchAudiobook
import com.dnfapps.arrmatey.arr.api.model.SeriesRatings
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.seerr.api.model.DiscoverResult
import com.dnfapps.arrmatey.utils.AspectRatio
import kotlinx.serialization.Serializable

@Serializable
sealed interface SearchResult {
    val id: String
    val title: String
    val cleanTitle: String
    val year: Int?
    val voteCount: Int
    val voteAverage: Double
    val popularity: Double
    var originalRank: Int

    @Serializable
    data class ArrMediaResult(
        val media: ArrMedia,
        val instanceId: Long? = null,
        override var originalRank: Int = 0,
    ) : SearchResult {
        override val id: String = "arr_${media.guid}"
        override val title: String = media.title.orEmpty()
        override val cleanTitle: String = media.cleanTitle ?: media.title.orEmpty()
        override val year: Int? = media.year
        override val voteCount: Int =
            when (val ratings = media.ratings) {
                is MovieRatings -> ratings.tmdb?.votes ?: ratings.imdb?.votes ?: 0
                is SeriesRatings -> ratings.votes
                is LidarrRatings -> ratings.votes
                else -> 0
            }
        override val voteAverage: Double = media.ratingScore()
        override val popularity: Double =
            when (media) {
                is ArrMovie -> media.popularity
                else -> 0.0
            }
        val aspectRatio: AspectRatio =
            when (media) {
                is SearchAudiobook, Audiobook, Author, Arrtist -> AspectRatio.Cover
                else -> AspectRatio.Poster
            }
        val instanceType: InstanceType =
            when (media) {
                is ArrSeries,
                is MockMedia.Sonarr,
                is MockMedia.Default,
                -> InstanceType.Sonarr
                is ArrMovie,
                is MockMedia.Radarr,
                -> InstanceType.Radarr
                is Arrtist,
                is MockMedia.Lidarr,
                -> InstanceType.Lidarr
                is Author,
                is MockMedia.Readarr,
                -> InstanceType.Booksehelf
                is Audiobook,
                is SearchAudiobook,
                is MockMedia.Listenarr,
                -> InstanceType.Listenarr
            }
    }

    @Serializable
    data class SeerrMediaResult(
        val result: DiscoverResult,
        override var originalRank: Int = 0,
    ) : SearchResult {
        override val id: String = "seerr_media_${result.id}"
        override val title: String = result.title ?: result.name.orEmpty()
        override val cleanTitle: String = result.title ?: result.name.orEmpty()
        override val year: Int? = result.releaseDate?.take(4)?.toIntOrNull() ?: result.firstAirDate?.take(4)?.toIntOrNull()
        override val voteCount: Int = result.voteCount
        override val voteAverage: Double = result.voteAverage
        override val popularity: Double = result.popularity
    }

    @Serializable
    data class SeerrPersonResult(
        val result: DiscoverResult,
        override var originalRank: Int = 0,
    ) : SearchResult {
        override val id: String = "seerr_person_${result.id}"
        override val title: String = result.name.orEmpty()
        override val cleanTitle: String = result.name.orEmpty()
        override val year: Int? = null
        override val voteCount: Int = 0
        override val voteAverage: Double = 0.0
        override val popularity: Double = result.popularity
    }
}
