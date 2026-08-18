package com.dnfapps.arrmatey.model

import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Episode as ArrEpisode
import com.dnfapps.arrmatey.arr.api.model.Season as ArrSeason
import com.dnfapps.arrmatey.arr.api.model.FinaleType
import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.LidarrTrack
import com.dnfapps.arrmatey.arr.api.model.LidarrTrackFile
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.BookSeries
import com.dnfapps.arrmatey.arr.api.model.BookFile
import com.dnfapps.arrmatey.arr.api.model.ExtraFile
import com.dnfapps.arrmatey.arr.api.model.RatingItem
import com.dnfapps.arrmatey.arr.api.model.toRatingItems
import com.dnfapps.arrmatey.bazarr.state.BazarrDetails
import com.dnfapps.arrmatey.extensions.formatMinutesAsRuntime
import com.dnfapps.arrmatey.seerr.api.model.Episode as SeerrEpisode
import com.dnfapps.arrmatey.seerr.api.model.Season as SeerrSeason
import com.dnfapps.arrmatey.seerr.api.model.ImdbRating
import com.dnfapps.arrmatey.seerr.api.model.RequestMediaDetails
import com.dnfapps.arrmatey.seerr.api.model.RottenTomatoesRating
import com.dnfapps.arrmatey.seerr.api.model.MovieDetails
import com.dnfapps.arrmatey.seerr.api.model.TvDetails
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.format
import kotlinx.datetime.LocalDate
import kotlin.math.roundToInt
import kotlin.time.Instant

data class SeasonWrapper(
    val seasonNumber: Int,
    val arrSeason: ArrSeason? = null,
    val seerrSeason: SeerrSeason? = null,
    val episodes: List<EpisodeWrapper> = emptyList()
) {
    val totalEpisodeCount: Int
        get() = arrSeason?.statistics?.totalEpisodeCount ?: seerrSeason?.episodeCount ?: episodes.size

    val episodeFileCount: Int?
        get() = arrSeason?.statistics?.episodeFileCount

    val monitored: Boolean?
        get() = arrSeason?.monitored

    val isMonitored: Boolean
        get() = arrSeason?.monitored == true

    val hasArrSeason: Boolean
        get() = arrSeason != null

    val name: String?
        get() = seerrSeason?.name
}

data class EpisodeWrapper(
    val arrEpisode: ArrEpisode? = null,
    val seerrEpisode: SeerrEpisode? = null,
    val isActive: Boolean = false,
    val activityProgress: String? = null
) {
    val seasonNumber: Int
        get() = arrEpisode?.seasonNumber ?: seerrEpisode?.seasonNumber ?: 0

    val episodeNumber: Int
        get() = arrEpisode?.episodeNumber ?: seerrEpisode?.episodeNumber ?: 0

    val title: String?
        get() = arrEpisode?.displayTitle ?: seerrEpisode?.name

    val overview: String?
        get() = arrEpisode?.overview ?: seerrEpisode?.overview

    val stillPath: String?
        get() = seerrEpisode?.stillPath ?: arrEpisode?.images?.firstOrNull()?.remoteUrl

    val airDate: LocalDate?
        get() = arrEpisode?.airDate ?: seerrEpisode?.airDate

    val airDateUtc: Instant?
        get() = arrEpisode?.airDateUtc

    val monitored: Boolean?
        get() = arrEpisode?.monitored

    val isMonitored: Boolean
        get() = arrEpisode?.monitored == true

    val hasFile: Boolean
        get() = arrEpisode?.hasFile == true

    val fileQualityName: String?
        get() = arrEpisode?.fileQualityName

    val finaleType: FinaleType?
        get() = arrEpisode?.finaleType

    fun formatAirDateUtc(): String? = arrEpisode?.formatAirDateUtc()
}

sealed interface UnifiedMediaDetailsUiState {
    object Initial : UnifiedMediaDetailsUiState
    object Loading : UnifiedMediaDetailsUiState
    data class Error(val message: String?) : UnifiedMediaDetailsUiState
    data class Success(
        val arrMedia: ArrMedia? = null,
        val seerrMedia: RequestMediaDetails? = null,
        val bazarrMedia: BazarrDetails? = null,
        val rtRatings: RottenTomatoesRating? = null,
        val imdbRatings: ImdbRating? = null,

        val seasons: List<SeasonWrapper> = emptyList(),
        val episodes: List<EpisodeWrapper> = emptyList(),

        // Arr-specific additions (Seasons, Albums, etc.)
        val albums: List<ArrAlbum> = emptyList(),
        val tracks: Map<Long, List<LidarrTrack>> = emptyMap(),
        val trackFiles: Map<Long, List<LidarrTrackFile>> = emptyMap(),
        val bookSeries: List<BookSeries> = emptyList(),
        val bookFiles: List<BookFile> = emptyList(),
        val books: List<Book> = emptyList(),
        val extraFiles: List<ExtraFile> = emptyList(),

        val isMonitored: Boolean = false
    ) : UnifiedMediaDetailsUiState {

        val hasArrId: Boolean
            get() = arrMedia?.let { it.id != null && it.id != 0L } ?: false

        val displayTitle: String?
            get() = seerrMedia?.displayTitle ?: arrMedia?.title

        val tagline: String?
            get() = seerrMedia?.tagline

        val overview: String?
            get() = seerrMedia?.overview ?: arrMedia?.overview

        val bannerUrl: String?
            get() = seerrMedia?.fullBackdropPath ?: arrMedia?.getBanner()?.remoteUrl

        val posterUrl: String?
            get() = seerrMedia?.fullPosterPath ?: arrMedia?.getPoster()?.remoteUrl

        val clearLogo: String?
            get() = arrMedia?.getClearLogo()?.remoteUrl

        val ratings: List<RatingItem>
            get() {
                val seerrRatings = if (seerrMedia != null) {
                    buildList {
                        rtRatings?.let { rt ->
                            if (rt.criticsScore != null && rt.criticsRating != null) {
                                add(RatingItem("${rt.criticsScore}%", rt.criticsRating.icon))
                            }
                            if (rt.audienceRating != null && rt.audienceScore != null) {
                                add(RatingItem("${rt.audienceScore}%", rt.audienceRating.icon))
                            }
                        }
                        imdbRatings?.let { imdb ->
                            add(RatingItem("${(imdb.criticsScore * 10).roundToInt()}%", MR.images.imdb))
                        }
                        add(RatingItem("${(seerrMedia.voteAverage * 10).roundToInt()}%", MR.images.tmdb))
                    }
                } else emptyList()

                val arrRatings = arrMedia?.ratings?.toRatingItems() ?: emptyList()

                val combined = ArrayList(seerrRatings)
                val addedProviders = combined.mapNotNull { it.provider }.toMutableSet()

                for (item in arrRatings) {
                    val prov = item.provider
                    if (prov == null || !addedProviders.contains(prov)) {
                        combined.add(item)
                        if (prov != null) {
                            addedProviders.add(prov)
                        }
                    }
                }
                return combined
            }

        val year: String?
            get() = seerrMedia?.displayDate?.format("yyyy") ?: arrMedia?.year?.toString()

        val runtimeString: String?
            get() = (seerrMedia as? MovieDetails)?.runtime?.formatMinutesAsRuntime() ?: arrMedia?.runtimeString

        val releasedBy: String?
            get() = arrMedia?.releasedBy

        val seasonCount: Int?
            get() = (seerrMedia as? TvDetails)?.seasons?.size ?: (arrMedia as? ArrSeries)?.seasons?.size

        val genres: List<String>
            get() = seerrMedia?.genres?.map { it.name } ?: arrMedia?.genres ?: emptyList()

        fun getCertification(countryCode: String): String? {
            return seerrMedia?.getCertification(countryCode) ?: arrMedia?.certification
        }

    }
}

private val RatingItem.provider: String?
    get() = when (icon) {
        MR.images.imdb -> "imdb"
        MR.images.tmdb -> "tmdb"
        MR.images.rt_fresh, MR.images.rt_rotten -> "rt_critics"
        MR.images.rt_aud_fresh, MR.images.rt_aud_rotten -> "rt_audience"
        MR.images.trakt -> "trakt"
        else -> null
    }
